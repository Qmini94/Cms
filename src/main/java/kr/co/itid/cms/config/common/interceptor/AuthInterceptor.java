package kr.co.itid.cms.config.common.interceptor;

import kr.co.itid.cms.config.security.JwtTokenProvider;
import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.service.auth.SessionManager;
import kr.co.itid.cms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final SessionManager sessionManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        try {
            JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
            if (user.isGuest() || user.isDev()) return true;

            String sessionId = user.sessionId();
            if (sessionId == null) {
                log.warn("[AuthInterceptor] 세션 ID가 없는 토큰 - 재로그인 필요");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"유효하지 않은 토큰입니다. 다시 로그인해주세요.\"}");
                return false;
            }

            // Redis 세션 TTL 연장
            boolean sessionExtended = sessionManager.extendSession(sessionId);
            
            // JWT 토큰 갱신 체크 (만료 5분 전에 갱신)
            if (sessionExtended && isTokenNearExpiry(user)) {
                refreshJwtToken(user, response);
            }
            
            if (!sessionExtended) {
                // Redis 장애이거나 세션이 만료된 경우
                if (!sessionManager.isRedisHealthy()) {
                    log.error("[AuthInterceptor] Redis 장애 감지 - 서비스 일시 중단");
                    response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"서비스가 일시적으로 사용할 수 없습니다. 잠시 후 다시 시도해주세요.\"}");
                    return false;
                } else {
                    log.info("[AuthInterceptor] 세션 만료 - 401 응답");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"세션이 만료되었습니다. 다시 로그인해주세요.\"}");
                    return false;
                }
            } else {
                log.debug("[AuthInterceptor] 세션 TTL 연장 완료: sid={}", sessionId);
            }

        } catch (Exception e) {
            log.error("[AuthInterceptor] 인증 처리 중 오류: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"인증 처리 중 오류가 발생했습니다.\"}");
            return false;
        }
        return true;
    }
    
    /**
     * JWT 토큰이 만료 임박인지 확인 (5분 이내)
     */
    private boolean isTokenNearExpiry(JwtAuthenticatedUser user) {
        long currentTime = System.currentTimeMillis() / 1000; // 현재 시간 (초)
        long tokenExpiry = user.exp(); // 토큰 만료 시간 (초)
        long timeUntilExpiry = tokenExpiry - currentTime;
        
        // 5분(300초) 이내에 만료되면 갱신
        return timeUntilExpiry <= 300;
    }
    
    /**
     * JWT 토큰 갱신
     */
    private void refreshJwtToken(JwtAuthenticatedUser user, HttpServletResponse response) {
        try {
            // 기존 토큰에서 새 토큰 생성
            String newToken = jwtTokenProvider.recreateTokenFrom(user.token());
            
            // 새 쿠키 설정
            ResponseCookie cookie = jwtTokenProvider.createAccessTokenCookie(newToken);
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            
            // SecurityContext 업데이트
            var claims = jwtTokenProvider.getClaimsFromToken(newToken);
            JwtAuthenticatedUser newUser = new JwtAuthenticatedUser(
                    claims.get("idx", Long.class),
                    claims.getSubject(),
                    claims.get("userName", String.class),
                    claims.get("userLevel", Integer.class),
                    claims.get("exp", Long.class),
                    newToken,
                    user.hostname(),
                    user.menuId(),
                    claims.get("sid", String.class)
            );
            
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(newUser, null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.debug("[AuthInterceptor] JWT 토큰 갱신 완료: userId={}", user.userId());
            
        } catch (Exception e) {
            log.error("[AuthInterceptor] JWT 토큰 갱신 실패: {}", e.getMessage());
        }
    }
}

package kr.co.itid.cms.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.dto.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = jwtTokenProvider.resolveToken(request);
            String uri = request.getRequestURI();
            String referer = request.getHeader("Referer");

            // DEBUG: 요청 정보 로그
            logger.info("[JWT] 요청 URI: " + uri); // DEBUG:
            logger.info("[JWT] Referer: " + referer); // DEBUG:
            logger.info("[JWT] 토큰 존재 여부: " + StringUtils.hasText(token)); // DEBUG:

            JwtAuthenticatedUser user = null;

            if (StringUtils.hasText(token)) {
                if (jwtTokenProvider.isBlacklisted(token)) {
                    logger.info("[JWT] 블랙리스트 토큰 감지 → 게스트로 처리"); // DEBUG:
                    user = createGuestUser(request);
                } else if (jwtTokenProvider.validateToken(token)) {
                    logger.info("[JWT] 유효한 토큰 → 사용자 인증 처리"); // DEBUG:
                    Claims claims = jwtTokenProvider.getClaimsFromToken(token);
                    user = new JwtAuthenticatedUser(
                            claims.get("idx", Long.class),
                            claims.getSubject(),
                            claims.get("userName", String.class),
                            claims.get("userLevel", Integer.class),
                            token
                    );
                } else {
                    logger.info("[JWT] 토큰 유효성 검증 실패 → 게스트 처리"); // DEBUG:
                    user = createGuestUser(request);
                }
            } else {
                logger.info("[JWT] 토큰 없음 → 게스트 처리"); // DEBUG:
                user = createGuestUser(request);
            }

            // DEBUG: 최종 인증 유저 정보 출력
            logger.info("[JWT] 인증 유저: " + user.userId() + ", 레벨: " + user.userLevel()); // DEBUG:

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 관리자 접근 체크
            if (isUnauthorizedAdmin(user, request)) {
                logger.warn("[JWT] 관리자 페이지 접근 권한 없음 → 403 응답"); // DEBUG:
                writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "관리자 권한 필요");
                return;
            }

        } catch (Exception e) {
            logger.error("JWT 필터 처리 중 예외 발생", e);
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "필터 처리 오류");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private JwtAuthenticatedUser createGuestUser(HttpServletRequest request) {
        String referer = request.getHeader("Referer");

        // DEBUG: 개발 환경 프론트에서 접근 시 관리자 권한 부여
        if (referer != null && referer.startsWith("http://localhost:3000")) {
            logger.info("[JWT] 로컬 개발환경 접근 → 관리자 권한 임시 부여"); // DEBUG:
            return new JwtAuthenticatedUser(
                    0L,
                    "DEV_ADMIN",
                    "개발관리자",
                    1, // 관리자 레벨
                    "dev-token"
            );
        }

        // 기본 게스트
        return new JwtAuthenticatedUser(
                -1L,
                "GUEST",
                "게스트",
                11,
                "not has token"
        );
    }

    private boolean isAdminAccess(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        String uri = request.getRequestURI();

        boolean result = referer != null
                && referer.contains("/admin/")
                && !uri.startsWith("/api/auth/");

        // DEBUG: 관리자 접근 여부 로그
        logger.info("[JWT] 관리자 접근 판단: " + result); // DEBUG:

        return result;
    }

    private boolean isUnauthorizedAdmin(JwtAuthenticatedUser user, HttpServletRequest request) {
        boolean unauthorized = isAdminAccess(request) && !user.isAdmin();
        // DEBUG: 관리자 권한 체크 로그
        logger.info("[JWT] 관리자 권한 있음?: " + user.isAdmin() + ", 접근 차단?: " + unauthorized); // DEBUG:
        return unauthorized;
    }

    private void writeJsonError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<?> body = ApiResponse.error(statusCode, message);
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
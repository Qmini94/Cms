package kr.co.itid.cms.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.config.security.model.JwtProperties;
import kr.co.itid.cms.config.security.model.SessionData;
import kr.co.itid.cms.config.security.port.SiteAccessChecker;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.auth.SessionManager;
import kr.co.itid.cms.util.AuthCookieUtil;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final SiteAccessChecker siteAccessChecker;
    private final SessionManager sessionManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String uri = request.getRequestURI();
            String hostname = request.getHeader("X-Site-Hostname");
            hostname = StringUtils.hasText(hostname) ? hostname : "unknown";

            String clientIp = getClientIp(request);
            logger.info("[JWT] IP: " + clientIp + ", URI: " + uri);

            // IP 접근 제어
            if (!siteAccessChecker.isIpAllowed(hostname, clientIp)) {
                writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "차단된 IP입니다.");
                return;
            }

            String accessToken = jwtTokenProvider.extractAccessTokenFromRequest(request);
            String menuIdHeader = request.getHeader("X-Menu-Id");
            Long menuId = StringUtils.hasText(menuIdHeader) ? Long.valueOf(menuIdHeader) : null;

            JwtAuthenticatedUser user;

            if (StringUtils.hasText(accessToken)) {
                try {
                    jwtTokenProvider.validateToken(accessToken);

                    Claims claims = jwtTokenProvider.getClaimsFromToken(accessToken);
                    String sid = claims.get("sid", String.class);
                    if (!StringUtils.hasText(sid)) {
                        unauthorizedAndClear(response, "세션 정보가 없습니다.");
                        return;
                    }

                    Long sessionExpEpoch;
                    if (sessionManager.isRedisHealthy()) {
                        Optional<SessionData> sessionOpt = sessionManager.getSession(sid);
                        if (sessionOpt.isEmpty()) {
                            unauthorizedAndClear(response, "세션이 만료되었거나 철회되었습니다.");
                            return;
                        }
                        // 슬라이딩
                        sessionManager.touchSession(sid);
                        sessionExpEpoch = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                                .plusSeconds(jwtProperties.getSessionTtlSeconds()) // yml 기반
                                .toEpochSecond();
                    } else {
                        // 폴백: 토큰 커스텀 exp
                        sessionExpEpoch = claims.get("exp", Long.class);
                    }

                    if (sessionExpEpoch != null) {
                        AuthCookieUtil.setSessionExpires(response, sessionExpEpoch);
                    }

                    user = new JwtAuthenticatedUser(
                            claims.get("idx", Long.class),
                            claims.getSubject(),
                            claims.get("userName", String.class),
                            claims.get("userLevel", Integer.class),
                            sessionExpEpoch,
                            accessToken,
                            hostname,
                            menuId,
                            sid
                    );

                } catch (ExpiredJwtException eje) {
                    user = tryReissueWithRefresh(request, response, hostname, menuId);
                }
            } else {
                user = tryReissueWithRefresh(request, response, hostname, menuId);
            }

            if (user == null) {
                AuthCookieUtil.clearAll(response);
                user = createGuestUser(request, hostname, menuId);
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 관리자 접근 체크
            if (isUnauthorizedAdmin(user, uri, hostname) && !AdminByPassPaths.ALLOWED_PATHS.contains(uri)) {
                writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "관리자 권한 필요");
                return;
            }

        } catch (Exception e) {
            logger.error("JWT 필터 처리 중 예외", e);
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "필터 처리 오류");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private JwtAuthenticatedUser tryReissueWithRefresh(HttpServletRequest request,
                                                       HttpServletResponse response,
                                                       String hostname,
                                                       Long menuId) {
        try {
            if (!sessionManager.isRedisHealthy()) {
                return null;
            }
            String refresh = jwtTokenProvider.extractRefreshTokenFromRequest(request);
            if (!StringUtils.hasText(refresh)) return null;

            jwtTokenProvider.validateRefreshToken(refresh);
            Claims rClaims = jwtTokenProvider.getClaimsFromRefreshToken(refresh);

            String sid = rClaims.get("sid", String.class);
            if (!StringUtils.hasText(sid)) return null;

            Optional<SessionData> sessionOpt = sessionManager.getSession(sid);
            if (sessionOpt.isEmpty()) return null;

            SessionData s = sessionOpt.get();

            // 슬라이딩 후 세션 만료(epoch) = now + sessionTtlSeconds
            sessionManager.touchSession(sid);
            long sessionExpEpoch = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                    .plusSeconds(jwtProperties.getSessionTtlSeconds())     // yml 기반
                    .toEpochSecond();

            Map<String, Object> claims = new HashMap<>();
            claims.put("userLevel", s.getUserLevel());
            claims.put("userName", s.getUserName());
            claims.put("idx", s.getIdx());
            claims.put("sid", sid);
            claims.put("exp", sessionExpEpoch); // 프론트 편의상 동일 값

            String newAccess = jwtTokenProvider.createToken(s.getUserId(), claims);

            // ACCESS 쿠키 TTL = accessTokenValidity
            AuthCookieUtil.setAccessToken(response, newAccess, Duration.ofSeconds(jwtProperties.getAccessTokenValidity()));
            AuthCookieUtil.setSessionExpires(response, sessionExpEpoch);

            return new JwtAuthenticatedUser(
                    s.getIdx(), s.getUserId(), s.getUserName(), s.getUserLevel(),
                    sessionExpEpoch, newAccess, hostname, menuId, sid
            );

        } catch (ExpiredJwtException e) {
            logger.info("[JWT] REFRESH 만료 → 게스트 처리");
            return null;
        } catch (Exception e) {
            logger.info("[JWT] ACCESS 재발급 실패: {}" + e.getMessage());
            return null;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        String ip = (xfHeader != null && !xfHeader.isEmpty())
                ? xfHeader.split(",")[0].trim()
                : request.getRemoteAddr();

        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) ip = "127.0.0.1";
        if (ip.startsWith("::ffff:")) ip = ip.substring(7);
        return ip;
    }

    private JwtAuthenticatedUser createGuestUser(HttpServletRequest request, String hostname, Long menuId) {
        String origin = request.getHeader("Origin");
        long exp = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).plusMinutes(30).toEpochSecond();

        if ("https://localhost:3000".equalsIgnoreCase(origin)) {
            return new JwtAuthenticatedUser(0L, "DEV_ADMIN", "개발관리자",
                    1, exp, "dev-token", hostname, menuId, null);
        }
        return new JwtAuthenticatedUser(-1L, "GUEST", "게스트",
                11, exp, "not has token", hostname, menuId, null);
    }

    private boolean isAdminAccess(String uri, String hostname) throws Exception {
        if (hostname == null || uri.startsWith("/back-api/auth/")) return false;
        return siteAccessChecker.isClosedSite(hostname);
    }

    private boolean isUnauthorizedAdmin(JwtAuthenticatedUser user, String uri, String hostname) throws Exception {
        return isAdminAccess(uri, hostname) && !user.isAdmin();
    }

    private void unauthorizedAndClear(HttpServletResponse response, String msg) throws IOException {
        AuthCookieUtil.clearAll(response);
        writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, msg);
    }

    private void writeJsonError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<?> body = ApiResponse.error(statusCode, message);
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
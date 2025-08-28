package kr.co.itid.cms.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.config.security.port.SiteAccessChecker;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final SiteAccessChecker siteAccessChecker;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String uri = request.getRequestURI();
            String hostname = request.getHeader("X-Site-Hostname");
            hostname = StringUtils.hasText(hostname) ? hostname : "unknown";

            String clientIp = getClientIp(request);
            logger.info("[JWT] 클라이언트 IP: " + clientIp);

            if (!siteAccessChecker.isIpAllowed(hostname, clientIp)) {
                logger.warn("[JWT] 차단된 IP 접근 감지 → IP: " + clientIp + ", Hostname: " + hostname);
                writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, "차단된 IP입니다.");
                return;
            }

            String token = jwtTokenProvider.extractAccessTokenFromRequest(request);
            String menuIdHeader = request.getHeader("X-Menu-Id");
            Long menuId = StringUtils.hasText(menuIdHeader) ? Long.valueOf(menuIdHeader) : null;

            // DEBUG: 요청 정보 로그
            logger.info("[JWT] 요청 URI: " + uri); // DEBUG:
            logger.info("[JWT] X-Site-Hostname: " + hostname); // DEBUG:
            logger.info("[JWT] X-Menu-Id: " + menuId); // DEBUG:
            logger.info("[JWT] 토큰 존재 여부: " + StringUtils.hasText(token)); // DEBUG:

            JwtAuthenticatedUser user = null;

            if (StringUtils.hasText(token)) {
                try {
                    jwtTokenProvider.validateToken(token);

                    logger.info("[JWT] 유효한 토큰 → 사용자 인증 처리");
                    Claims claims = jwtTokenProvider.getClaimsFromToken(token);
                    user = new JwtAuthenticatedUser(
                            claims.get("idx", Long.class),
                            claims.getSubject(),
                            claims.get("userName", String.class),
                            claims.get("userLevel", Integer.class),
                            claims.get("exp", Long.class),
                            token,
                            hostname,
                            menuId
                    );

                } catch (Exception e) {
                    logger.info("[JWT] 토큰 검증 실패 ({}): 게스트 처리" + e.getMessage()); // DEBUG:
                    user = createGuestUser(request, hostname, menuId);
                }

            } else {
                logger.info("[JWT] 토큰 없음 → 게스트 처리"); // DEBUG:
                user = createGuestUser(request, hostname, menuId);
            }

            // DEBUG: 최종 인증 유저 정보 출력
            logger.info("[JWT] 인증 유저: " + user.userId() + ", 레벨: " + user.userLevel()); // DEBUG:

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 관리자 접근 체크
            if (isUnauthorizedAdmin(user, uri, hostname) && !AdminByPassPaths.ALLOWED_PATHS.contains(uri)) {
                logger.warn("[JWT] 관리자 페이지 접근 권한 없음 → 403 응답");
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

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        String ip = (xfHeader != null && !xfHeader.isEmpty())
                ? xfHeader.split(",")[0].trim()
                : request.getRemoteAddr();

        // IPv6 루프백 → IPv4로 변환
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }

        // IPv4-mapped IPv6 (::ffff:192.168.0.1) → IPv4 추출
        if (ip.startsWith("::ffff:")) {
            ip = ip.substring(7);
        }

        return ip;
    }

    private JwtAuthenticatedUser createGuestUser(HttpServletRequest request, String hostname, Long menuId) {
        String origin = request.getHeader("Origin");
        logger.info("[JWT] 현재 Origin 헤더: " + origin);

        long exp = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                .plusMinutes(30)
                .toEpochSecond();

        if ("https://localhost:3000".equalsIgnoreCase(origin)) {
            logger.info("[JWT] 로컬 개발환경 접근 → 관리자 권한 임시 부여");
            return new JwtAuthenticatedUser(
                    0L,
                    "DEV_ADMIN",
                    "개발관리자",
                    1, // 관리자 레벨
                    exp,
                    "dev-token",
                    hostname,
                    menuId
            );
        }

        return new JwtAuthenticatedUser(
                -1L,
                "GUEST",
                "게스트",
                11,
                exp,
                "not has token",
                hostname,
                menuId
        );
    }

    private boolean isAdminAccess(String uri, String hostname) throws Exception {
        if (hostname == null || uri.startsWith("/back-api/auth/")) {
            return false;
        }

        boolean result = siteAccessChecker.isClosedSite(hostname);

        logger.info("[JWT] 관리자 접근 판단 - siteHostName: " + hostname + ", result: " + result);
        return result;
    }

    private boolean isUnauthorizedAdmin(JwtAuthenticatedUser user, String uri, String hostname) throws Exception {
        boolean unauthorized = isAdminAccess(uri, hostname) && !user.isAdmin();

        logger.info("[JWT] 관리자 권한 있음?: " + user.isAdmin() + ", 접근 차단?: " + unauthorized);
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
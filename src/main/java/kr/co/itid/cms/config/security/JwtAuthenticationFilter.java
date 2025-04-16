package kr.co.itid.cms.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.dto.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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

            JwtAuthenticatedUser user = null;

            if (StringUtils.hasText(token)) {
                if (jwtTokenProvider.isBlacklisted(token)) {
                    user = createGuestUser();
                } else if (jwtTokenProvider.validateToken(token)) {
                    Claims claims = jwtTokenProvider.getClaimsFromToken(token);
                    user = new JwtAuthenticatedUser(
                            claims.get("idx", Long.class),
                            claims.getSubject(),
                            claims.get("userName", String.class),
                            claims.get("userLevel", Integer.class),
                            token
                    );

                    // 슬라이딩 토큰 재발급
                    String newToken = jwtTokenProvider.recreateTokenFrom(token);
                    ResponseCookie cookie = jwtTokenProvider.createAccessTokenCookie(newToken);
                    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
                } else {
                    user = createGuestUser();
                }
            } else {
                user = createGuestUser();
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 관리자 권한 체크
            if (isUnauthorizedAdmin(user, request)) {
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

    private JwtAuthenticatedUser createGuestUser() {
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

        return referer != null && referer.contains("/admin/") && !uri.startsWith("/auth/");
    }

    private boolean isUnauthorizedAdmin(JwtAuthenticatedUser user, HttpServletRequest request) {
        return isAdminAccess(request) && !user.isAdmin();
    }

    private void writeJsonError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<?> body = ApiResponse.error(statusCode, message);
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
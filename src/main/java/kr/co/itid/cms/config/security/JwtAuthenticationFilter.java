package kr.co.itid.cms.config.security;

import io.jsonwebtoken.Claims;
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
import java.util.HashMap;
import java.util.Map;

import static kr.co.itid.cms.config.security.SecurityConstants.GUEST_USER;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = jwtTokenProvider.resolveToken(request);

            if (StringUtils.hasText(token)) {
                if (jwtTokenProvider.isBlacklisted(token)) {
                    setGuestContext();
                } else if (jwtTokenProvider.validateToken(token)) {
                    Claims claims = jwtTokenProvider.getClaimsFromToken(token);
                    String userId = claims.getSubject();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, claims, null);

                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // 슬라이딩 토큰 재발급
                    String newToken = jwtTokenProvider.recreateTokenFrom(token);
                    ResponseCookie cookie = jwtTokenProvider.createAccessTokenCookie(newToken);
                    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
                } else {
                    setGuestContext();  // 수정 필요
                }
            } else {
                setGuestContext();  // 수정 필요
            }
        } catch (Exception e) {
            logger.error("JWT 필터 처리 중 예외 발생", e);
        }

        filterChain.doFilter(request, response);
    }

    private void setGuestContext() {
        Map<String, Object> guestClaims = new HashMap<>();
        guestClaims.put("userLevel", 11);
        guestClaims.put("idx", -1);

        UsernamePasswordAuthenticationToken guestAuth =
                new UsernamePasswordAuthenticationToken(GUEST_USER, guestClaims, null);
        SecurityContextHolder.getContext().setAuthentication(guestAuth);
    }
}
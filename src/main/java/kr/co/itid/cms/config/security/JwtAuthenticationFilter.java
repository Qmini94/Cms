package kr.co.itid.cms.config.security;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
                //블랙리스트 여부 확인
                if (jwtTokenProvider.isBlacklisted(token)) {
                    setGuestContext(request); //게스트 처리
                } else if (jwtTokenProvider.validateToken(token)) {
                    //정상 토큰
                    String userId = jwtTokenProvider.getUserId(token);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, null);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    //요청 시마다 토큰 재발급(슬라이딩 방식)
                    String newToken = jwtTokenProvider.recreateTokenFrom(token);
                    ResponseCookie cookie = jwtTokenProvider.createAccessTokenCookie(newToken);
                    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
                } else {
                    // 유효하지 않은 토큰 → 익명 사용자 처리
                    setGuestContext(request);
                }
            } else {
                setGuestContext(request); //게스트 처리
            }

        } catch (Exception e) {
            logger.error("JWT 필터 처리 중 예외 발생", e);
        }

        filterChain.doFilter(request, response);
    }

    private void setGuestContext(HttpServletRequest request) {
        UsernamePasswordAuthenticationToken guestAuth =
                new UsernamePasswordAuthenticationToken(GUEST_USER, null, null);
        guestAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(guestAuth);
    }
}
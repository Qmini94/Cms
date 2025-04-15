package kr.co.itid.cms.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AdminContextRequestFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String referer = request.getHeader("Referer");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (referer != null && referer.contains("/admin/")) {
            if (auth == null || !auth.isAuthenticated()) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Admin 요청은 인증이 필요합니다.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}

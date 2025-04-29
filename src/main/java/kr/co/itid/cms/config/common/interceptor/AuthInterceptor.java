package kr.co.itid.cms.config.common.interceptor;

import kr.co.itid.cms.config.security.JwtTokenProvider;
import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
            if (user.isGuest() || user.isDev()) return true;

            // 슬라이딩 세션 처리
            jwtTokenProvider.refreshIfNeeded(user);
        } catch (Exception e) {
            // 슬라이딩 실패해도 auth/me 응답은 막지 않음
        }
        return true;
    }
}

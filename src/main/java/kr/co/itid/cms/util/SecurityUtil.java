package kr.co.itid.cms.util;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    private SecurityUtil() {}

    public static JwtAuthenticatedUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof JwtAuthenticatedUser user)) {
            throw new IllegalStateException("인증 정보가 올바르지 않습니다.");
        }

        return user;
    }
}


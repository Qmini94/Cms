package kr.co.itid.cms.service.auth.impl;

import io.jsonwebtoken.Claims;
import kr.co.itid.cms.config.security.JwtTokenProvider;
import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.service.auth.PermissionResolverService;
import kr.co.itid.cms.service.auth.PermissionService;
import kr.co.itid.cms.util.LoggingUtil;
import kr.co.itid.cms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;

@Service("permService")
@RequiredArgsConstructor
public class PermissionServiceImpl extends EgovAbstractServiceImpl implements PermissionService {

    private final LoggingUtil loggingUtil;
    private final PermissionResolverService permissionResolverService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean hasAccess(long menuId, String permission) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Check access: menuId=" + menuId + ", permission=" + permission);

        try {
            // 1. 토큰 갱신 및 SecurityContext 갱신
            refreshTokenIfRequired();

            // 2. 최신 인증 정보로 사용자 객체 재조회
            JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();

            if (user.isAdmin()) {
                loggingUtil.logSuccess(Action.RETRIEVE, "Admin override access granted for user=" + user.userId());
                return true;
            }

            boolean result = permissionResolverService.resolvePermission(user, menuId, permission);

            if (result) {
                loggingUtil.logSuccess(Action.RETRIEVE, "Permission granted: user=" + user.userId());
            } else {
                loggingUtil.logFail(Action.RETRIEVE, "Permission denied: user=" + user.userId());
            }

            return result;

        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Access check error: " + e.getMessage());
            throw processException("Access check error", e);
        }
    }

    private void refreshTokenIfRequired() {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();

        if (user.isGuest() || user.isDev()) return;

        // 1. accessToken 재발급
        String newToken = jwtTokenProvider.recreateTokenFrom(user.token());
        ResponseCookie cookie = jwtTokenProvider.createAccessTokenCookie(newToken);

        HttpServletResponse response = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getResponse();

        if (response != null) {
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        // 2. claims 파싱 → 최신 JwtAuthenticatedUser 생성
        Claims claims = jwtTokenProvider.getClaimsFromToken(newToken);

        JwtAuthenticatedUser refreshedUser = new JwtAuthenticatedUser(
                claims.get("idx", Long.class),
                claims.getSubject(), // userId
                claims.get("userName", String.class),
                claims.get("userLevel", Integer.class),
                newToken,
                user.hostname() // hostname은 기존 user에서 유지
        );

        // 3. SecurityContext 갱신
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(refreshedUser, null, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
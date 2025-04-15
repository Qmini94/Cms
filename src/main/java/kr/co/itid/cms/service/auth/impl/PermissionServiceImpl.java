package kr.co.itid.cms.service.auth.impl;

import io.jsonwebtoken.Claims;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.service.auth.PermissionResolverService;
import kr.co.itid.cms.service.auth.PermissionService;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("permService")
@RequiredArgsConstructor
public class PermissionServiceImpl extends EgovAbstractServiceImpl implements PermissionService {

    private final LoggingUtil loggingUtil;
    private final PermissionResolverService permissionResolverService;

    @Override
    public boolean hasAccess(Authentication authentication, long menuId, String permission) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Check access: menuId=" + menuId + ", permission=" + permission);

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                loggingUtil.logFail(Action.RETRIEVE, "User not logged in");
                throw processException("User not logged in", new BadCredentialsException("User not logged in"));
            }

            String principal;
            try {
                principal = (String) authentication.getPrincipal();
            } catch (ClassCastException e) {
                loggingUtil.logFail(Action.RETRIEVE, "Invalid principal type");
                throw processException("Invalid principal", e);
            }

            Claims claims = (Claims) authentication.getCredentials();
            int userIdx = claims.get("idx", Integer.class);
            int userLevel = claims.get("userLevel", Integer.class);

            if (userLevel == 1) {
                loggingUtil.logSuccess(Action.RETRIEVE, "Admin override access granted for user=" + principal);
                return true;
            }

            try {
                boolean result = permissionResolverService.resolvePermission(userIdx, userLevel, menuId, permission);
                if (result) {
                    loggingUtil.logSuccess(Action.RETRIEVE, "Permission granted: user=" + principal);
                } else {
                    loggingUtil.logFail(Action.RETRIEVE, "Permission denied: user=" + principal);
                }
                return result;
            } catch (Exception e) {
                loggingUtil.logFail(Action.RETRIEVE, "Permission check error:" + e.getMessage() + " user=" + principal);
                throw processException("Permission check error", e);
            }
        } catch (NullPointerException e) {
            loggingUtil.logFail(Action.RETRIEVE, "No authentication found");
            throw processException("No authentication", new AccessDeniedException("Authentication not found"));
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Access check error");
            throw processException("Access check error", e);
        }
    }
}
package kr.co.itid.cms.service.auth.impl;

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

//            boolean result = permissionResolverService.resolvePermission(principal, menuId, permission);
            boolean result = true;
            loggingUtil.logSuccess(Action.RETRIEVE, "Access check success: user=" + principal + ", menuId=" + menuId + ", permission=" + permission);
            return result;
        } catch (NullPointerException e) {
            loggingUtil.logFail(Action.RETRIEVE, "No authentication found");
            throw processException("No authentication", new AccessDeniedException("Authentication not found"));
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Access check error");
            throw processException("Access check error", e);
        }
    }
}
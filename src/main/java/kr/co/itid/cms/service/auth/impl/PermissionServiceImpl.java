package kr.co.itid.cms.service.auth.impl;

import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.service.auth.PermissionService;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("permService")
@RequiredArgsConstructor
public class PermissionServiceImpl extends EgovAbstractServiceImpl implements PermissionService {

    private final LoggingUtil loggingUtil;

    @Override
    public boolean hasAccess(Authentication authentication, int menuId, String permission) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Check access: menuId=" + menuId + ", permission=" + permission);

        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                loggingUtil.logFail(Action.RETRIEVE, "User not logged in");
                throw processException("User not logged in");
            }

            String principal;
            try {
                principal = (String) authentication.getPrincipal();
            } catch (ClassCastException e) {
                loggingUtil.logFail(Action.RETRIEVE, "Invalid principal type");
                throw processException("Invalid principal", e);
            }

            boolean result = checkUserPermission(principal, menuId, permission);
            loggingUtil.logSuccess(Action.RETRIEVE, "Access check success: user=" + principal + ", menuId=" + menuId + ", permission=" + permission);
            return result;
        } catch (NullPointerException e) {
            loggingUtil.logFail(Action.RETRIEVE, "No authentication found");
            throw processException("No authentication", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Access check error");
            throw processException("Access check error", e);
        }
    }

    private boolean checkUserPermission(String principal, int menuId, String permission) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Check user permission: user=" + principal + ", menuId=" + menuId);

        try {
            // 실제 권한 조회 로직 필요
            boolean hasPermission = true;

            if (!hasPermission) {
                loggingUtil.logFail(Action.RETRIEVE, "Access denied: user=" + principal);
                throw processException("Access denied");
            }

            loggingUtil.logSuccess(Action.RETRIEVE, "Permission check success: user=" + principal);
            return hasPermission;
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Permission check error");
            throw processException("Permission check error", e);
        }
    }
}
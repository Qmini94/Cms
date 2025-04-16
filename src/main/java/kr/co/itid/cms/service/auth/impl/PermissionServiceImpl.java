package kr.co.itid.cms.service.auth.impl;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.service.auth.PermissionResolverService;
import kr.co.itid.cms.service.auth.PermissionService;
import kr.co.itid.cms.util.LoggingUtil;
import kr.co.itid.cms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;

@Service("permService")
@RequiredArgsConstructor
public class PermissionServiceImpl extends EgovAbstractServiceImpl implements PermissionService {

    private final LoggingUtil loggingUtil;
    private final PermissionResolverService permissionResolverService;

    @Override
    public boolean hasAccess(long menuId, String permission) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Check access: menuId=" + menuId + ", permission=" + permission);

        try {
            JwtAuthenticatedUser user;
            try {
                user = SecurityUtil.getCurrentUser();
            } catch (Exception e) {
                loggingUtil.logFail(Action.RETRIEVE, "User not logged in or invalid principal");
                throw processException("User not logged in", e);
            }

            if (user.isAdmin()) {
                loggingUtil.logSuccess(Action.RETRIEVE, "Admin override access granted for user=" + user.userId());
                return true;
            }

            try {
                boolean result = permissionResolverService.resolvePermission(user, menuId, permission);

                if (result) {
                    loggingUtil.logSuccess(Action.RETRIEVE, "Permission granted: user=" + user.userId());
                } else {
                    loggingUtil.logFail(Action.RETRIEVE, "Permission denied: user=" + user.userId());
                }

                return result;
            } catch (Exception e) {
                loggingUtil.logFail(Action.RETRIEVE, "Permission check error: " + e.getMessage() + " user=" + user.userId());
                throw processException("Permission check error", e);
            }

        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Access check error");
            throw processException("Access check error", e);
        }
    }
}
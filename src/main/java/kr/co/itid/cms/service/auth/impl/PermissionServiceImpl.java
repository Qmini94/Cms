package kr.co.itid.cms.service.auth.impl;

import kr.co.itid.cms.config.security.JwtTokenProvider;
import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.dto.auth.UserPermissionResponse;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.service.auth.PermissionResolverService;
import kr.co.itid.cms.service.auth.PermissionService;
import kr.co.itid.cms.service.auth.model.PermissionEntry;
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
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean hasAccess(String permission) throws Exception {
        try {
            // 1. 현재 사용자 가져오기
            JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();

            // 2. 토큰 갱신 및 SecurityContext 갱신
            jwtTokenProvider.refreshIfNeeded(user);

            // 3. (갱신 후) 최신 인증 정보로 사용자 객체 재조회
            user = SecurityUtil.getCurrentUser();
            loggingUtil.logAttempt(Action.RETRIEVE, "Check access: menuId=" + user.menuId() + ", permission=" + permission);

            if (user.isAdmin()) {
                loggingUtil.logSuccess(Action.RETRIEVE, "Admin override access granted for user=" + user.userId());
                return true;
            }

            boolean result = permissionResolverService.hasPermission(user, permission);

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

    @Override
    public UserPermissionResponse getPermissionByMenu(JwtAuthenticatedUser user) throws Exception {
        Long menuId = user.menuId();

        loggingUtil.logAttempt(Action.RETRIEVE, "Try to resolve permissions: user=" + user.userId() + ", menuId=" + menuId);

        try {
            if (user.userLevel() == 1) {
                loggingUtil.logSuccess(Action.RETRIEVE, "Admin full permission granted: user=" + user.userId());
                return UserPermissionResponse.builder()
                        .view(true).write(true).modify(true).remove(true)
                        .build();
            }

            PermissionEntry entry = permissionResolverService.resolvePermissions(user);

            loggingUtil.logSuccess(Action.RETRIEVE, "Resolved permissions for user=" + user.userId());

            return UserPermissionResponse.builder()
                    .view(entry.getPermissions().contains("VIEW"))
                    .write(entry.getPermissions().contains("WRITE"))
                    .modify(entry.getPermissions().contains("MODIFY"))
                    .remove(entry.getPermissions().contains("REMOVE"))
                    .build();

        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Permission resolution failed: " + e.getMessage());
            throw processException("Failed to resolve permissions", e);
        }
    }
}
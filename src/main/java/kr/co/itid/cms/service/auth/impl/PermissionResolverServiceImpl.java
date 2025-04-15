package kr.co.itid.cms.service.auth.impl;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.entity.cms.base.Menu;
import kr.co.itid.cms.entity.cms.base.Permission;
import kr.co.itid.cms.repository.cms.MenuRepository;
import kr.co.itid.cms.repository.cms.PermissionRepository;
import kr.co.itid.cms.service.auth.PermissionResolverService;
import kr.co.itid.cms.service.auth.model.MenuPermissionData;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service("permissionResolverService")
@RequiredArgsConstructor
public class PermissionResolverServiceImpl extends EgovAbstractServiceImpl implements PermissionResolverService {

    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final RedisTemplate<String, MenuPermissionData> redisTemplate;

    private static final String PERMISSION_KEY_PREFIX = "perm:menu:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    @Override
    public boolean resolvePermission(JwtAuthenticatedUser user, long menuId, String permission) throws Exception {
        String redisKey = getCacheKey(menuId);
        MenuPermissionData cached;

        try {
            cached = redisTemplate.opsForValue().get(redisKey);
        } catch (Exception e) {
            throw processException("Fail to read permission cache", e);
        }

        if (cached == null) {
            try {
                cached = buildMenuPermission(menuId);
                redisTemplate.opsForValue().set(redisKey, cached, CACHE_TTL);
            } catch (Exception e) {
                throw processException("Fail to build permission cache", e);
            }
        }else{
            redisTemplate.expire(redisKey, CACHE_TTL);
        }

        return cached.hasPermission(user.userIdx(), user.userLevel(), permission);
    }

    private MenuPermissionData buildMenuPermission(long menuId) throws Exception {
        MenuPermissionData permissionData = new MenuPermissionData();
        permissionData.setMenuId(menuId);
        permissionData.setLastUpdate(new Date());
        permissionData.setVersion(System.currentTimeMillis()); // 버전 관리용

        List<Integer> menuHierarchy;
        try {
            menuHierarchy = findMenuHierarchy(menuId);
        } catch (Exception e) {
            throw processException("Cannot find menu path", e);
        }

        if (menuHierarchy.isEmpty()) {
            throw processException("Menu path is empty");
        }

        List<Permission> allPermissions;
        try {
            allPermissions = permissionRepository.findAllByMenuIds(menuHierarchy);
        } catch (Exception e) {
            throw processException("Fail to get permission list", e);
        }

        for (Permission perm : allPermissions) {
            List<String> allowed = extractAllowedPermissions(perm);
            int sort = perm.getSort() != null ? perm.getSort() : 9999;
            Set<String> permissionSet = new HashSet<>(allowed);

            try {
                if ("id".equalsIgnoreCase(perm.getType())) {
                    int idx = Integer.parseInt(
                            Optional.ofNullable(perm.getValue())
                                    .orElseThrow(() -> new IllegalArgumentException("Login idx is null"))
                    );
                    permissionData.addPermissionEntry(sort, idx, null, permissionSet);
                } else if ("login".equalsIgnoreCase(perm.getType())) {
                    int level = Integer.parseInt(
                            Optional.ofNullable(perm.getValue())
                                    .orElseThrow(() -> new IllegalArgumentException("Login level is null"))
                    );
                    permissionData.addPermissionEntry(sort, -1, level, permissionSet);
                }
            } catch (Exception e) {
                throw processException("Fail to parse permission data", e);
            }
        }

        return permissionData;
    }

    private List<Integer> findMenuHierarchy(long menuId) throws Exception {
        String path;
        try {
            path = menuRepository.findById(menuId)
                    .map(Menu::getPathId)
                    .orElseThrow(() -> new NoSuchElementException("Menu not found"));
        } catch (Exception e) {
            throw processException("Fail to get menu path", e);
        }

        List<Integer> hierarchy = new ArrayList<>();
        try {
            for (String id : path.split("\\.")) {
                hierarchy.add(Integer.parseInt(id));
            }
        } catch (NumberFormatException e) {
            throw processException("Menu path format error", e);
        }

        return hierarchy;
    }

    private List<String> extractAllowedPermissions(Permission perm) {
        List<String> permissions = new ArrayList<>();

        if ("y".equalsIgnoreCase(perm.getView())) permissions.add("VIEW");
        if ("y".equalsIgnoreCase(perm.getWrite())) permissions.add("WRITE");
        if ("y".equalsIgnoreCase(perm.getModify())) permissions.add("MODIFY");
        if ("y".equalsIgnoreCase(perm.getRemove())) permissions.add("REMOVE");
        if ("y".equalsIgnoreCase(perm.getManage())) permissions.add("MANAGE");
        if ("y".equalsIgnoreCase(perm.getAccess())) permissions.add("ACCESS");
        if ("y".equalsIgnoreCase(perm.getReply())) permissions.add("REPLY");
        if ("y".equalsIgnoreCase(perm.getAdmin())) permissions.add("ADMIN");

        return permissions;
    }

    private String getCacheKey(long menuId) {
        return PERMISSION_KEY_PREFIX + menuId;
    }

    /**
     * 특정 메뉴 ID의 권한 캐시를 무효화합니다.
     * TODO: 삭제할때 싱위, 하위 ID들 캐쉬 같이 삭제해야함.
     * @param menuId 삭제할 메뉴 ID
     */
    public void invalidateMenuPermission(long menuId) {
        try {
            redisTemplate.delete(getCacheKey(menuId));
        } catch (Exception e) {
            throw new RuntimeException("Fail to invalidate menu permission cache", e);
        }
    }
}
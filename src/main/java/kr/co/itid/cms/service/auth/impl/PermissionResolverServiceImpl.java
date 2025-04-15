package kr.co.itid.cms.service.auth.impl;

import kr.co.itid.cms.entity.cms.base.Menu;
import kr.co.itid.cms.entity.cms.base.Permission;
import kr.co.itid.cms.repository.cms.MenuRepository;
import kr.co.itid.cms.repository.cms.PermissionRepository;
import kr.co.itid.cms.service.auth.PermissionResolverService;
import kr.co.itid.cms.service.auth.model.MenuPermissionData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service("permissionResolverService")
@RequiredArgsConstructor
public class PermissionResolverServiceImpl implements PermissionResolverService {
    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final RedisTemplate<String, MenuPermissionData> redisTemplate;

    @Override
    public boolean resolvePermission(String userId, long menuId, String permission) throws Exception {
        MenuPermissionData cached = redisTemplate.opsForValue().get("perm:menu:" + menuId);

        if (cached == null) {
            cached = buildMenuPermission(menuId);
            redisTemplate.opsForValue().set("perm:menu:" + menuId, cached, Duration.ofHours(1));
        }

        int level = fetchUserLevel(); // 사용자 레벨을 조회하는 메서드 필요
        return cached.hasPermission(userId, level, permission);
    }

    private MenuPermissionData buildMenuPermission(long menuId) throws Exception {
        MenuPermissionData permissionData = new MenuPermissionData();
        permissionData.setMenuId(menuId);
        permissionData.setLastUpdate(new Date());

        // 1. 상위 메뉴 포함한 트리 라인업 조회
        List<Integer> menuHierarchy = findMenuHierarchy(menuId);

        // 2. 해당 메뉴 + 상위 메뉴들의 권한을 전부 조회
        List<Permission> allPermissions = permissionRepository.findAllByMenuIds(menuHierarchy);

        for (Permission perm : allPermissions) {
            List<String> allowed = extractAllowedPermissions(perm);
            int sort = perm.getSort() != null ? perm.getSort() : 9999; // 정렬 기준값

            Set<String> permissionSet = new HashSet<>(allowed);

            if ("id".equalsIgnoreCase(perm.getType())) {
                permissionData.addPermissionEntry(sort, perm.getValue(), null, permissionSet);
            } else if ("login".equalsIgnoreCase(perm.getType())) {
                int level = Integer.parseInt(perm.getValue());
                permissionData.addPermissionEntry(sort, null, level, permissionSet);
            }
        }

        return permissionData;
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

    private List<Integer> findMenuHierarchy(long menuId) {
        List<Integer> hierarchy = new ArrayList<>();
        String path = menuRepository.findById(menuId)
                .map(Menu::getPathId)
                .orElse(null);

        if (path != null && !path.isEmpty()) {
            String[] ids = path.split("\\.");
            for (String id : ids) {
                try {
                    hierarchy.add(Integer.parseInt(id));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return hierarchy;
    }

    private int fetchUserLevel() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getCredentials() instanceof Map) {
            Map<String, Object> claims = (Map<String, Object>) authentication.getCredentials();
            Object userLevelObj = claims.get("userLevel");

            if (userLevelObj instanceof Integer) {
                return (Integer) userLevelObj;
            } else if (userLevelObj instanceof String) {
                return Integer.parseInt((String) userLevelObj);
            }
        }

        throw new IllegalStateException("SecurityContext에 사용자 권한 정보가 없습니다.");
    }
}
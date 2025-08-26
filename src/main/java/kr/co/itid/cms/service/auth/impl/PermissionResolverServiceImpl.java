package kr.co.itid.cms.service.auth.impl;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.entity.cms.core.menu.Menu;
import kr.co.itid.cms.entity.cms.core.permission.Permission;
import kr.co.itid.cms.repository.cms.core.menu.MenuRepository;
import kr.co.itid.cms.repository.cms.core.permission.PermissionRepository;
import kr.co.itid.cms.service.auth.PermissionResolverService;
import kr.co.itid.cms.service.auth.model.MenuPermissionData;
import kr.co.itid.cms.service.auth.model.PermissionEntry;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static kr.co.itid.cms.constanrt.PermissionConstants.*;
import static kr.co.itid.cms.constanrt.RedisConstants.PERMISSION_KEY_PREFIX;
import static kr.co.itid.cms.constanrt.RedisConstants.PERMISSION_TTL;

@Service("permissionResolverService")
@RequiredArgsConstructor
public class PermissionResolverServiceImpl extends EgovAbstractServiceImpl implements PermissionResolverService {

    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;
    private final RedisTemplate<String, MenuPermissionData> redisTemplate;

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public boolean hasPermission(JwtAuthenticatedUser user, String permission) throws Exception {
        Long menuId = user.menuId();
        MenuPermissionData permissionData = getOrBuildPermissionData(menuId);

        return permissionData.hasPermission(user.userIdx(), user.userLevel(), permission);
    }

    @Override
    public PermissionEntry resolvePermissions(JwtAuthenticatedUser user) throws Exception {
        Long menuId = user.menuId();
        MenuPermissionData permissionData = getOrBuildPermissionData(menuId);

        return permissionData.getEffectivePermissionEntryForUser(user);
    }

    private MenuPermissionData getOrBuildPermissionData(Long menuId) throws Exception {
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
                redisTemplate.opsForValue().set(redisKey, cached, PERMISSION_TTL);
            } catch (Exception e) {
                throw processException("Fail to build permission cache", e);
            }
        } else {
            redisTemplate.expire(redisKey, PERMISSION_TTL);
        }

        return cached;
    }

    private MenuPermissionData buildMenuPermission(Long menuId) throws Exception {
        MenuPermissionData permissionData = new MenuPermissionData();
        permissionData.setMenuId(menuId);
        permissionData.setLastUpdate(new Date());

        // 1) 경로 조회
        final List<Long> menuHierarchy;
        try {
            menuHierarchy = findMenuHierarchy(menuId); // 보통 [root ... current]
        } catch (Exception e) {
            throw processException("Cannot find menu path", e);
        }
        if (menuHierarchy.isEmpty()) {
            throw processException("Menu path is empty");
        }

        // 2) menuId -> distance 맵 (현재=0, 부모=1, ...)
        final int n = menuHierarchy.size();
        final Map<Long, Integer> distanceByMenuId = new HashMap<>(n * 2);
        // [root..current] 가정: current가 맨 뒤
        for (int i = 0; i < n; i++) {
            distanceByMenuId.put(menuHierarchy.get(i), (n - 1) - i);
        }

        // 3) 권한 전체 조회
        final List<Permission> allPermissions;
        try {
            allPermissions = permissionRepository.findAllByMenuIds(menuHierarchy);
        } catch (Exception e) {
            throw processException("Fail to get permission list", e);
        }

        // 4) 거리별 그룹핑
        Map<Integer, List<Permission>> byDistance = new TreeMap<>(); // 0,1,2 ... 오름차순
        for (Permission p : allPermissions) {
            int d = distanceByMenuId.getOrDefault(p.getMenuId(), Integer.MAX_VALUE);
            byDistance.computeIfAbsent(d, k -> new ArrayList<>()).add(p);
        }

        // 같은 거리 안 정렬: sort -> pk
        Comparator<Permission> inGroup = Comparator
                .comparing((Permission p) -> Optional.ofNullable(p.getSort()).orElse(9999))
                .thenComparing(p -> Optional.ofNullable(p.getIdx()).orElse(Long.MAX_VALUE));

        // 5) 최종 삽입 순서: 거리(가까운→먼) 안에서 user(id) 먼저, 그다음 level
        int order = 0; // 0부터 1씩 증가
        for (Map.Entry<Integer, List<Permission>> bucket : byDistance.entrySet()) {
            List<Permission> perms = bucket.getValue();

            List<Permission> userPerms = perms.stream()
                    .filter(p -> "id".equalsIgnoreCase(p.getType()))
                    .sorted(inGroup)
                    .collect(Collectors.toList());

            List<Permission> levelPerms = perms.stream()
                    .filter(p -> "level".equalsIgnoreCase(p.getType()))
                    .sorted(inGroup)
                    .collect(Collectors.toList());

            // user(idx) 먼저
            for (Permission perm : userPerms) {
                final Set<String> permissionSet = new HashSet<>(extractAllowedPermissions(perm));
                final Long idx = Long.parseLong(
                        Optional.ofNullable(perm.getValue())
                                .orElseThrow(() -> new IllegalArgumentException("Login idx is null"))
                );
                permissionData.addPermissionEntry(order++, idx, null, permissionSet);
            }
            // level 다음
            for (Permission perm : levelPerms) {
                final Set<String> permissionSet = new HashSet<>(extractAllowedPermissions(perm));
                final Integer level = Integer.parseInt(
                        Optional.ofNullable(perm.getValue())
                                .orElseThrow(() -> new IllegalArgumentException("Login level is null"))
                );
                permissionData.addPermissionEntry(order++, null, level, permissionSet);
            }
        }

        return permissionData;
    }

    private List<Long> findMenuHierarchy(Long menuId) throws Exception {
        String path;
        try {
            path = menuRepository.findById(menuId)
                    .map(Menu::getPathId)
                    .orElseThrow(() -> new NoSuchElementException("Menu not found"));
        } catch (Exception e) {
            throw processException("Fail to get menu path", e);
        }

        List<Long> hierarchy = new ArrayList<>();
        try {
            for (String id : path.split("\\.")) {
                hierarchy.add(Long.parseLong(id));
            }
        } catch (NumberFormatException e) {
            throw processException("Menu path format error", e);
        }

        return hierarchy;
    }

    private List<String> extractAllowedPermissions(Permission perm) {
        List<String> permissions = new ArrayList<>();

        if ("y".equalsIgnoreCase(perm.getView())) permissions.add(VIEW);
        if ("y".equalsIgnoreCase(perm.getWrite())) permissions.add(WRITE);
        if ("y".equalsIgnoreCase(perm.getModify())) permissions.add(MODIFY);
        if ("y".equalsIgnoreCase(perm.getRemove())) permissions.add(REMOVE);
        if ("y".equalsIgnoreCase(perm.getManage())) permissions.add(MANAGE);
        if ("y".equalsIgnoreCase(perm.getAccess())) permissions.add(ACCESS);
        if ("y".equalsIgnoreCase(perm.getReply())) permissions.add(REPLY);
        if ("y".equalsIgnoreCase(perm.getAdmin())) permissions.add(ADMIN);

        return permissions;
    }

    private String getCacheKey(Long menuId) {
        return PERMISSION_KEY_PREFIX + menuId;
    }
}
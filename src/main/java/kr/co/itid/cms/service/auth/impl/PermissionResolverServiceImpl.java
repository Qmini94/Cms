package kr.co.itid.cms.service.auth.impl;

import kr.co.itid.cms.repository.cms.MenuRepository;
import kr.co.itid.cms.service.auth.PermissionResolverService;
import kr.co.itid.cms.service.auth.model.MenuPermissionData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service("permissionResolverService")
@RequiredArgsConstructor
public class PermissionResolverServiceImpl implements PermissionResolverService {
    private final MenuRepository menuRepository;

    @Override
    public boolean resolvePermission(String userId, int menuId, String permission) throws Exception {
        return false;
    }

//    private MenuPermissionData buildMenuPermission(int menuId) throws Exception {
//        MenuPermissionData permissionData = new MenuPermissionData();
//        permissionData.setMenuId(menuId);
//        permissionData.setLastUpdate(new Date());
//
//        // 1. 상위 메뉴 포함한 트리 라인업 조회
//        List<Integer> menuHierarchy = findMenuHierarchy(menuId); // [1, 23, 78]
//
//        // 2. 해당 메뉴 + 상위 메뉴들의 권한을 전부 조회
//        List<PermissionEntity> allPermissions = permissionRepository.findAllByMenuIds(menuHierarchy);
//
//        for (PermissionEntity perm : allPermissions) {
//            String[] allowed = perm.getPermission().split(","); // 예: "VIEW,WRITE"
//
//            for (String p : allowed) {
//                if ("LEVEL".equals(perm.getType())) {
//                    int level = Integer.parseInt(perm.getValue());
//                    permissionData.getLevelPermissions()
//                            .computeIfAbsent(level, k -> new HashSet<>())
//                            .add(p);
//                } else if ("USER".equals(perm.getType())) {
//                    String userId = perm.getValue();
//                    permissionData.getUserPermissions()
//                            .computeIfAbsent(userId, k -> new HashSet<>())
//                            .add(p);
//                }
//            }
//        }
//
//        return permissionData;
//    }
//
//    private List<Integer> findMenuHierarchy(int menuId) {
//        List<Integer> hierarchy = new ArrayList<>();
//        Integer current = menuId;
//
//        while (current != null) {
//            hierarchy.add(current);
//            current = menuRepository.findParentIdById(current); // 메뉴 테이블에서 parent_id 조회
//        }
//
//        Collections.reverse(hierarchy); // 상위 → 하위 순으로 정렬
//        return hierarchy;
//    }

}

package kr.co.itid.cms.service.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuPermissionData implements Serializable {

    private long menuId;

    private Date lastUpdate; // 캐시 마지막 갱신 시간

    // 정렬 기준 권한 리스트: sort 값이 키, 해당 메뉴에서 부여된 권한 리스트 (userId, userLevel 기준 함께 보관)
    private Map<Integer, List<PermissionEntry>> sortedPermissionMap = new TreeMap<>();

    /**
     * 권한 확인 메서드: 상위 메뉴부터 순차적으로 우선순위(sort) 기준 체크
     * @param userId 사용자 ID
     * @param level 사용자 등급
     * @param targetPermission 확인할 권한
     * @return 보유 여부
     */
    public boolean hasPermission(String userId, int level, String targetPermission) {
        boolean levelAllowed = false;

        for (Map.Entry<Integer, List<PermissionEntry>> entry : sortedPermissionMap.entrySet()) {
            for (PermissionEntry permissionEntry : entry.getValue()) {
                // 1. userId는 항상 우선
                if (permissionEntry.getUserId() != null && permissionEntry.getUserId().equals(userId)) {
                    if (permissionEntry.getPermissions().contains(targetPermission)) {
                        return true;
                    }
                }

                // 2. userLevel은 나중에 적용할 수 있도록 상태로 보관
                if (permissionEntry.getLevel() != null && permissionEntry.getLevel() == level) {
                    levelAllowed = permissionEntry.getPermissions().contains(targetPermission);
                }
            }
        }

        return levelAllowed;
    }

    // 권한 등록
    public void addPermissionEntry(int sort, String userId, Integer level, Set<String> permissions) {
        sortedPermissionMap.computeIfAbsent(sort, k -> new ArrayList<>())
                .add(new PermissionEntry(userId, level, permissions));
    }
}
package kr.co.itid.cms.service.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

/**
 * 메뉴별 권한 캐시 데이터 구조
 * Redis에 저장되는 단위이며, 메뉴 ID 기준으로 캐싱됨
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuPermissionData implements Serializable {

    private long menuId;

    private Date lastUpdate; // 캐시 마지막 갱신 시점

    /**
     * 정렬 기준에 따른 권한 목록 (sort -> [PermissionEntry...])
     * 상속된 상위 메뉴 권한이 먼저 평가되도록 TreeMap 사용
     */
    private Map<Integer, List<PermissionEntry>> sortedPermissionMap = new TreeMap<>();

    /**
     * 사용자 권한 확인 메서드
     * 1. userId가 일치하면 즉시 true
     * 2. 등급(level)은 순차 탐색 후 마지막 일치 상태를 기록
     *
     * @param userIdx 사용자 IDX
     * @param level 사용자 등급
     * @param targetPermission 확인할 권한
     * @return 권한이 있을 경우 true
     */
    public boolean hasPermission(long userIdx, int level, String targetPermission) {
        boolean levelAllowed = false;

        for (Map.Entry<Integer, List<PermissionEntry>> entry : sortedPermissionMap.entrySet()) {
            for (PermissionEntry permissionEntry : entry.getValue()) {
                if (permissionEntry.getUserIdx() != null &&
                        permissionEntry.getUserIdx().equals(userIdx)) {
                    if (permissionEntry.getPermissions().contains(targetPermission)) {
                        return true;
                    }
                }

                if (permissionEntry.getLevel() != null && permissionEntry.getLevel() == level) {
                    levelAllowed = permissionEntry.getPermissions().contains(targetPermission);
                }
            }
        }

        return levelAllowed;
    }

    /**
     * 정렬 순서에 따라 권한 엔트리를 추가
     */
    public void addPermissionEntry(int sort, long userIdx, Integer level, Set<String> permissions) {
        sortedPermissionMap.computeIfAbsent(sort, k -> new ArrayList<>())
                .add(new PermissionEntry(userIdx, level, permissions));
    }
}
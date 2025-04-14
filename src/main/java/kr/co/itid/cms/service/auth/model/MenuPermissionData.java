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

    private int menuId;

    // menuId → userLevel 기준 권한: 예) 1 → ["VIEW", "WRITE"]
    private Map<Integer, Set<String>> levelPermissions = new HashMap<>();

    // menuId → userId 기준 권한: 예) "user_1001" → ["VIEW", "REMOVE", "MANAGE"]
    private Map<String, Set<String>> userPermissions = new HashMap<>();

    private Date lastUpdate; // 캐시 마지막 갱신 시간

    // 메뉴별로 정렬 우선순위 (sort) 보관: 상속 우선순위 판단에 사용
    private Map<Integer, Integer> menuSortPriority = new HashMap<>();

    /**
     * 권한 확인 메서드 (메뉴 상속 우선순위 포함)
     *
     * @param userId           사용자 ID
     * @param level            사용자 등급
     * @param targetPermission 확인할 권한 (예: "VIEW")
     * @return 권한 보유 여부
     */
    public boolean hasPermission(String userId, int level, String targetPermission) {
        // 1. 우선순위: userPermissions > levelPermissions
        // 단, 같은 권한이 여러 메뉴 계층에 존재할 경우, sort 값이 낮은(menuSortPriority 기준) 메뉴가 우선

        Set<String> finalPermissions = new HashSet<>();
        int lowestSort = Integer.MAX_VALUE;

        for (Map.Entry<String, Set<String>> entry : userPermissions.entrySet()) {
            String key = entry.getKey();
            if (key.equals(userId)) {
                int sort = menuSortPriority.getOrDefault(menuId, Integer.MAX_VALUE);
                if (sort < lowestSort && entry.getValue().contains(targetPermission)) {
                    finalPermissions = entry.getValue();
                    lowestSort = sort;
                }
            }
        }

        for (Map.Entry<Integer, Set<String>> entry : levelPermissions.entrySet()) {
            if (entry.getKey() == level) {
                int sort = menuSortPriority.getOrDefault(menuId, Integer.MAX_VALUE);
                if (sort < lowestSort && entry.getValue().contains(targetPermission)) {
                    finalPermissions = entry.getValue();
                    lowestSort = sort;
                }
            }
        }

        return finalPermissions.contains(targetPermission);
    }

    // menuId별 sort 값 등록
    public void registerSort(int menuId, int sort) {
        menuSortPriority.put(menuId, sort);
    }
}
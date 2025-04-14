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

    // userLevel 기준 권한: 예) 1 → ["VIEW", "WRITE"]
    private Map<Integer, Set<String>> levelPermissions = new HashMap<>();

    // userId 기준 권한: 예) "user_1001" → ["VIEW", "REMOVE", "MANAGE"]
    private Map<String, Set<String>> userPermissions = new HashMap<>();

    private Date lastUpdate; // 캐시 마지막 갱신 시간

    /**
     * 권한 확인 메서드
     * @param userId 사용자 ID
     * @param level 사용자 등급
     * @param targetPermission 확인할 권한 (예: "VIEW")
     * @return 권한 보유 여부
     */
    public boolean hasPermission(String userId, int level, String targetPermission) {
        // 1. userId로 우선 체크 (우선순위 높음)
        if (userPermissions.containsKey(userId)) {
            return userPermissions.get(userId).contains(targetPermission);
        }

        // 2. userLevel 기준 권한 체크
        if (levelPermissions.containsKey(level)) {
            return levelPermissions.get(level).contains(targetPermission);
        }

        // 3. 둘 다 없으면 false
        return false;
    }
}

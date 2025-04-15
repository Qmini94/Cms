package kr.co.itid.cms.service.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * 개별 권한 엔트리
 * 사용자 ID 또는 레벨에 대해 권한 목록을 부여
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionEntry implements Serializable {
    private Integer userIdx;           // 사용자 ID (우선순위 높음)
    private Integer level;           // 사용자 등급 (null이면 무시)
    private Set<String> permissions; // 부여된 권한 집합
}

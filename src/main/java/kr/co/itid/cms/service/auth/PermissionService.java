package kr.co.itid.cms.service.auth;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.dto.auth.UserPermissionResponse;

/**
 * 사용자 권한 처리 서비스
 */
public interface PermissionService {

    /**
     * 현재 로그인한 사용자가 특정 권한(permission)을 가지고 있는지 확인합니다.
     *
     * 슬라이딩 토큰 방식이 적용되어 있으며,
     * 인증된 사용자의 경우 조건에 따라 액세스 토큰 재발급이 이루어질 수 있습니다.
     *
     * @param permission 확인할 권한 유형 (예: VIEW, WRITE, MODIFY, REMOVE, MANAGE 등)
     * @return 권한이 있을 경우 true, 없으면 false
     * @throws Exception 예외 발생 시
     */
    boolean hasAccess(String permission) throws Exception;

    /**
     * 현재 로그인한 사용자가 접근 중인 메뉴에 대해 갖고 있는 모든 권한 정보를 반환합니다.
     *
     * @param user 인증된 사용자 정보
     * @return 사용자 권한 정보 DTO
     */
    UserPermissionResponse getPermissionByMenu(JwtAuthenticatedUser user) throws Exception;
}

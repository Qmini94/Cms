package kr.co.itid.cms.service.auth;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;

/**
 * PermissionResolverService 인터페이스
 * 사용자 및 권한 정보를 기반으로 실제 메뉴 접근 권한 여부를 판단하는 기능을 정의합니다.
 */
public interface PermissionResolverService {

    /**
     * 특정 사용자에 대한 메뉴 권한 여부를 확인합니다.
     *
     * @param user 사용자 인증 객체 (JwtAuthenticatedUser)
     * @param menuId 메뉴 식별자
     * @param permission 권한 유형 (VIEW, WRITE, MODIFY, REMOVE, MANAGE, ACCESS, REPLY, ADMIN)
     * @return 권한이 있을 경우 true, 없을 경우 false
     * @throws Exception 권한 조회 또는 처리 중 예외 발생 시
     */
    boolean resolvePermission(JwtAuthenticatedUser user, long menuId, String permission) throws Exception;
}
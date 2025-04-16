package kr.co.itid.cms.service.auth;

/**
 * PermissionService 인터페이스
 * 사용자 권한 관련 서비스 메서드를 정의합니다.
 */
public interface PermissionService {
    /**
     * 권한 검증 메소드
     *
     * @param menuId 메뉴 식별자
     * @param permission 권한 유형 (VIEW, WRITE, MODIFY, REMOVE, MANAGE, ACCESS, REPLY, ADMIN)
     * @return 권한이 있는 경우 true, 없는 경우 false
     */
    boolean hasAccess(long menuId, String permission) throws Exception;
}

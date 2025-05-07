package kr.co.itid.cms.service.auth;

/**
 * PermissionService 인터페이스
 * 사용자 권한 관련 서비스 메서드를 정의합니다.
 */
public interface PermissionService {

    /**
     * 특정 메뉴에 대해 사용자가 지정된 권한을 가지고 있는지 검증합니다.
     *
     * 로그인된 사용자의 권한을 확인하며, 슬라이딩 토큰 방식이 적용되어
     * 인증된 사용자일 경우 액세스 토큰을 조건에 따라 재발급할 수 있습니다.
     *
     * @param permission 검증할 권한 유형 (예: VIEW, WRITE, MODIFY, REMOVE, MANAGE, ACCESS, REPLY, ADMIN)
     * @return 권한이 있는 경우 true, 그렇지 않으면 false
     * @throws Exception 권한 조회 또는 토큰 재발급 과정에서의 예외
     */
    boolean hasAccess(String permission) throws Exception;
}
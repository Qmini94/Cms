package kr.co.itid.cms.service.auth;

import org.springframework.security.core.Authentication;

/**
 * PermissionService 인터페이스
 * 사용자 권한 관련 서비스 메서드를 정의합니다.
 */
public interface PermissionService {
    /**
     * 권한 검증 메소드
     *
     * @param authentication 현재 인증 정보
     * @param menuId 메뉴 식별자
     * @param permission 권한 유형 (READ, WRITE, MODIFY, DELETE)
     * @return 권한이 있는 경우 true, 없는 경우 false
     */
    boolean hasAccess(Authentication authentication, int menuId, String permission) throws Exception;
}

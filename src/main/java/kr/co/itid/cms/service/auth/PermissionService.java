package kr.co.itid.cms.service.auth;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.dto.auth.UserPermissionResponse;
import kr.co.itid.cms.dto.auth.permission.PermissionEntryDto;
import kr.co.itid.cms.dto.auth.permission.response.PermissionChainResponse;

import java.util.List;

/**
 * 사용자 권한 처리 + 권한 관리(체인 조회/업서트) 서비스 퍼사드
 */
public interface PermissionService {

    /**
     * 현재 로그인한 사용자가 특정 권한(permission)을 가지고 있는지 확인합니다.
     *
     * 슬라이딩 토큰 방식이 적용되어 있으며,
     * 인증된 사용자의 경우 조건에 따라 액세스 토큰 재발급이 이루어질 수 있습니다.
     *
     * @param permission 확인할 권한 유형 (예: VIEW, WRITE, MODIFY, REMOVE, MANAGE, ACCESS, REPLY, ADMIN)
     * @return 권한이 있을 경우 true, 없으면 false
     * @throws Exception 예외 발생 시
     */
    boolean hasAccess(String permission) throws Exception;

    /**
     * 현재 로그인한 사용자가 특정 권한(permission)을 가지고 있는지 확인합니다(게시글 단위 검사 포함).
     *
     * @param permission 확인할 권한 유형 (예: VIEW, WRITE, MODIFY, REMOVE, MANAGE, ACCESS, REPLY, ADMIN)
     * @param postId     소유자 검사를 수행할 게시글 식별자 (null이면 소유자 검사 생략)
     * @return 권한이 있을 경우 true, 없으면 false
     * @throws Exception 예외 발생 시
     */
    boolean hasAccess(String permission, Long postId) throws Exception;

    /**
     * 현재 로그인한 사용자가 접근 중인 메뉴에 대해 갖고 있는 모든 권한 정보를 반환합니다.
     *
     * @param user 인증된 사용자 정보
     * @return 사용자 권한 정보 DTO
     * @throws Exception 예외 발생 시
     */
    UserPermissionResponse getPermissionByMenu(JwtAuthenticatedUser user) throws Exception;

    // ====== 권한 관리(편집) 기능 ======

    /**
     * 특정 메뉴의 권한 체인(현재 + 상속)을 조회합니다.
     *
     * @param menuId 메뉴 ID (필수)
     * @param pathId 상속 체인을 식별하는 경로(예: "1.3.8"), null 가능
     * @return 현재(current) / 상속(inherited) 엔트리 목록
     * @throws Exception 조회 중 오류
     */
    PermissionChainResponse getPermissionChain(Long menuId, String pathId) throws Exception;

    /**
     * 특정 메뉴의 권한 엔트리들을 일괄 업서트합니다(정렬 포함).
     * 요청에 없는 기존 엔트리는 삭제합니다.
     *
     * @param menuId  메뉴 ID
     * @param entries {subject, sort, permissions} 목록
     * @throws Exception 저장 중 오류
     */
    void upsertPermissions(Long menuId, List<PermissionEntryDto> entries) throws Exception;
}
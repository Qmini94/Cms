package kr.co.itid.cms.service.cms.core.member;

import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.dto.cms.core.member.request.MemberCreateRequest;
import kr.co.itid.cms.dto.cms.core.member.request.MemberUpdateRequest;
import kr.co.itid.cms.dto.cms.core.member.response.MemberListResponse;
import kr.co.itid.cms.dto.cms.core.member.response.MemberResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 회원 관리 서비스 인터페이스입니다.
 * 회원 목록 조회, 단일 조회, 생성(임시 비밀번호 정책), 수정 기능을 제공합니다.
 */
public interface MemberService {

    /**
     * 회원 목록을 검색 조건 및 페이징 옵션에 따라 조회합니다.
     *
     * @param option   검색 조건(키워드, 검색 대상 필드, 기간 등)
     * @param pageable 페이징 및 정렬 정보
     * @return Page&lt;MemberListResponse&gt; 페이징 처리된 회원 목록
     * @throws Exception 조회 중 오류 발생 시
     */
    Page<MemberListResponse> searchMembers(SearchOption option, Pageable pageable) throws Exception;

    /**
     * 회원 단일 정보를 조회합니다.
     *
     * @param idx 회원 고유 ID
     * @return MemberResponse 조회된 회원 정보
     * @throws Exception 데이터 없음 등 오류 발생 시 (예: 미존재 시 EgovBizException)
     */
    MemberResponse getMemberByIdx(Long idx) throws Exception;

    /**
     * 내부 기본키(idx) 목록으로 표시 이름을 배치 조회합니다.
     *
     * @param idxList 회원 idx 목록
     * @return key: idx, value: 표시 이름
     * @throws Exception 조회 실패 시
     */
    Map<String, String> getDisplayNamesByIds(Set<String> idxList) throws Exception;

    /**
     * 회원을 생성합니다.
     * - 입력: userId, userName
     * - 비밀번호 정책: 평문(userId)로 설정 후 서비스에서 BCrypt 등으로 암호화하여 저장
     * - userId 중복 시 예외 발생
     *
     * @param request 생성 요청 DTO
     * @throws Exception 생성 중 오류 발생 시 (예: 중복 아이디 시 EgovBizException)
     */
    void createMemberWithIdEqualsPassword(MemberCreateRequest request) throws Exception;

    /**
     * 회원 정보를 수정합니다.
     * - 요구사항에 따라 대부분 필드 수정 허용
     * - 대상 회원이 없을 경우 예외 발생
     *
     * @param idx     회원 고유 ID
     * @param request 수정 요청 DTO
     * @throws Exception 수정 중 오류 발생 시 (예: 미존재 시 EgovBizException)
     */
    void updateMember(Long idx, MemberUpdateRequest request) throws Exception;
}

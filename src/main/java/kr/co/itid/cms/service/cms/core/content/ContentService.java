package kr.co.itid.cms.service.cms.core.content;

import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.dto.cms.core.content.request.ChildContentRequest;
import kr.co.itid.cms.dto.cms.core.content.request.ContentRequest;
import kr.co.itid.cms.dto.cms.core.content.response.ContentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * 콘텐츠 관리 서비스 인터페이스입니다.
 * 콘텐츠 생성, 조회, 수정, 삭제 기능을 제공합니다.
 */
public interface ContentService {

    /**
     * 콘텐츠 목록을 검색 조건 및 페이징 옵션에 따라 조회합니다.
     *
     * @param option 콘텐츠 검색 조건
     * @param pageable Pageable 객체
     * @return Page<ContentResponse> 페이징 처리된 콘텐츠 목록
     * @throws Exception 콘텐츠 조회 중 오류 발생 시
     */
    Page<ContentResponse> searchContents(SearchOption option, Pageable pageable) throws Exception;

    /**
     * 특정 컨텐츠의 같은 그룹의 콘텐츠 목록을 순서대로 조회합니다.
     *
     * @param idx 콘텐츠 ID
     * @return 해당 그룹의 하위 콘텐츠 리스트
     * @throws Exception DB 조회 또는 매핑 오류 발생 시
     */
    List<ContentResponse> getContentsByIdx(Long idx) throws Exception;

    /**
     * 콘텐츠의 group중 실제 isMain이 true인 콘텐츠 상세 조회
     *
     * @param idx 콘텐츠 고유번호
     * @return ContentResponse
     * @throws Exception DB 조회 실패 또는 데이터 없음
     */
    ContentResponse getContentByParentId(Long idx) throws Exception;

    /**
     * 대표 콘텐츠(루트 콘텐츠)를 생성합니다.
     * sort는 무조건 0, 저장 후 자기 자신을 parentId로 설정합니다.
     *
     * @param request 콘텐츠 등록 요청 객체
     * @throws Exception DB 저장 실패 시
     */
    void createRootContent(ContentRequest request) throws Exception;

    /**
     * 특정 콤텐츠 ID의 parentId 그룹에 속하는 하위 콘텐츠를 생성합니다.
     *
     * @param idx 콘텐츠 ID
     * @param request 콘텐츠 등록 요청 객체
     * @throws Exception DB 저장 실패 또는 유효성 오류
     */
    void createChildContent(Long idx, ContentRequest request) throws Exception;

    /**
     * 콘텐츠를 수정합니다.
     *
     * @param idx 수정 대상 콘텐츠 ID
     * @param request 수정 요청 객체
     * @throws Exception DB 조회 또는 저장 실패 시
     */
    void updateContent(Long idx, ContentRequest request) throws Exception;

    /**
     * 콘텐츠를 활성화합니다.
     *
     * @param idx 활성화할 콘텐츠
     * @throws Exception DB 조회 또는 저장 실패 시
     */
    void activeContent(Long idx) throws Exception;

    /**
     * 메뉴 트리 기준으로 사용 중인 콘텐츠만 is_use=1, 그 외는 is_use=0으로 동기화합니다.
     *
     * @param inUseContentIds 메뉴에서 참조 중인 content_id 집합 (null 또는 빈 경우 전체 0 처리)
     * @throws Exception 동기화 중 오류 발생 시
     */
    void syncUsageFlagsByContentIds(Set<String> inUseContentIds) throws Exception;

    /**
     * 단일 콘텐츠를 삭제합니다.
     *
     * @param idx 삭제할 콘텐츠 ID
     * @throws Exception DB 삭제 실패 또는 존재하지 않을 경우
     */
    void deleteContentByIdx(Long idx) throws Exception;

    /**
     * 루트 콘텐츠 + 그에 속한 모든 하위 콘텐츠를 삭제합니다.
     *
     * @param idx 삭제할 IDX
     * @throws Exception DB 삭제 실패 또는 트랜잭션 오류 발생 시
     */
    void deleteContentByParentId(Long idx) throws Exception;
}
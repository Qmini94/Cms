package kr.co.itid.cms.service.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.request.BoardCreateRequest;
import kr.co.itid.cms.dto.cms.core.board.request.BoardUpdateRequest;
import kr.co.itid.cms.dto.cms.core.board.request.BoardFieldDefinitionsUpsertRequest;
import kr.co.itid.cms.dto.cms.core.board.response.BoardFieldDefinitionResponse;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterListResponse;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterResponse;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
 * 게시판 마스터 관리 서비스 인터페이스입니다.
 * 게시판 목록 조회, 단일 조회, 생성, 수정, 삭제 기능을 제공합니다.
 */
public interface BoardMasterService {

    /**
     * 게시판 목록을 검색 조건 및 페이징 옵션에 따라 조회합니다.
     *
     * @param option 검색 조건 (키워드, 검색 대상 필드 등)
     * @param pageable 페이징 및 정렬 정보
     * @return Page&lt;BoardMasterListResponse&gt; 페이징 처리된 게시판 목록
     * @throws Exception 조회 중 오류 발생 시
     */
    Page<BoardMasterListResponse> searchBoardMasters(SearchOption option, Pageable pageable) throws Exception;

    /**
     * 게시판 단일 정보를 조회합니다.
     *
     * @param idx 게시판 고유 ID
     * @return BoardMasterResponse 조회된 게시판 정보
     * @throws Exception 조회 실패 또는 데이터 없음 등 오류 발생 시
     */
    BoardMasterResponse getBoardByIdx(Long idx) throws Exception;

    /**
     * 특정 게시판의 필드 정의 목록을 조회합니다.
     *
     * @param boardMasterIdx 게시판 고유 ID
     * @return List&lt;BoardFieldDefinitionResponse&gt; 필드 정의 목록
     * @throws Exception 조회 중 오류 발생 시
     */
    List<BoardFieldDefinitionResponse> getFieldDefinitions(Long boardMasterIdx) throws Exception;

    /**
     * 게시판을 생성합니다.
     * 게시판 필드 정의 저장 및 게시판 테이블 생성이 함께 수행됩니다.
     *
     * @param request 생성할 게시판 메타 정보와 필드 정의 목록
     * @throws Exception 생성 중 오류 발생 시
     */
    void createBoard(BoardCreateRequest request) throws Exception;

    /**
     * 게시판을 수정합니다.
     * board_id는 수정되지 않습니다.
     * 필드 정의 교체 후 실제 테이블 구조 동기화가 수행됩니다.
     *
     * @param request 수정할 게시판 메타 정보와 필드 정의 목록(게시판 고유 ID 포함)
     * @throws Exception 수정 중 오류 발생 시
     */
    void updateBoard(BoardUpdateRequest request) throws Exception;

    /**
     * 특정 게시판의 필드 정의를 일괄 업서트(교체)하고
     * 실제 물리 테이블 구조를 정의와 동기화합니다.
     *
     * @param request 게시판 고유 ID와 필드 정의 목록
     * @throws Exception 업서트 또는 동기화 중 오류 발생 시
     */
    void upsertFieldDefinitionsAndSync(BoardFieldDefinitionsUpsertRequest request) throws Exception;

    /**
     * 게시판을 삭제합니다.
     * 필드 정의 및 게시판 테이블도 함께 삭제됩니다.
     *
     * @param idx 삭제할 게시판 고유 ID
     * @throws Exception 삭제 중 오류 발생 시
     */
    void deleteBoard(Long idx) throws Exception;

    /**
     * 메뉴에서 참조 중인 board_id만 is_use=1, 나머지는 is_use=0으로 동기화합니다.
     * 빈/NULL이면 아무 것도 하지 않습니다(다른 사이트 오염 방지).
     *
     * @param inUseBoardIds 사용 중인 board_id 집합
     * @throws Exception 동기화 중 오류 발생 시
     */
    void syncUsageFlagsByBoardIds(Set<String> inUseBoardIds) throws Exception;

    /**
     * 특정 게시판의 실제 물리 테이블을
     * board_field_definition 정의와 동기화합니다.
     *
     * @param boardMasterIdx 게시판 고유 ID
     * @throws Exception 동기화 중 오류 발생 시
     */
    void syncPhysicalTableWithDefinitions(Long boardMasterIdx) throws Exception;
}
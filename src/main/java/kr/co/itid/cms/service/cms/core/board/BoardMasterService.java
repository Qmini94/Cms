package kr.co.itid.cms.service.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.request.BoardMasterRequest;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterListResponse;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterResponse;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
     * 게시판을 생성합니다.
     * 게시판 테이블도 함께 생성됩니다.
     *
     * @param request 생성할 게시판 정보
     * @throws Exception 생성 중 오류 발생 시
     */
    void createBoard(BoardMasterRequest request) throws Exception;

    /**
     * 게시판을 수정합니다.
     * board_id는 수정되지 않습니다.
     *
     * @param idx 수정할 게시판 고유 ID
     * @param request 수정할 게시판 정보
     * @throws Exception 수정 중 오류 발생 시
     */
    void updateBoard(Long idx, BoardMasterRequest request) throws Exception;

    /**
     * 게시판을 삭제합니다.
     * 게시판 테이블도 함께 삭제됩니다.
     *
     * @param idx 삭제할 게시판 고유 ID
     * @throws Exception 삭제 중 오류 발생 시
     */
    void deleteBoard(Long idx) throws Exception;
}
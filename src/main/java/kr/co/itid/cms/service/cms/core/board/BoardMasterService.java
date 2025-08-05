package kr.co.itid.cms.service.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.BoardSearchOption;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterListResponse;
import kr.co.itid.cms.dto.cms.core.board.request.BoardMasterRequest;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 게시판 마스터 관리 서비스 인터페이스입니다.
 * 게시판 정의 데이터를 조회, 등록, 수정, 삭제하는 메서드를 제공합니다.
 */
public interface BoardMasterService {

    /**
     * 게시판 목록을 검색 조건 및 페이징 옵션에 따라 조회합니다.
     *
     * @param option 게시글 검색 및 페이징 옵션
     * @param pageable Pageable 객체
     * @return Page&lt;BoardMasterListResponse&gt; 페이징 처리된 게시글 목록
     * @throws Exception 게시글 조회 중 오류 발생 시
     */
    Page<BoardMasterListResponse> searchBoardMasters(BoardSearchOption option, Pageable pageable) throws Exception;

    /**
     * 게시판 식별용 ID(board_id)로 게시판 정보를 조회합니다.
     *
     * @param boardId 게시판 식별용 ID
     * @return Optional&lt;BoardMasterResponse&gt; 해당 게시판 정보
     * @throws Exception 데이터베이스 또는 시스템 오류 발생 시
     */
    BoardMasterResponse getBoardByBoardId(String boardId) throws Exception;

    /**
     * 게시판 정보를 저장하거나 수정합니다.
     * idx가 없으면 신규 등록, 존재하면 수정 처리됩니다.
     *
     * @param idx 저장할 게시판 ID
     * @param request 저장할 게시판 정보
     * @return BoardMasterResponse 저장된 게시판 정보
     * @throws Exception 데이터 저장 중 오류 발생 시
     */
    void saveBoard(Long idx, BoardMasterRequest request) throws Exception;

    /**
     * 게시판을 삭제합니다.
     *
     * @param idx 삭제할 게시판 ID
     * @throws Exception 데이터 삭제 중 오류 발생 시
     */
    void deleteBoard(Long idx) throws Exception;
}
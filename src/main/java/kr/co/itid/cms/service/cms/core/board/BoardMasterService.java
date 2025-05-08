package kr.co.itid.cms.service.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.BoardMasterListResponse;
import kr.co.itid.cms.dto.cms.core.board.BoardMasterRequest;
import kr.co.itid.cms.dto.cms.core.board.BoardMasterResponse;

import java.util.List;
import java.util.Optional;

/**
 * 게시판 마스터 관리 서비스 인터페이스입니다.
 * 게시판 정의 데이터를 조회, 등록, 수정, 삭제하는 메서드를 제공합니다.
 */
public interface BoardMasterService {

    /**
     * 전체 게시판 목록을 조회합니다.
     *
     * @return List&lt;BoardMasterListResponse&gt; 게시판 목록
     * @throws Exception 데이터베이스 또는 시스템 오류 발생 시
     */
    List<BoardMasterListResponse> getAllBoards() throws Exception;

    /**
     * 게시판 식별용 ID(board_id)로 게시판 정보를 조회합니다.
     *
     * @param boardId 게시판 식별용 ID
     * @return Optional&lt;BoardMasterResponse&gt; 해당 게시판 정보
     * @throws Exception 데이터베이스 또는 시스템 오류 발생 시
     */
    Optional<BoardMasterResponse> getBoardByBoardId(String boardId) throws Exception;

    /**
     * 게시판 정보를 저장하거나 수정합니다.
     * idx가 없으면 신규 등록, 존재하면 수정 처리됩니다.
     *
     * @param idx 저장할 게시판 ID
     * @param request 저장할 게시판 정보
     * @return BoardMasterResponse 저장된 게시판 정보
     * @throws Exception 데이터 저장 중 오류 발생 시
     */
    void save(Long idx, BoardMasterRequest request) throws Exception;

    /**
     * 게시판을 삭제합니다.
     *
     * @param idx 삭제할 게시판 ID
     * @throws Exception 데이터 삭제 중 오류 발생 시
     */
    void delete(Long idx) throws Exception;
}
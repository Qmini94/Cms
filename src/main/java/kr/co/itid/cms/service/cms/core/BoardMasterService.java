package kr.co.itid.cms.service.cms.core;

import kr.co.itid.cms.entity.cms.core.BoardMaster;

import java.util.List;
import java.util.Optional;

/**
 * 게시판 마스터 관리 서비스 인터페이스
 * 게시판 정의 데이터를 조회, 등록, 수정, 삭제하는 메서드를 정의합니다.
 */
public interface BoardMasterService {

    /**
     * 전체 게시판 목록을 조회합니다.
     *
     * @return List<BoardMaster> 게시판 목록
     */
    List<BoardMaster> getAllBoards() throws Exception;

    /**
     * 게시판 ID(PK)로 게시판 정보를 조회합니다.
     *
     * @param id 게시판 고유번호 (PK)
     * @return Optional<BoardMaster> 해당 게시판 정보
     */
    Optional<BoardMaster> getBoardById(Long id) throws Exception;

    /**
     * 게시판 식별용 ID(board_id)로 게시판 정보를 조회합니다.
     *
     * @param boardId 게시판 식별용 ID
     * @return Optional<BoardMaster> 해당 게시판 정보
     */
    Optional<BoardMaster> getBoardByBoardId(String boardId) throws Exception;

    /**
     * 게시판 정보를 저장하거나 수정합니다.
     *
     * @param boardMaster 저장할 게시판 정보
     * @return BoardMaster 저장된 게시판 정보
     */
    BoardMaster save(BoardMaster boardMaster) throws Exception;

    /**
     * 게시판을 삭제합니다.
     *
     * @param id 삭제할 게시판 ID
     */
    void delete(Long id) throws Exception;
}
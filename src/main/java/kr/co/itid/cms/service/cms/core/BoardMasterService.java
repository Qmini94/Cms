package kr.co.itid.cms.service.cms.core;

import kr.co.itid.cms.entity.cms.core.BoardMaster;

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
     * @return List&lt;BoardMaster&gt; 게시판 목록
     * @throws Exception 데이터베이스 또는 시스템 오류 발생 시
     */
    List<BoardMaster> getAllBoards() throws Exception;

    /**
     * 게시판 ID(PK)로 게시판 정보를 조회합니다.
     *
     * @param id 게시판 고유번호 (PK)
     * @return Optional&lt;BoardMaster&gt; 해당 게시판 정보
     * @throws Exception 데이터베이스 또는 시스템 오류 발생 시
     */
    Optional<BoardMaster> getBoardById(Long id) throws Exception;

    /**
     * 게시판 식별용 ID(board_id)로 게시판 정보를 조회합니다.
     *
     * @param boardId 게시판 식별용 ID
     * @return Optional&lt;BoardMaster&gt; 해당 게시판 정보
     * @throws Exception 데이터베이스 또는 시스템 오류 발생 시
     */
    Optional<BoardMaster> getBoardByBoardId(String boardId) throws Exception;

    /**
     * 게시판 정보를 저장하거나 수정합니다.
     * id가 없으면 신규 등록, 존재하면 수정 처리됩니다.
     *
     * @param boardMaster 저장할 게시판 정보
     * @return BoardMaster 저장된 게시판 정보
     * @throws Exception 데이터 저장 중 오류 발생 시
     */
    BoardMaster save(BoardMaster boardMaster) throws Exception;

    /**
     * 게시판을 삭제합니다.
     *
     * @param id 삭제할 게시판 ID
     * @throws Exception 데이터 삭제 중 오류 발생 시
     */
    void delete(Long id) throws Exception;
}
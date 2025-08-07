package kr.co.itid.cms.repository.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.request.BoardMasterRequest;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterResponse;

public interface BoardMasterDao {

    /**
     * 게시판 단건 조회 (by idx)
     */
    BoardMasterResponse findBoardMasterByIdx(Long idx);

    /**
     * 게시판 생성 - 메타정보 등록
     */
    void insertBoardMaster(BoardMasterRequest request);

    /**
     * 게시판 생성 - 필드 정의 등록
     */
    void insertBoardFieldDefinitions(BoardMasterRequest request);

    /**
     * 게시판 테이블 생성 (board_{boardId})
     */
    void createBoardTable(String boardId);

    /**
     * 게시판 수정 - 메타정보 수정
     */
    void updateBoardMaster(Long idx, BoardMasterRequest request);

    /**
     * 게시판 수정 - 필드 정의 수정
     */
    void updateBoardFieldDefinitions(Long boardMasterIdx, BoardMasterRequest request);

    /**
     * 게시판 삭제 - 메타정보 삭제
     */
    void deleteBoardMaster(Long idx);

    /**
     * 게시판 삭제 - 필드 정의 삭제
     */
    void deleteBoardFieldDefinitions(Long boardMasterIdx);

    /**
     * 게시판 테이블 삭제 (DROP TABLE board_{boardId})
     */
    void dropBoardTable(String boardId);

    /**
     * 게시판 idx로 board_id 조회
     */
    String findBoardIdByIdx(Long idx);
}

package kr.co.itid.cms.repository.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.request.BoardMasterRequest;
import kr.co.itid.cms.dto.cms.core.board.request.BoardFieldDefinitionRequest;
import kr.co.itid.cms.dto.cms.core.board.response.BoardFieldDefinitionResponse;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterResponse;

import java.util.List;

/**
 * 게시판 마스터 DAO 인터페이스입니다.
 * 메타 정보, 필드 정의, 물리 테이블(DDL) 관련 작업을 제공합니다.
 */
public interface BoardMasterDao {

    // -------------------- 조회 --------------------

    /** 게시판 단일 정보 조회 */
    BoardMasterResponse findBoardMasterByIdx(Long idx) throws Exception;

    /** idx → board_id 조회 */
    String findBoardIdByIdx(Long idx) throws Exception;

    /** 필드 정의 목록 조회 (Response DTO) */
    List<BoardFieldDefinitionResponse> selectFieldDefinitions(Long boardMasterIdx) throws Exception;

    /** [보상용] 메타 스냅샷 조회 (Request DTO 형태) */
    BoardMasterRequest getMasterSnapshot(Long idx) throws Exception;

    /** [보상용] 필드 정의 스냅샷 조회 (Request DTO 형태) */
    List<BoardFieldDefinitionRequest> getFieldSnapshot(Long boardMasterIdx) throws Exception;

    // -------------------- 생성 --------------------

    /** 메타 저장 + 생성된 idx 반환 */
    Long insertBoardMasterReturningIdx(BoardMasterRequest req) throws Exception;

    /** 필드 정의 일괄 저장 */
    void insertBoardFieldDefinitions(Long boardMasterIdx, List<BoardFieldDefinitionRequest> fields) throws Exception;

    /** 물리 테이블 생성 */
    void createBoardTable(String boardId) throws Exception;

    // -------------------- 수정 --------------------

    /** 메타 수정 (board_id 불변) */
    void updateBoardMaster(Long idx, BoardMasterRequest req) throws Exception;

    /** 필드 정의 일괄 교체 (DELETE → INSERT) */
    void replaceBoardFieldDefinitions(Long boardMasterIdx, List<BoardFieldDefinitionRequest> fields) throws Exception;

    /**
     * 필드 정의 ↔ 물리 테이블 동기화
     * - 컬럼 ADD/MODIFY/DROP
     * - 보호 컬럼 제외
     * - 위험 변경 가드
     */
    void syncPhysicalTableWithDefinitions(Long boardMasterIdx) throws Exception;

    // -------------------- 삭제 --------------------

    /** 메타 삭제 */
    void deleteBoardMaster(Long idx) throws Exception;

    /** 필드 정의 삭제 */
    void deleteBoardFieldDefinitions(Long boardMasterIdx) throws Exception;

    /** 물리 테이블 삭제 */
    void dropBoardTable(String boardId) throws Exception;

    /** [보상용] 물리 테이블 안전 삭제 (존재하면 DROP, 없으면 무시) */
    void safeDropBoardTable(String boardId) throws Exception;
}
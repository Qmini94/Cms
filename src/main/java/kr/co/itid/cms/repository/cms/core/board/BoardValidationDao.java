package kr.co.itid.cms.repository.cms.core.board;

public interface BoardValidationDao {

    /**
     * board_master에서 유효한 board_id인지 확인
     * @param boardId 게시판 ID
     * @return 유효하면 true, 아니면 false
     */
    boolean isValidBoardId(String boardId);

    /**
     * boardId 검증 및 정제
     * @param boardId 검증할 게시판 ID
     * @return 검증되고 정제된 게시판 ID
     * @throws IllegalArgumentException boardId가 null이거나 빈 값인 경우
     * @throws SecurityException 보안 규칙 위반 시
     */
    String validateBoardId(String boardId);
}

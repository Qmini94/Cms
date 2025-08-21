package kr.co.itid.cms.repository.cms.core.board.impl;

import kr.co.itid.cms.repository.cms.core.board.BoardValidationDao;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository("boardValidationDao")
@RequiredArgsConstructor
public class BoardValidationDaoImpl implements BoardValidationDao {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean isValidBoardId(String boardId) {
        String sql = "SELECT COUNT(*) FROM board_master WHERE board_id = ? AND is_active = 1";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, boardId);
        return count != null && count > 0;
    }

    @Override
    public String validateBoardId(String boardId) {
        if (boardId == null || boardId.trim().isEmpty()) {
            throw new IllegalArgumentException("Board ID cannot be empty");
        }

        String cleaned = boardId.trim().toLowerCase();

        // 1. 패턴 검증 (영문자로 시작, 영문자/숫자/언더스코어만)
        if (!cleaned.matches("^[a-z][a-z0-9_]*$")) {
            throw new SecurityException("Invalid board ID pattern: " + boardId);
        }

        // 2. 길이 제한
        if (cleaned.length() > 30) {
            throw new SecurityException("Board ID too long: " + boardId);
        }

        // 3. DB 존재 여부 확인
        if (!isValidBoardId(cleaned)) {
            throw new SecurityException("Board not found or inactive: " + boardId);
        }

        return cleaned;
    }
}

package kr.co.itid.cms.service.cms.core.page.widget.handlers.board;

import kr.co.itid.cms.dto.cms.core.board.response.FieldDefinitionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.util.List;

/**
 * BoardPostSummary interface를 위한 RowMapper
 */
@RequiredArgsConstructor
public class BoardPostSummaryRowMapper implements RowMapper<BoardPostSummary> {

    private final List<FieldDefinitionResponse> fieldDefinitions;

    @Override
    public BoardPostSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new BoardPostSummaryImpl(
                rs.getLong("id"),
                rs.getString("slug"),
                rs.getString("title"),
                rs.getTimestamp("createdAt") != null ?
                        rs.getTimestamp("createdAt").toLocalDateTime().atOffset(ZoneOffset.of("+09:00")) : null,
                rs.getString("author"),
                rs.getLong("views"),
                rs.getBoolean("hasAttach")
        );
    }
}
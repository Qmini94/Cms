package kr.co.itid.cms.repository.cms.core.board.impl;

import kr.co.itid.cms.dto.cms.core.board.request.BoardMasterRequest;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterResponse;
import kr.co.itid.cms.repository.cms.core.board.BoardMasterDao;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository("boardMasterDao")
@RequiredArgsConstructor
public class BoardMasterDaoImpl implements BoardMasterDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public BoardMasterResponse findBoardMasterByIdx(Long idx) {
        String sql = """
            SELECT 
                idx,
                board_id,
                board_name,
                description,
                board_type,
                is_use,
                is_admin_approval,
                is_privacy_option,
                max_file_upload,
                max_total_file_size,
                restricted_files
            FROM board_master
            WHERE idx = :idx
        """;

        MapSqlParameterSource params = new MapSqlParameterSource("idx", idx);

        return jdbcTemplate.query(sql, params, rs -> {
            if (rs.next()) {
                return mapRowToResponse(rs);
            }
            return null;
        });
    }

    private BoardMasterResponse mapRowToResponse(ResultSet rs) throws SQLException {
        return BoardMasterResponse.builder()
                .idx(rs.getLong("idx"))
                .boardId(rs.getString("board_id"))
                .boardName(rs.getString("board_name"))
                .description(rs.getString("description"))
                .boardType(rs.getString("board_type"))
                .isUse(rs.getBoolean("is_use"))
                .isAdminApproval(rs.getBoolean("is_admin_approval"))
                .isPrivacyOption(rs.getBoolean("is_privacy_option"))
                .maxFileUpload(rs.getInt("max_file_upload"))
                .maxTotalFileSize(rs.getInt("max_total_file_size"))
                .restrictedFiles(rs.getString("restricted_files"))
                .build();
    }

    @Override
    public void insertBoardMaster(BoardMasterRequest request) {

    }

    @Override
    public void insertBoardFieldDefinitions(BoardMasterRequest request) {

    }

    @Override
    public void createBoardTable(String boardId) {

    }

    @Override
    public void updateBoardMaster(Long idx, BoardMasterRequest request) {

    }

    @Override
    public void updateBoardFieldDefinitions(Long boardMasterIdx, BoardMasterRequest request) {

    }

    @Override
    public void deleteBoardMaster(Long idx) {

    }

    @Override
    public void deleteBoardFieldDefinitions(Long boardMasterIdx) {

    }

    @Override
    public void dropBoardTable(String boardId) {

    }

    @Override
    public String findBoardIdByIdx(Long idx) {
        return "";
    }
}

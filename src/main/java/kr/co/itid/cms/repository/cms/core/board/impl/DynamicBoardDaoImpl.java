package kr.co.itid.cms.repository.cms.core.board.impl;

import kr.co.itid.cms.dto.cms.core.board.response.FieldDefinitionResponse;
import kr.co.itid.cms.dto.cms.core.common.PaginationOption;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.repository.cms.core.board.DynamicBoardDao;
import kr.co.itid.cms.repository.cms.core.board.sqlbuilder.DynamicBoardSqlBuilder;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.psl.dataaccess.EgovAbstractMapper;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository("dynamicBoardDao")
@RequiredArgsConstructor
public class DynamicBoardDaoImpl extends EgovAbstractMapper implements DynamicBoardDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DynamicBoardSqlBuilder dynamicBoardSqlBuilder;

    /**
     * menu_id로 board_id를 조회
     */
    private String resolveBoardIdByMenuId(Long menuId) {
        String sql = """
            SELECT bm.board_id
            FROM menu m
            JOIN board_master bm ON bm.idx = CAST(m.value AS UNSIGNED)
            WHERE m.idx = :menuId AND m.type = 'board' AND m.is_show = 1
        """;

        Map<String, Object> params = Map.of("menuId", menuId);
        return jdbcTemplate.queryForObject(sql, params, String.class);
    }

    /**
     * menu_id로 board_master.idx 조회
     */
    private Long resolveBoardMasterIdxByMenuId(Long menuId) {
        String sql = """
            SELECT bm.idx
            FROM menu m
            JOIN board_master bm ON bm.idx = CAST(m.value AS UNSIGNED)
            WHERE m.idx = :menuId AND m.type = 'board' AND m.is_show = 1
        """;

        Map<String, Object> params = Map.of("menuId", menuId);
        return jdbcTemplate.queryForObject(sql, params, Long.class);
    }

    @Override
    public List<FieldDefinitionResponse> getFieldDefinitionsByMenuId(Long menuId) {
        Long boardMasterIdx = resolveBoardMasterIdxByMenuId(menuId);
        String sql = """
            SELECT
                id,
                board_master_idx AS boardMasterIdx,
                field_name AS fieldName,
                display_name AS displayName,
                field_type AS fieldType,
                is_required AS required,
                is_searchable AS searchable,
                field_order AS fieldOrder,
                default_value AS defaultValue,
                placeholder
            FROM board_field_definition
            WHERE board_master_idx = :boardMasterIdx
            ORDER BY field_order ASC
        """;

        Map<String, Object> params = Map.of("boardMasterIdx", boardMasterIdx);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> {
            FieldDefinitionResponse field = new FieldDefinitionResponse();
            field.setId(rs.getLong("id"));
            field.setBoardMasterIdx(rs.getLong("boardMasterIdx"));
            field.setFieldName(rs.getString("fieldName"));
            field.setDisplayName(rs.getString("displayName"));
            field.setFieldType(rs.getString("fieldType"));
            field.setRequired(rs.getBoolean("required"));
            field.setSearchable(rs.getBoolean("searchable"));
            field.setFieldOrder(rs.getInt("fieldOrder"));
            field.setDefaultValue(rs.getString("defaultValue"));
            field.setPlaceholder(rs.getString("placeholder"));
            return field;
        });
    }

    @Override
    public void insertByMenuId(Long menuId, Map<String, Object> data) {
        String boardId = resolveBoardIdByMenuId(menuId);
        List<FieldDefinitionResponse> fields = getFieldDefinitionsByMenuId(menuId);

        String sql = dynamicBoardSqlBuilder.buildInsertQuery(boardId, fields, data);
        jdbcTemplate.update(sql, data);
    }

    @Override
    public void updateByMenuId(Long menuId, Long id, Map<String, Object> data) {
        String boardId = resolveBoardIdByMenuId(menuId);
        List<FieldDefinitionResponse> fields = getFieldDefinitionsByMenuId(menuId);

        String sql = dynamicBoardSqlBuilder.buildUpdateQuery(boardId, fields, data);
        data.put("id", id);
        jdbcTemplate.update(sql, data);
    }

    @Override
    public Map<String, Object> selectOneByMenuId(Long menuId, Long id) {
        String boardId = resolveBoardIdByMenuId(menuId);
        List<FieldDefinitionResponse> fields = getFieldDefinitionsByMenuId(menuId);

        String sql = dynamicBoardSqlBuilder.buildSelectOneQuery(boardId, fields);
        Map<String, Object> params = Map.of("id", id);
        return jdbcTemplate.queryForMap(sql, params);
    }

    @Override
    public void deleteByMenuId(Long menuId, Long id) {
        String boardId = resolveBoardIdByMenuId(menuId);
        String sql = dynamicBoardSqlBuilder.buildDeleteQuery(boardId);

        Map<String, Object> params = Map.of("id", id);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public Page<Map<String, Object>> selectListByMenuId(Long menuId, SearchOption option, PaginationOption pagination) {
        String boardId = resolveBoardIdByMenuId(menuId);
        List<FieldDefinitionResponse> fields = getFieldDefinitionsByMenuId(menuId);

        return dynamicBoardSqlBuilder.buildPaginatedListQuery(jdbcTemplate, boardId, fields, option, pagination);
    }
}
package kr.co.itid.cms.repository.cms.core.board.impl;

import kr.co.itid.cms.dto.cms.core.board.response.FieldDefinitionResponse;
import kr.co.itid.cms.dto.cms.core.common.PaginationOption;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.repository.cms.core.board.DynamicBoardDao;
import kr.co.itid.cms.repository.cms.core.board.sqlbuilder.DynamicBoardSqlBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Repository("dynamicBoardDao")
@RequiredArgsConstructor
public class DynamicBoardDaoImpl implements DynamicBoardDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DynamicBoardSqlBuilder dynamicBoardSqlBuilder;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /**
     * menu_id로 board_id를 조회
     */
    private String resolveBoardIdByMenuId(Long menuId) {
        String sql = """
            SELECT bm.board_id
            FROM cms_menu m
            JOIN board_master bm ON bm.idx = CAST(m.value AS UNSIGNED)
            WHERE m.id = :menuId AND m.type = 'board' AND m.is_show = 1
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
            FROM cms_menu m
            JOIN board_master bm ON bm.idx = CAST(m.value AS UNSIGNED)
            WHERE m.id = :menuId AND m.type = 'board' AND m.is_show = 1
        """;

        Map<String, Object> params = Map.of("menuId", menuId);
        return jdbcTemplate.queryForObject(sql, params, Long.class);
    }

    /**
     * boardId로 필드 정의 조회
     */
    private List<FieldDefinitionResponse> getFieldDefinitionsByBoardId(String boardId) {
        String sql = """
        SELECT
            bfd.id,
            bfd.board_master_idx AS boardMasterIdx,
            bfd.field_name AS fieldName,
            bfd.display_name AS displayName,
            bfd.field_type AS fieldType,
            bfd.is_required AS required,
            bfd.is_searchable AS searchable,
            bfd.field_order AS fieldOrder,
            bfd.default_value AS defaultValue,
            bfd.placeholder
        FROM board_field_definition bfd
        JOIN board_master bm ON bm.idx = bfd.board_master_idx
        WHERE bm.board_id = :boardId AND bm.is_active = 1
        ORDER BY bfd.field_order ASC
    """;

        Map<String, Object> params = Map.of("boardId", boardId);

        return jdbcTemplate.query(sql, params, (rs, rowNum) ->
                FieldDefinitionResponse.builder()
                        .id(rs.getLong("id"))
                        .boardMasterIdx(rs.getLong("boardMasterIdx"))
                        .fieldName(rs.getString("fieldName"))
                        .displayName(rs.getString("displayName"))
                        .fieldType(rs.getString("fieldType"))
                        .required(rs.getBoolean("required"))
                        .searchable(rs.getBoolean("searchable"))
                        .fieldOrder(rs.getInt("fieldOrder"))
                        .defaultValue(rs.getString("defaultValue"))
                        .placeholder(rs.getString("placeholder"))
                        .build()
        );
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

        return jdbcTemplate.query(sql, params, (rs, rowNum) ->
                FieldDefinitionResponse.builder()
                        .id(rs.getLong("id"))
                        .boardMasterIdx(rs.getLong("boardMasterIdx"))
                        .fieldName(rs.getString("fieldName"))
                        .displayName(rs.getString("displayName"))
                        .fieldType(rs.getString("fieldType"))
                        .required(rs.getBoolean("required"))
                        .searchable(rs.getBoolean("searchable"))
                        .fieldOrder(rs.getInt("fieldOrder"))
                        .defaultValue(rs.getString("defaultValue"))
                        .placeholder(rs.getString("placeholder"))
                        .build()
        );
    }

    @Override
    public void insertByMenuId(Long menuId, Map<String, Object> data) {
        String boardId = resolveBoardIdByMenuId(menuId);
        List<FieldDefinitionResponse> fields = getFieldDefinitionsByMenuId(menuId);

        // KST 기준 현재 시각
        LocalDateTime now = LocalDateTime.now(KST);
        // 이미 값이 들어오면 덮어쓰지 않음
        data.putIfAbsent("created_date", Timestamp.valueOf(now));
        data.put("updated_date", Timestamp.valueOf(now));

        String sql = dynamicBoardSqlBuilder.buildInsertQuery(boardId, fields, data);
        jdbcTemplate.update(sql, data); // NamedParameterJdbcTemplate 가정
    }

    @Override
    public void updateByMenuId(Long menuId, Long idx, Map<String, Object> data) {
        String boardId = resolveBoardIdByMenuId(menuId);
        List<FieldDefinitionResponse> fields = getFieldDefinitionsByMenuId(menuId);

        data.put("idx", idx);
        data.put("updated_date", Timestamp.valueOf(LocalDateTime.now(KST)));

        String sql = dynamicBoardSqlBuilder.buildUpdateQuery(boardId, fields, data);
        jdbcTemplate.update(sql, data);
    }

    @Override
    public Map<String, Object> selectOneByMenuId(Long menuId, Long idx) {
        String boardId = resolveBoardIdByMenuId(menuId);
        List<FieldDefinitionResponse> fields = getFieldDefinitionsByMenuId(menuId);

        String sql = dynamicBoardSqlBuilder.buildSelectOneQuery(boardId, fields);
        Map<String, Object> params = Map.of("idx", idx);
        return jdbcTemplate.queryForMap(sql, params);
    }

    @Override
    public String selectRegIdByMenuId(Long menuId, Long idx) throws Exception {
        // 1) 보드 식별 및 검증
        String boardId = resolveBoardIdByMenuId(menuId);
        validateBoardId(boardId);

        // 2) 쿼리
        String sql = "SELECT reg_id FROM board_" + boardId +
                " WHERE idx = :idx AND is_deleted = false";
        Map<String, Object> params = Map.of("idx", idx);

        // 3) 결과가 없으면 null 반환
        return jdbcTemplate.query(sql, params, rs -> rs.next() ? rs.getString("reg_id") : null);
    }

    @Override
    public void increaseViewCountByMenuId(Long menuId, Long idx) {
        String boardId = resolveBoardIdByMenuId(menuId);
        String sql = String.format("UPDATE board_%s SET view_count = view_count + 1 WHERE idx = :idx AND is_deleted = false", boardId);
        Map<String, Object> params = Map.of("idx", idx);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public void deleteByMenuId(Long menuId, Long idx) {
        String boardId = resolveBoardIdByMenuId(menuId);
        String sql = dynamicBoardSqlBuilder.buildDeleteQuery(boardId);

        Map<String, Object> params = Map.of("idx", idx);
        jdbcTemplate.update(sql, params);
    }

    @Override
    public Page<Map<String, Object>> selectListByMenuId(Long menuId, SearchOption option, PaginationOption pagination) {
        String boardId = resolveBoardIdByMenuId(menuId);
        List<FieldDefinitionResponse> fields = getFieldDefinitionsByMenuId(menuId);

        return dynamicBoardSqlBuilder.buildPaginatedListQuery(jdbcTemplate, boardId, fields, option, pagination);
    }

    /**
     * 전자정부 보안: boardId 검증
     */
    private void validateBoardId(String boardId) {
        if (boardId == null || boardId.trim().isEmpty()) {
            throw new IllegalArgumentException("Board ID cannot be null or empty");
        }

        // SQL Injection 방지: 영숫자와 언더스코어만 허용
        if (!boardId.matches("^[a-zA-Z0-9_]+$")) {
            throw new SecurityException("Invalid board ID format: " + boardId);
        }

        // 길이 제한
        if (boardId.length() > 50) {
            throw new SecurityException("Board ID too long: " + boardId);
        }
    }

    /**
     * 필드 정의에서 특정 타입의 컬럼명 찾기
     * @param fields 필드 정의 목록
     * @param preferredType 우선적으로 찾을 필드 타입
     * @param fallbackColumn 못 찾았을 때 사용할 기본 컬럼명
     * @return 컬럼명
     */
    private String findColumnByType(List<FieldDefinitionResponse> fields, String preferredType, String fallbackColumn) {
        return fields.stream()
                .filter(field -> preferredType.equals(field.getFieldType()))
                .map(FieldDefinitionResponse::getFieldName)
                .findFirst()
                .orElse(fallbackColumn);
    }
}
package kr.co.itid.cms.repository.cms.core.board.impl;

import kr.co.itid.cms.dto.cms.core.board.request.BoardFieldDefinitionRequest;
import kr.co.itid.cms.dto.cms.core.board.request.BoardMasterRequest;
import kr.co.itid.cms.dto.cms.core.board.response.BoardFieldDefinitionResponse;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterResponse;
import kr.co.itid.cms.repository.cms.core.board.BoardMasterDao;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository("boardMasterDao")
@RequiredArgsConstructor
public class BoardMasterDaoImpl implements BoardMasterDao {

    private final NamedParameterJdbcTemplate jdbc;

    private static final Set<String> RESERVED = Set.of(
            "idx", "created_date", "updated_date",
            "is_deleted", "view_count", "reg_id", "reg_name"
    );

    private static final Map<String, String> DB_TYPE_MAP = Map.of(
            "VARCHAR", "VARCHAR(255)",
            "TEXT", "TEXT",
            "INT", "INT",
            "BIGINT", "BIGINT",
            "DATE", "DATE",
            "DATETIME", "DATETIME",
            "BOOLEAN", "TINYINT(1)"
    );

    private String tableName(String boardId) {
        return "board_" + boardId;
    }

    // ==================== 조회 ====================

    @Override
    public BoardMasterResponse findBoardMasterByIdx(Long idx) {
        String sql = """
            SELECT 
                bm.idx,
                bm.board_id,
                bm.board_name,
                bm.description,
                bm.is_use,
                bm.board_type,
                bm.is_admin_approval,
                bm.is_privacy_option,
                bm.max_file_upload,
                bm.max_total_file_size,
                bm.restricted_files,
                bm.max_file_size,
                bm.allowed_images,
                bm.max_image_size,
                bm.is_sms_alert,
                bm.is_required_fields,
                bm.is_comment,
                bm.is_use_period,
                bm.is_author_posts_view,
                bm.is_admin_deleted_view,
                bm.list_count,
                bm.is_show_author,
                bm.is_show_date,
                bm.is_search_field_control,
                bm.is_top_post,
                DATE_FORMAT(bm.created_date, '%Y-%m-%d %H:%i:%s') AS created_date,
                DATE_FORMAT(bm.updated_date, '%Y-%m-%d %H:%i:%s') AS updated_date
            FROM board_master bm
            WHERE bm.idx = :idx
            LIMIT 1
        """;

        List<BoardMasterResponse> list = jdbc.query(sql, Map.of("idx", idx), (rs, n) ->
                BoardMasterResponse.builder()
                        .idx(rs.getLong("idx"))
                        .boardId(rs.getString("board_id"))
                        .boardName(rs.getString("board_name"))
                        .description(rs.getString("description"))
                        .isUse(getNullableBoolean(rs, "is_use"))
                        .boardType(rs.getString("board_type"))
                        .isAdminApproval(getNullableBoolean(rs, "is_admin_approval"))
                        .isPrivacyOption(getNullableBoolean(rs, "is_privacy_option"))
                        .maxFileUpload(getNullableInt(rs, "max_file_upload"))
                        .maxTotalFileSize(getNullableInt(rs, "max_total_file_size"))
                        .restrictedFiles(rs.getString("restricted_files"))
                        .maxFileSize(getNullableInt(rs, "max_file_size"))
                        .allowedImages(rs.getString("allowed_images"))
                        .maxImageSize(getNullableInt(rs, "max_image_size"))
                        .isSmsAlert(getNullableBoolean(rs, "is_sms_alert"))
                        .isRequiredFields(getNullableBoolean(rs, "is_required_fields"))
                        .isComment(getNullableBoolean(rs, "is_comment"))
                        .isUsePeriod(getNullableBoolean(rs, "is_use_period"))
                        .isAuthorPostsView(getNullableBoolean(rs, "is_author_posts_view"))
                        .isAdminDeletedView(getNullableBoolean(rs, "is_admin_deleted_view"))
                        .listCount(getNullableInt(rs, "list_count"))
                        .isShowAuthor(getNullableBoolean(rs, "is_show_author"))
                        .isShowDate(getNullableBoolean(rs, "is_show_date"))
                        .isSearchFieldControl(getNullableBoolean(rs, "is_search_field_control"))
                        .isTopPost(getNullableBoolean(rs, "is_top_post"))
                        .createdDate(rs.getString("created_date"))
                        .updatedDate(rs.getString("updated_date"))
                        .build()
        );
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public String findBoardIdByIdx(Long idx) {
        String sql = "SELECT board_id FROM board_master WHERE idx=:idx";
        List<String> list = jdbc.queryForList(sql, Map.of("idx", idx), String.class);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<BoardFieldDefinitionResponse> selectFieldDefinitions(Long boardMasterIdx) {
        String sql = """
            SELECT
                id,
                board_master_idx,
                field_name,
                display_name,
                field_type,
                is_required,
                is_searchable,
                field_order,
                default_value,
                placeholder,
                DATE_FORMAT(created_date, '%Y-%m-%d %H:%i:%s') AS created_date,
                DATE_FORMAT(updated_date, '%Y-%m-%d %H:%i:%s') AS updated_date
            FROM board_field_definition
            WHERE board_master_idx = :idx
            ORDER BY field_order ASC, id ASC
        """;
        return jdbc.query(sql, Map.of("idx", boardMasterIdx), (rs, n) ->
                BoardFieldDefinitionResponse.builder()
                        .id(rs.getLong("id"))
                        .boardMasterIdx(rs.getLong("board_master_idx"))
                        .fieldName(rs.getString("field_name"))
                        .displayName(rs.getString("display_name"))
                        .fieldType(rs.getString("field_type"))
                        .isRequired(rs.getBoolean("is_required"))
                        .isSearchable(rs.getBoolean("is_searchable"))
                        .fieldOrder(rs.getInt("field_order"))
                        .defaultValue(rs.getString("default_value"))
                        .placeholder(rs.getString("placeholder"))
                        .createdDate(rs.getString("created_date"))
                        .updatedDate(rs.getString("updated_date"))
                        .build()
        );
    }

    /** [보상용] 메타 스냅샷 */
    @Override
    public BoardMasterRequest getMasterSnapshot(Long idx) {
        String sql = """
            SELECT
                board_id, board_name, board_type, description, is_use,
                is_admin_approval, is_privacy_option,
                max_file_upload, max_total_file_size, restricted_files,
                max_file_size, allowed_images, max_image_size,
                is_sms_alert, is_required_fields, is_comment, is_use_period,
                is_author_posts_view, is_admin_deleted_view, list_count,
                is_show_author, is_show_date, is_search_field_control, is_top_post
            FROM board_master
            WHERE idx=:idx
            LIMIT 1
        """;
        List<BoardMasterRequest> list = jdbc.query(sql, Map.of("idx", idx), (rs, n) ->
                BoardMasterRequest.builder()
                        .boardId(rs.getString("board_id"))
                        .boardName(rs.getString("board_name"))
                        .boardType(rs.getString("board_type"))
                        .description(rs.getString("description"))
                        .isUse(getNullableBoolean(rs, "is_use"))
                        .isAdminApproval(getNullableBoolean(rs, "is_admin_approval"))
                        .isPrivacyOption(getNullableBoolean(rs, "is_privacy_option"))
                        .maxFileUpload(getNullableInt(rs, "max_file_upload"))
                        .maxTotalFileSize(getNullableInt(rs, "max_total_file_size"))
                        .restrictedFiles(rs.getString("restricted_files"))
                        .maxFileSize(getNullableInt(rs, "max_file_size"))
                        .allowedImages(rs.getString("allowed_images"))
                        .maxImageSize(getNullableInt(rs, "max_image_size"))
                        .isSmsAlert(getNullableBoolean(rs, "is_sms_alert"))
                        .isRequiredFields(getNullableBoolean(rs, "is_required_fields"))
                        .isComment(getNullableBoolean(rs, "is_comment"))
                        .isUsePeriod(getNullableBoolean(rs, "is_use_period"))
                        .isAuthorPostsView(getNullableBoolean(rs, "is_author_posts_view"))
                        .isAdminDeletedView(getNullableBoolean(rs, "is_admin_deleted_view"))
                        .listCount(getNullableInt(rs, "list_count"))
                        .isShowAuthor(getNullableBoolean(rs, "is_show_author"))
                        .isShowDate(getNullableBoolean(rs, "is_show_date"))
                        .isSearchFieldControl(getNullableBoolean(rs, "is_search_field_control"))
                        .isTopPost(getNullableBoolean(rs, "is_top_post"))
                        .build()
        );
        return list.isEmpty() ? null : list.get(0);
    }

    /** [보상용] 필드 스냅샷 */
    @Override
    public List<BoardFieldDefinitionRequest> getFieldSnapshot(Long boardMasterIdx) {
        String sql = """
            SELECT field_name, display_name, field_type,
                   is_required, is_searchable, field_order,
                   default_value, placeholder
            FROM board_field_definition
            WHERE board_master_idx=:idx
            ORDER BY field_order ASC, id ASC
        """;
        return jdbc.query(sql, Map.of("idx", boardMasterIdx), (rs, n) ->
                BoardFieldDefinitionRequest.builder()
                        .fieldName(rs.getString("field_name"))
                        .displayName(rs.getString("display_name"))
                        .fieldType(rs.getString("field_type"))
                        .isRequired(rs.getBoolean("is_required"))
                        .isSearchable(rs.getBoolean("is_searchable"))
                        .fieldOrder(rs.getInt("field_order"))
                        .defaultValue(rs.getString("default_value"))
                        .placeholder(rs.getString("placeholder"))
                        .build()
        );
    }

    // ==================== 생성 ====================

    @Override
    public Long insertBoardMasterReturningIdx(BoardMasterRequest req) {
        String sql = """
        INSERT INTO board_master
          (board_id, board_name, board_type, description, is_use,
           is_admin_approval, is_privacy_option, 
           max_file_upload, max_total_file_size, restricted_files,
           max_file_size, allowed_images, max_image_size,
           is_sms_alert, is_required_fields, is_comment, is_use_period,
           is_author_posts_view, is_admin_deleted_view, list_count,
           is_show_author, is_show_date, is_search_field_control, is_top_post)
        VALUES
          (:boardId, :boardName, :boardType, :description, :isUse,
           :isAdminApproval, :isPrivacyOption,
           :maxFileUpload, :maxTotalFileSize, :restrictedFiles,
           :maxFileSize, :allowedImages, :maxImageSize,
           :isSmsAlert, :isRequiredFields, :isComment, :isUsePeriod,
           :isAuthorPostsView, :isAdminDeletedView, :listCount,
           :isShowAuthor, :isShowDate, :isSearchFieldControl, :isTopPost)
    """;

        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("boardId", req.getBoardId())
                .addValue("boardName", req.getBoardName())
                .addValue("boardType", req.getBoardType())
                .addValue("description", req.getDescription())
                .addValue("isUse", Optional.ofNullable(req.getIsUse()).orElse(Boolean.TRUE))
                .addValue("isAdminApproval", Optional.ofNullable(req.getIsAdminApproval()).orElse(false))
                .addValue("isPrivacyOption", Optional.ofNullable(req.getIsPrivacyOption()).orElse(false))
                .addValue("maxFileUpload", Optional.ofNullable(req.getMaxFileUpload()).orElse(0))
                .addValue("maxTotalFileSize", Optional.ofNullable(req.getMaxTotalFileSize()).orElse(0))
                .addValue("restrictedFiles", req.getRestrictedFiles())
                .addValue("maxFileSize", req.getMaxFileSize())
                .addValue("allowedImages", req.getAllowedImages())
                .addValue("maxImageSize", req.getMaxImageSize())
                .addValue("isSmsAlert", Optional.ofNullable(req.getIsSmsAlert()).orElse(false))
                .addValue("isRequiredFields", Optional.ofNullable(req.getIsRequiredFields()).orElse(false))
                .addValue("isComment", Optional.ofNullable(req.getIsComment()).orElse(false))
                .addValue("isUsePeriod", Optional.ofNullable(req.getIsUsePeriod()).orElse(false))
                .addValue("isAuthorPostsView", Optional.ofNullable(req.getIsAuthorPostsView()).orElse(false))
                .addValue("isAdminDeletedView", Optional.ofNullable(req.getIsAdminDeletedView()).orElse(false))
                .addValue("listCount", Optional.ofNullable(req.getListCount()).orElse(10))
                .addValue("isShowAuthor", Optional.ofNullable(req.getIsShowAuthor()).orElse(true))
                .addValue("isShowDate", Optional.ofNullable(req.getIsShowDate()).orElse(true))
                .addValue("isSearchFieldControl", Optional.ofNullable(req.getIsSearchFieldControl()).orElse(false))
                .addValue("isTopPost", Optional.ofNullable(req.getIsTopPost()).orElse(false));

        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(sql, p, kh, new String[]{"idx"});
        Number n = (Number) Objects.requireNonNull(kh.getKeys()).get("idx");
        return n.longValue();
    }

    @Override
    public void insertBoardFieldDefinitions(Long boardMasterIdx, List<BoardFieldDefinitionRequest> fields) {
        if (fields == null || fields.isEmpty()) return;

        String sql = """
        INSERT INTO board_field_definition
          (board_master_idx, field_name, display_name, field_type,
           is_required, is_searchable, field_order, default_value, placeholder)
        VALUES
          (:masterIdx, :fieldName, :displayName, :fieldType,
           :isRequired, :isSearchable, :fieldOrder, :defaultValue, :placeholder)
    """;

        MapSqlParameterSource[] batch = fields.stream()
                .map(f -> new MapSqlParameterSource()
                        .addValue("masterIdx", boardMasterIdx)
                        .addValue("fieldName", f.getFieldName())
                        .addValue("displayName", f.getDisplayName())
                        .addValue("fieldType", f.getFieldType())
                        .addValue("isRequired", Boolean.TRUE.equals(f.getIsRequired()))
                        .addValue("isSearchable", Boolean.TRUE.equals(f.getIsSearchable()))
                        .addValue("fieldOrder", Optional.ofNullable(f.getFieldOrder()).orElse(0))
                        .addValue("defaultValue", f.getDefaultValue())
                        .addValue("placeholder", f.getPlaceholder()))
                .toArray(MapSqlParameterSource[]::new);

        jdbc.batchUpdate(sql, batch);
    }

    @Override
    public void createBoardTable(String boardId) throws Exception{
        String table = tableName(boardId);
        Long masterIdx = getMasterIdxByBoardId(boardId); // 생성하지 않음
        List<FieldDef> defs = loadFieldDefs(masterIdx);

        String ddl = buildCreateTableDDL(table, defs);
        jdbc.getJdbcTemplate().execute(ddl);

        // MySQL 8.0.13+만 IF NOT EXISTS 지원. 낮은 버전이면 try/catch 처리
        jdbc.getJdbcTemplate().execute("CREATE INDEX IF NOT EXISTS idx_" + table + "__isdel_created ON " + table + " (is_deleted, created_date)");
        jdbc.getJdbcTemplate().execute("CREATE INDEX IF NOT EXISTS idx_" + table + "__created ON " + table + " (created_date)");
    }

    // ==================== 수정 ====================

    @Override
    public void updateBoardMaster(Long idx, BoardMasterRequest req) {
        String sql = """
            UPDATE board_master
               SET board_name=:boardName,
                   board_type=:boardType,
                   description=:description,
                   is_use=:isUse,
                   is_admin_approval=:isAdminApproval,
                   is_privacy_option=:isPrivacyOption,
                   max_file_upload=:maxFileUpload,
                   max_total_file_size=:maxTotalFileSize,
                   restricted_files=:restrictedFiles,
                   max_file_size=:maxFileSize,
                   allowed_images=:allowedImages,
                   max_image_size=:maxImageSize,
                   is_sms_alert=:isSmsAlert,
                   is_required_fields=:isRequiredFields,
                   is_comment=:isComment,
                   is_use_period=:isUsePeriod,
                   is_author_posts_view=:isAuthorPostsView,
                   is_admin_deleted_view=:isAdminDeletedView,
                   list_count=:listCount,
                   is_show_author=:isShowAuthor,
                   is_show_date=:isShowDate,
                   is_search_field_control=:isSearchFieldControl,
                   is_top_post=:isTopPost,
                   updated_date=CURRENT_TIMESTAMP
             WHERE idx=:idx
        """;
        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("idx", idx)
                .addValue("boardName", req.getBoardName())
                .addValue("boardType", req.getBoardType())
                .addValue("description", req.getDescription())
                .addValue("isUse", Optional.ofNullable(req.getIsUse()).orElse(Boolean.TRUE))
                .addValue("isAdminApproval", Optional.ofNullable(req.getIsAdminApproval()).orElse(false))
                .addValue("isPrivacyOption", Optional.ofNullable(req.getIsPrivacyOption()).orElse(false))
                .addValue("maxFileUpload", Optional.ofNullable(req.getMaxFileUpload()).orElse(0))
                .addValue("maxTotalFileSize", Optional.ofNullable(req.getMaxTotalFileSize()).orElse(0))
                .addValue("restrictedFiles", req.getRestrictedFiles())
                .addValue("maxFileSize", req.getMaxFileSize())
                .addValue("allowedImages", req.getAllowedImages())
                .addValue("maxImageSize", req.getMaxImageSize())
                .addValue("isSmsAlert", Optional.ofNullable(req.getIsSmsAlert()).orElse(false))
                .addValue("isRequiredFields", Optional.ofNullable(req.getIsRequiredFields()).orElse(false))
                .addValue("isComment", Optional.ofNullable(req.getIsComment()).orElse(false))
                .addValue("isUsePeriod", Optional.ofNullable(req.getIsUsePeriod()).orElse(false))
                .addValue("isAuthorPostsView", Optional.ofNullable(req.getIsAuthorPostsView()).orElse(false))
                .addValue("isAdminDeletedView", Optional.ofNullable(req.getIsAdminDeletedView()).orElse(false))
                .addValue("listCount", Optional.ofNullable(req.getListCount()).orElse(10))
                .addValue("isShowAuthor", Optional.ofNullable(req.getIsShowAuthor()).orElse(true))
                .addValue("isShowDate", Optional.ofNullable(req.getIsShowDate()).orElse(true))
                .addValue("isSearchFieldControl", Optional.ofNullable(req.getIsSearchFieldControl()).orElse(false))
                .addValue("isTopPost", Optional.ofNullable(req.getIsTopPost()).orElse(false));
        jdbc.update(sql, p);
    }

    @Override
    public void replaceBoardFieldDefinitions(Long boardMasterIdx, List<BoardFieldDefinitionRequest> fields) {
        jdbc.update("DELETE FROM board_field_definition WHERE board_master_idx=:idx", Map.of("idx", boardMasterIdx));
        insertBoardFieldDefinitions(boardMasterIdx, fields);
    }

    @Override
    public void syncPhysicalTableWithDefinitions(Long boardMasterIdx) throws Exception {
        String boardId = findBoardIdByIdx(boardMasterIdx);
        if (boardId == null) throw new EgovBizException("boardId not found for idx=" + boardMasterIdx);
        String table = tableName(boardId);

        List<FieldDef> targetDefs = loadFieldDefs(boardMasterIdx);
        Map<String, ColumnInfo> actual = loadActualColumns(table);

        Map<String, FieldDef> targetMap = targetDefs.stream()
                .collect(Collectors.toMap(f -> f.fieldName, f -> f, (a, b) -> b, LinkedHashMap::new));

        List<String> toAdd = new ArrayList<>();
        List<String> toModify = new ArrayList<>();
        List<String> toDrop = new ArrayList<>();

        for (FieldDef f : targetDefs) {
            if (RESERVED.contains(f.fieldName)) continue;

            ColumnInfo cur = actual.get(f.fieldName);

            // wanted DDL 생성에서 터질 경우 컬럼명 포함해 원인 노출
            final String wanted;
            try {
                wanted = toColumnDDL(f);
            } catch (Exception genEx) {
                throw new EgovBizException(
                        "[DDL 생성 실패] col=" + f.fieldName + ", type=" + f.fieldType +
                                ", required=" + f.isRequired + ", default=" + f.defaultValue +
                                " / cause=" + genEx.getMessage(), genEx);
            }

            if (cur == null) {
                toAdd.add("ADD COLUMN " + wanted);
            } else {
                String normalizedTarget = normalizeDDL(wanted);
                String normalizedActual = normalizeColumn(cur);

                if (!normalizedActual.equals(normalizedTarget)) {
                    // 위험 변경 가드에서 터질 경우 현재/목표 상태를 함께 노출
                    try {
                        guardDangerousChange(cur, f);
                    } catch (Exception guardEx) {
                        throw new EgovBizException(
                                "[위험 변경 감지] col=" + f.fieldName +
                                        " / actual=" + normalizedActual +
                                        " / target=" + normalizedTarget +
                                        " / cause=" + guardEx.getMessage(), guardEx);
                    }
                    toModify.add("MODIFY COLUMN " + wanted);
                }
            }
        }

        for (String col : actual.keySet()) {
            if (RESERVED.contains(col)) continue;
            if (!targetMap.containsKey(col)) {
                toDrop.add("DROP COLUMN " + col);
            }
        }

        if (toAdd.isEmpty() && toModify.isEmpty() && toDrop.isEmpty()) return;

        String alter = "ALTER TABLE " + table + "\n  " +
                String.join(",\n  ", concat(concat(toAdd, toModify), toDrop)) + ";";

        try {
            jdbc.getJdbcTemplate().execute(alter);
        } catch (Exception e) {
            // 최종 ALTER 실패 시 전체 SQL을 포함해서 바로 재현·디버깅 가능하게
            throw new EgovBizException("[ALTER 실패] " + e.getMessage() + "\nSQL:\n" + alter, e);
        }
    }

    // ==================== 삭제 ====================

    @Override
    public void deleteBoardMaster(Long idx) {
        jdbc.update("DELETE FROM board_master WHERE idx=:idx", Map.of("idx", idx));
    }

    @Override
    public void deleteBoardFieldDefinitions(Long boardMasterIdx) {
        jdbc.update("DELETE FROM board_field_definition WHERE board_master_idx=:idx", Map.of("idx", boardMasterIdx));
    }

    @Override
    public void dropBoardTable(String boardId) {
        jdbc.getJdbcTemplate().execute("DROP TABLE IF EXISTS " + tableName(boardId));
    }

    /** [보상용] 존재하면 드롭(없으면 무시) */
    @Override
    public void safeDropBoardTable(String boardId) {
        try {
            dropBoardTable(boardId);
        } catch (Exception ignore) {
            // 보상 과정에서의 드롭 실패는 로깅만 (상위 서비스에서 처리)
        }
    }

    // ==================== 내부 헬퍼 ====================

    /** board_id → idx 조회 (없으면 예외) */
    private Long getMasterIdxByBoardId(String boardId) {
        List<Long> exists = jdbc.queryForList(
                "SELECT idx FROM board_master WHERE board_id=:bid",
                Map.of("bid", boardId), Long.class
        );
        if (!exists.isEmpty()) return exists.get(0);
        throw new IllegalStateException("board_master not found: board_id=" + boardId);
    }

    private List<FieldDef> loadFieldDefs(Long masterIdx) {
        String sql = """
            SELECT field_name, display_name, field_type, is_required, is_searchable, field_order, default_value, placeholder
            FROM board_field_definition
            WHERE board_master_idx=:idx
            ORDER BY field_order ASC, id ASC
        """;
        return jdbc.query(sql, Map.of("idx", masterIdx), (rs, n) -> {
            FieldDef f = new FieldDef();
            f.fieldName = rs.getString("field_name");
            f.displayName = rs.getString("display_name");
            f.fieldType = rs.getString("field_type");
            f.isRequired = rs.getBoolean("is_required");
            f.isSearchable = rs.getBoolean("is_searchable");
            f.fieldOrder = rs.getInt("field_order");
            f.defaultValue = rs.getString("default_value");
            f.placeholder = rs.getString("placeholder");
            return f;
        });
    }

    private Map<String, ColumnInfo> loadActualColumns(String table) {
        String sql = """
            SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = DATABASE()
              AND TABLE_NAME = :tbl
        """;
        List<ColumnInfo> list = jdbc.query(sql, Map.of("tbl", table), (rs, n) -> {
            ColumnInfo c = new ColumnInfo();
            c.columnName = rs.getString("COLUMN_NAME");
            c.dataType = rs.getString("COLUMN_TYPE").toUpperCase(Locale.ROOT);
            c.isNotNull = "NO".equalsIgnoreCase(rs.getString("IS_NULLABLE"));
            c.columnDefault = rs.getString("COLUMN_DEFAULT");
            return c;
        });
        return list.stream().collect(Collectors.toMap(ci -> ci.columnName, ci -> ci));
    }

    private String buildCreateTableDDL(String table, List<FieldDef> defs) throws Exception {
        List<String> baseCols = List.of(
                "idx BIGINT PRIMARY KEY AUTO_INCREMENT",
                "created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP",
                "updated_date DATETIME NULL",
                "is_deleted TINYINT(1) NOT NULL DEFAULT 0",
                "view_count INT NOT NULL DEFAULT 0",
                "reg_id VARCHAR(50) NULL",
                "reg_name VARCHAR(100) NULL"
        );

        // 예외 전파가 필요한 toColumnDDL 때문에 스트림 대신 루프 사용
        List<FieldDef> sorted = new ArrayList<>(defs);
        sorted.sort(Comparator.comparingInt(f -> Optional.ofNullable(f.fieldOrder).orElse(0)));

        List<String> dynCols = new ArrayList<>();
        for (FieldDef f : sorted) {
            dynCols.add(toColumnDDL(f));
        }

        return "CREATE TABLE " + table + " (\n  " +
                String.join(",\n  ", concat(baseCols, dynCols)) +
                "\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
    }

    private String toColumnDDL(FieldDef f) throws Exception {
        String raw = f.fieldType.trim();
        String dbType = DB_TYPE_MAP.getOrDefault(raw.toUpperCase(), raw); // 원문 유지
        String up = dbType.toUpperCase(Locale.ROOT);

        String req = Boolean.TRUE.equals(f.isRequired) ? " NOT NULL" : " NULL";

        // TEXT/BLOB 류는 DEFAULT 불가
        String def = "";
        if (!isTextOrBlob(up) && f.defaultValue != null && !f.defaultValue.isBlank()) {
            // ENUM 기본값 검증
            if (up.startsWith("ENUM(")) {
                String v = stripQuotes(f.defaultValue);
                if (v != null && !v.isBlank()) {
                    Set<String> enums = parseEnumValues(dbType);
                    if (!enums.contains(v)) {
                        throw new EgovBizException(
                                "ENUM 기본값이 옵션에 없습니다: " + f.fieldName +
                                        " = " + v + " / options=" + enums
                        );
                    }
                }
            }
            String lit = defaultLiteral(dbType, f.defaultValue);
            if (lit != null && !lit.isBlank()) {
                def = " DEFAULT " + lit;
            }
        }

        return f.fieldName + " " + dbType + req + def;
    }

    /** 기본값 문자열 전처리 + 타입별 리터럴 생성 */
    private String defaultLiteral(String dbType, String val) {
        if (val == null) return null;

        String up = dbType.toUpperCase(Locale.ROOT);
        String v = stripQuotes(val);

        // 함수/키워드 허용 (DATETIME 등)
        String upperV = v.toUpperCase(Locale.ROOT);
        if (upperV.equals("CURRENT_TIMESTAMP") || upperV.equals("CURRENT_TIMESTAMP()") || upperV.equals("NOW()")) {
            return v; // 함수는 따옴표 없이
        }
        if (upperV.equals("NULL")) {
            return "NULL";
        }

        // 숫자형은 따옴표 금지
        boolean isNumeric = up.startsWith("INT") || up.startsWith("BIGINT") || up.startsWith("DECIMAL")
                || up.startsWith("TINYINT") || up.startsWith("SMALLINT") || up.startsWith("FLOAT")
                || up.startsWith("DOUBLE");
        if (isNumeric) {
            return v;
        }

        // 나머지 문자형/ENUM/DATE는 따옴표
        return "'" + v.replace("'", "''") + "'";
    }

    private String stripQuotes(String v) {
        if (v == null) return null;
        String s = v.trim();
        if ((s.startsWith("'") && s.endsWith("'")) || (s.startsWith("\"") && s.endsWith("\""))) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }

    private boolean isTextOrBlob(String up) {
        return up.contains("TEXT") || up.contains("BLOB");
    }

    private Set<String> parseEnumValues(String enumType) {
        int s = enumType.indexOf('(');
        int e = enumType.lastIndexOf(')');
        if (s < 0 || e <= s) return Collections.emptySet();
        String inner = enumType.substring(s + 1, e).trim(); // 'all','writer'
        if (inner.isEmpty()) return Collections.emptySet();

        String[] tokens = inner.split(",");
        Set<String> out = new LinkedHashSet<>();
        for (String t : tokens) {
            String v = t.trim();
            if (v.startsWith("'") && v.endsWith("'") && v.length() >= 2) {
                v = v.substring(1, v.length() - 1);
            }
            out.add(v.replace("''", "'")); // 원문 유지
        }
        return out;
    }

    private void guardDangerousChange(ColumnInfo cur, FieldDef target) throws Exception {
        if (Boolean.TRUE.equals(target.isRequired) && !cur.isNotNull &&
                (target.defaultValue == null || target.defaultValue.isBlank())) {
            throw new EgovBizException("NOT NULL 변경은 기본값 없이 허용되지 않습니다: " + target.fieldName);
        }
        if (cur.dataType.startsWith("VARCHAR(") && target.fieldType.toUpperCase().startsWith("VARCHAR(")) {
            int curLen = lengthOf(cur.dataType);
            int tgtLen = lengthOf(DB_TYPE_MAP.getOrDefault(target.fieldType.toUpperCase(), target.fieldType));
            if (curLen > 0 && tgtLen > 0 && tgtLen < curLen) {
                throw new EgovBizException("컬럼 길이 축소는 금지: " + target.fieldName + " " + curLen + " -> " + tgtLen);
            }
        }
        // TODO: ENUM 축소 금지 등 추가 가드 필요 시 확장
    }

    private int lengthOf(String type) {
        try {
            int s = type.indexOf('('), e = type.indexOf(')');
            if (s > 0 && e > s) return Integer.parseInt(type.substring(s + 1, e));
            return -1;
        } catch (Exception ignore) { return -1; }
    }

    private String normalizeDDL(String ddl) {
        return ddl.replaceAll("\\s+", " ").trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeColumn(ColumnInfo c) {
        String base = c.columnName + " " + c.dataType + (c.isNotNull ? " NOT NULL" : " NULL");
        if (c.columnDefault != null) base += " DEFAULT " + c.columnDefault;
        return normalizeDDL(base);
    }

    private Boolean getNullableBoolean(ResultSet rs, String col) throws SQLException {
        boolean v = rs.getBoolean(col);
        return rs.wasNull() ? null : v;
    }

    private Integer getNullableInt(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    private static <T> List<T> concat(List<T> a, List<T> b) {
        List<T> r = new ArrayList<>(a);
        r.addAll(b);
        return r;
    }

    private static class FieldDef {
        String fieldName;
        String displayName;
        String fieldType;
        Boolean isRequired;
        Boolean isSearchable;
        Integer fieldOrder;
        String defaultValue;
        String placeholder;
    }

    private static class ColumnInfo {
        String columnName;
        String dataType;
        boolean isNotNull;
        String columnDefault;
    }
}
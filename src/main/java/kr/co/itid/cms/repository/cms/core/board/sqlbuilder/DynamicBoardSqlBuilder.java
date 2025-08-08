package kr.co.itid.cms.repository.cms.core.board.sqlbuilder;

import kr.co.itid.cms.dto.cms.core.board.response.FieldDefinitionResponse;
import kr.co.itid.cms.dto.cms.core.common.PaginationOption;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DynamicBoardSqlBuilder {

    // INSERT
    public String buildInsertQuery(String boardId, List<FieldDefinitionResponse> fields, Map<String, Object> data) {
        List<String> columns = new ArrayList<>();
        List<String> params = new ArrayList<>();

        for (FieldDefinitionResponse field : fields) {
            String fieldName = field.getFieldName();
            if (data.containsKey(fieldName)) {
                columns.add(fieldName);
                params.add(":" + fieldName);
            }
        }

        return String.format("INSERT INTO board_%s (%s) VALUES (%s)",
                boardId,
                String.join(", ", columns),
                String.join(", ", params));
    }

    // UPDATE
    public String buildUpdateQuery(String boardId, List<FieldDefinitionResponse> fields, Map<String, Object> data) {
        List<String> sets = new ArrayList<>();

        for (FieldDefinitionResponse field : fields) {
            String fieldName = field.getFieldName();
            if (data.containsKey(fieldName)) {
                sets.add(fieldName + " = :" + fieldName);
            }
        }

        return String.format("UPDATE board_%s SET %s WHERE idx = :idx", boardId, String.join(", ", sets));
    }

    // SELECT ONE
    public String buildSelectOneQuery(String boardId, List<FieldDefinitionResponse> fields) {
        String selectFields = fields.stream()
                .map(FieldDefinitionResponse::getFieldName)
                .collect(Collectors.joining(", "));

        return String.format("SELECT idx, %s FROM board_%s WHERE idx = :idx", selectFields, boardId);
    }

    // DELETE
    public String buildDeleteQuery(String boardId) {
        return String.format(
                "UPDATE board_%s SET is_deleted = true WHERE idx = :idx",
                boardId
        );
    }

    // PAGINATED SELECT
    public Page<Map<String, Object>> buildPaginatedListQuery(
            NamedParameterJdbcTemplate jdbcTemplate,
            String boardId,
            List<FieldDefinitionResponse> fields,
            SearchOption searchOption,
            PaginationOption pagination
    ) {
        // 1. SELECT 필드 구성
        String selectFields = fields.stream()
                .map(FieldDefinitionResponse::getFieldName)
                .collect(Collectors.joining(", "));

        // 2. WHERE 조건 조립
        StringBuilder where = new StringBuilder("WHERE is_deleted = false");
        Map<String, Object> params = new HashMap<>();

        if (searchOption != null) {
            // 2-1. 키워드 검색
            String keyword = searchOption.getKeyword();
            List<String> searchKeys = searchOption.getSearchKeys();

            if (keyword != null && !keyword.isBlank() && searchKeys != null && !searchKeys.isEmpty()) {
                List<String> keywordConditions = new ArrayList<>();
                for (String key : searchKeys) {
                    keywordConditions.add(key + " LIKE :keyword");
                }
                where.append(" AND (").append(String.join(" OR ", keywordConditions)).append(")");
                params.put("keyword", "%" + keyword.trim() + "%");
            }

            // 2-2. 날짜 범위 검색
            String startDate = searchOption.getStartDate();
            String endDate = searchOption.getEndDate();

            if (startDate != null && !startDate.isBlank() && endDate != null && !endDate.isBlank()) {
                where.append(" AND created_date BETWEEN :startDate AND :endDate");
                params.put("startDate", startDate);
                params.put("endDate", endDate);
            } else if (startDate != null && !startDate.isBlank()) {
                where.append(" AND created_date >= :startDate");
                params.put("startDate", startDate);
            } else if (endDate != null && !endDate.isBlank()) {
                where.append(" AND created_date <= :endDate");
                params.put("endDate", endDate);
            }
        }

        // 3. 정렬 처리 (기본: created_date DESC)
        String sortField = "created_date";
        String sortDir = "DESC";

        if (pagination.getSort() != null && !pagination.getSort().isBlank()) {
            String[] sortParts = pagination.getSort().split(",");
            String candidateSortField = sortParts[0]; //임시 변수

            if (sortParts.length > 1) {
                sortDir = sortParts[1].equalsIgnoreCase("ASC") ? "ASC" : "DESC";
            }

            // 정렬 필드 유효성 검증 (화이트리스트)
            boolean isValidSortField = fields.stream()
                    .anyMatch(f -> f.getFieldName().equals(candidateSortField));

            if (isValidSortField) {
                sortField = candidateSortField; //유효하면 할당
            } else {
                sortField = "created_date";
                sortDir = "DESC";
            }
        }

        // 4. 페이징 처리
        int page = pagination.getPage() != null ? pagination.getPage() : 0;
        int size = pagination.getSize() != null ? pagination.getSize() : 10;
        int offset = page * size;

        params.put("limit", size);
        params.put("offset", offset);

        String table = "board_" + boardId;

        // 5. 최종 쿼리 조립
        String listSql = String.format(
                "SELECT idx, %s FROM %s %s ORDER BY %s %s LIMIT :limit OFFSET :offset",
                selectFields, table, where, sortField, sortDir
        );

        String countSql = String.format(
                "SELECT COUNT(*) FROM %s %s", table, where
        );

        // 6. 실행 및 결과 매핑
        List<Map<String, Object>> content = jdbcTemplate.queryForList(listSql, params);
        int total = jdbcTemplate.queryForObject(countSql, params, Integer.class);

        return new PageImpl<>(content, pagination.toPageable(), total);
    }
}
package kr.co.itid.cms.repository.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.response.FieldDefinitionResponse;
import kr.co.itid.cms.dto.cms.core.common.PaginationOption;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.service.cms.core.template.widget.handlers.board.BoardPostSummary;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DynamicBoardDao {
    List<FieldDefinitionResponse> getFieldDefinitionsByMenuId(Long menuId) throws Exception;

    Page<Map<String, Object>> selectListByMenuId(Long menuId, SearchOption option, PaginationOption pagination) throws Exception;

    Map<String, Object> selectOneByMenuId(Long menuId, Long id) throws Exception;

    String selectRegIdByMenuId(Long menuId, Long id) throws Exception;

    void increaseViewCountByMenuId(Long menuId, Long idx) throws Exception;

    void insertByMenuId(Long menuId, Map<String, Object> data) throws Exception;

    void updateByMenuId(Long menuId, Long id, Map<String, Object> data) throws Exception;

    void deleteByMenuId(Long menuId, Long id) throws Exception;

    /**
     * 위젯용: 최근 게시글 요약 조회 (menuId 기반)
     * @param menuId 메뉴 ID
     * @param limit 조회 건수
     * @return 게시글 요약 목록
     */
    List<BoardPostSummary> findRecentSummaryByMenuId(Long menuId, int limit) throws Exception;

    /**
     * 위젯용: 최근 게시글 요약 조회 (boardId 기반)
     * @param boardId 게시판 ID (검증된)
     * @param limit 조회 건수
     * @return 게시글 요약 목록
     */
    List<BoardPostSummary> findRecentSummaryByBoardId(String boardId, int limit) throws Exception;
}

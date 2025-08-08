package kr.co.itid.cms.repository.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.response.FieldDefinitionResponse;
import kr.co.itid.cms.dto.cms.core.common.PaginationOption;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DynamicBoardDao {
    List<FieldDefinitionResponse> getFieldDefinitionsByMenuId(Long menuId) throws Exception;

    Page<Map<String, Object>> selectListByMenuId(Long menuId, SearchOption option, PaginationOption pagination) throws Exception;

    Map<String, Object> selectOneByMenuId(Long menuId, Long id) throws Exception;

    void increaseViewCountByMenuId(Long menuId, Long idx) throws Exception;

    void insertByMenuId(Long menuId, Map<String, Object> data) throws Exception;

    void updateByMenuId(Long menuId, Long id, Map<String, Object> data) throws Exception;

    void deleteByMenuId(Long menuId, Long id) throws Exception;
}

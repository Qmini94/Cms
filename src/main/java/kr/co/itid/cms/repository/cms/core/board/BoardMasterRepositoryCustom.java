package kr.co.itid.cms.repository.cms.core.board;

import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.entity.cms.core.board.BoardMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardMasterRepositoryCustom {
    Page<BoardMaster> searchByCondition(SearchOption option, Pageable pageable);
}

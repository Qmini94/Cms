package kr.co.itid.cms.repository.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.BoardSearchOption;
import kr.co.itid.cms.entity.cms.core.board.BoardMaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardMasterRepositoryCustom {
    Page<BoardMaster> searchByCondition(BoardSearchOption option, Pageable pageable);
}

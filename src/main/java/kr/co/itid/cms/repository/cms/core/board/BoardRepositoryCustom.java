package kr.co.itid.cms.repository.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.BoardSearchOption;
import kr.co.itid.cms.entity.cms.core.board.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 게시글 사용자 정의 쿼리 인터페이스
 */
public interface BoardRepositoryCustom {
    Page<Board> searchByCondition(String boardId, BoardSearchOption option, Pageable pageable);
}

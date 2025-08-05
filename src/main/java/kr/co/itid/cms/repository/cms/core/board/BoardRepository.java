package kr.co.itid.cms.repository.cms.core.board;

import kr.co.itid.cms.entity.cms.core.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long>, BoardRepositoryCustom {
}

package kr.co.itid.cms.repository.cms.core.board;

import kr.co.itid.cms.entity.cms.core.board.BoardMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardMasterRepository extends JpaRepository<BoardMaster, Long>, BoardMasterRepositoryCustom {
    Optional<BoardMaster> findByBoardId(String boardId);
}

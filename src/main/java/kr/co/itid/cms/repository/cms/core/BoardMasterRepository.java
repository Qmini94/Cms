package kr.co.itid.cms.repository.cms.core;

import kr.co.itid.cms.entity.cms.core.BoardMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardMasterRepository extends JpaRepository<BoardMaster, Long> {
    Optional<BoardMaster> findByBoardId(String boardId);
}

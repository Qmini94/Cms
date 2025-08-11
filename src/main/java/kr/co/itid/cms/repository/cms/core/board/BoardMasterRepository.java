package kr.co.itid.cms.repository.cms.core.board;

import kr.co.itid.cms.entity.cms.core.board.BoardMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface BoardMasterRepository extends JpaRepository<BoardMaster, Long>, BoardMasterRepositoryCustom {
    /**
     * boardId가 목록에 포함된 경우 is_use = true로 설정
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE BoardMaster bm SET bm.isUse = true WHERE bm.boardId IN :ids")
    int updateIsUseTrueByBoardIdIn(@Param("ids") Set<String> ids);

    /**
     * boardId가 목록에 포함되지 않은 경우 is_use = false로 설정
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE BoardMaster bm SET bm.isUse = false WHERE bm.boardId NOT IN :ids")
    int updateIsUseFalseByBoardIdNotIn(@Param("ids") Set<String> ids);
}

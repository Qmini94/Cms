package kr.co.itid.cms.repository.cms.core.content;

import io.lettuce.core.dynamic.annotation.Param;
import kr.co.itid.cms.entity.cms.core.content.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Integer> {

    List<Content> findByMenuIdxOrderByCreatedDateDesc(Integer menuIdx);

    @Query(value = """
        SELECT *
        FROM content c
        WHERE c.created_date = (
            SELECT MAX(c2.created_date)
            FROM content c2
            WHERE c2.menu_idx = c.menu_idx
        )
        ORDER BY c.menu_idx
        """, nativeQuery = true)
    List<Content> findLatestContentPerMenu();

    // 📌 [3] 수정 쿼리
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Content c SET c.isUse = false WHERE c.menuIdx = :menuIdx")
    void updateIsUseFalseByMenuIdx(@Param("menuIdx") Integer menuIdx);
}


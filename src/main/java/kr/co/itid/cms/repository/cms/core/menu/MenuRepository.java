package kr.co.itid.cms.repository.cms.core.menu;

import kr.co.itid.cms.entity.cms.core.menu.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    // 루트(드라이브) 목록
    List<Menu> findByParentIdIsNull();

    // 이름으로 루트(드라이브) 조회
    Optional<Menu> findByNameOrderByPositionAsc(String name);

    // 중복 체크/조회
    Optional<Menu> findByTypeAndName(String type, String name);
    boolean existsByTypeAndName(String type, String name);
    boolean existsByTypeAndNameAndIdNot(String type, String name, Long id);

    // 특정 pathId의 후손만(자기 자신 제외)
    @Query("SELECT m FROM Menu m WHERE m.pathId LIKE CONCAT(:pathId, '.%') ORDER BY m.pathId, m.position")
    List<Menu> findAllDescendantsByPathId(@Param("pathId") String pathId);

    // 삭제용(접두사에 이미 '.' 포함)
    @Query("SELECT m FROM Menu m WHERE m.pathId LIKE CONCAT(:pathIdWithDot, '%')")
    List<Menu> findAllDescendantsByPathIdWithDot(@Param("pathIdWithDot") String pathIdWithDot);

    // ===== pathId 갱신 =====
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Menu m SET m.pathId = :pathId WHERE m.id = :id")
    void updatePathIdById(@Param("id") Long id, @Param("pathId") String pathId);

    @Modifying(clearAutomatically = true)
    @Query("""
           UPDATE Menu m
           SET m.pathId = CONCAT(:parentPathId, '.', m.id)
           WHERE m.parentId = :parentId
           """)
    void updateChildrenPathIds(@Param("parentId") Long parentId,
                               @Param("parentPathId") String parentPathId);

    // ===== Permission 연동 =====
    // 자기 자신 제외: prefix는 "자기 path + '.'" 형태로 넣기
    @Query("SELECT m.id FROM Menu m WHERE m.pathId LIKE CONCAT(:prefix, '%')")
    List<Long> findDescendantIdsByPathPrefix(@Param("prefix") String prefix);
}
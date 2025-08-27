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

    List<Menu> findByParentIdIsNull();

    Optional<Menu> findByNameOrderByPositionAsc(String name);

    Optional<Menu> findByTypeAndName(String type, String name);

    boolean existsByTypeAndName(String type, String name);

    boolean existsByTypeAndNameAndIdNot(String type, String name, Long id);

    // 최적화: 특정 pathId의 하위 노드만 조회 (자기 자신 제외)
    @Query("SELECT m FROM Menu m WHERE m.pathId LIKE CONCAT(:pathId, '.%') ORDER BY m.pathId, m.position")
    List<Menu> findAllDescendantsByPathId(@Param("pathId") String pathId);

    @Query("SELECT m FROM Menu m WHERE m.pathId LIKE CONCAT(:pathIdWithDot, '%')")
    List<Menu> findAllDescendantsByPathIdWithDot(@Param("pathIdWithDot") String pathIdWithDot);

    // 배치 pathId 업데이트 (성능 개선)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Menu m SET m.pathId = CASE " +
            "WHEN m.id = :rootId THEN :rootPathId " +
            "ELSE CONCAT(:rootPathId, '.', m.id) END " +
            "WHERE m.id IN :ids")
    void batchUpdatePathIds(@Param("rootId") Long rootId,
                            @Param("rootPathId") String rootPathId,
                            @Param("ids") List<Long> ids);

    // 단일 pathId 업데이트 (기존 호환성)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Menu m SET m.pathId = :pathId WHERE m.id = :id")
    void updatePathIdById(@Param("id") Long id, @Param("pathId") String pathId);

    // 특정 parent 아래의 직접 자식들만 조회 (위치순 정렬)
    @Query("SELECT m FROM Menu m WHERE m.parentId = :parentId ORDER BY m.position ASC")
    List<Menu> findDirectChildrenByParentId(@Param("parentId") Long parentId);

    // Permission 서비스에서 사용하는 메서드들
    @Query("select m.pathId from Menu m where m.id = :id")
    String findPathIdById(@Param("id") Long id);

    // 자기 자신 제외: prefix를 "자기 path + '.'" 로 넣으므로 후손만 매칭됨
    @Query("select m.id from Menu m where m.pathId like concat(:prefix, '%')")
    List<Long> findDescendantIdsByPathPrefix(@Param("prefix") String prefix);
}
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

    List<Menu> findByParentIdOrderByPositionAsc(Long parentId);

    Optional<Menu> findByNameOrderByPositionAsc(String name);

    Optional<Menu> findById(Long id);

    Optional<Menu> findByTypeAndName(String type, String name);

    boolean existsByTypeAndName(String type, String name);

    boolean existsByTypeAndNameAndIdNot(String type, String name, Long id);

    @Query("SELECT m FROM Menu m WHERE m.pathId LIKE CONCAT(:pathId, '.%')")
    List<Menu> findAllDescendantsByPathId(@Param("pathId") String pathId);

    @Modifying(clearAutomatically = true)
    @Query("update Menu m set m.pathId = :pathId where m.id = :id")
    void updatePathIdById(@Param("id") Long id, @Param("pathId") String pathId);
}

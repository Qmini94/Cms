package kr.co.itid.cms.repository.cms.core.menu;

import kr.co.itid.cms.entity.cms.core.menu.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByParentIdIsNull();

    Optional<Menu> findByNameOrderByPositionAsc(String name);

    List<Menu> findByParentIdOrderByPositionAsc(Long parentId);

    Optional<Menu> findById(Long id);

    Optional<Menu> findByTypeAndName(String type, String name);

    @Query("SELECT m FROM Menu m WHERE m.pathId LIKE CONCAT(:pathId, '.%')")
    List<Menu> findAllDescendantsByPathId(@Param("pathId") String pathId);
}

package kr.co.itid.cms.repository.cms;

import kr.co.itid.cms.entity.cms.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findAllByOrderByLevelAscLeftAsc();

    List<Menu> findByParentIdOrderByLevelAscLeftAsc(Long parentId);
}

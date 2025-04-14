package kr.co.itid.cms.repository.cms;

import kr.co.itid.cms.entity.cms.base.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByParentIdIsNull();

    Optional<Menu> findByNameOrderByPositionAsc(String name);

    // 부모 ID로 하위 메뉴 조회
    List<Menu> findByParentIdOrderByPositionAsc(Long parentId);

    String findPathIdById(Long id);
}

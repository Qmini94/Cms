package kr.co.itid.cms.repository.cms;

import kr.co.itid.cms.entity.cms.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByType(String type);

    // 특정 id가 일치하는 메뉴 조회
    Optional<Menu> findById(Long id);

    // 부모 ID로 하위 메뉴 조회
    List<Menu> findByParentIdOrderByLevelAscLeftAsc(Long parentId);
}

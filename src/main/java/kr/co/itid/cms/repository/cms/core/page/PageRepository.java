package kr.co.itid.cms.repository.cms.core.page;

import kr.co.itid.cms.entity.cms.core.page.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PageRepository extends JpaRepository<Page, Long> {
    Optional<Page> findBySite_IdxAndPath(Long siteIdx, String path);
}

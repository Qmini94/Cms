package kr.co.itid.cms.repository.cms.core;

import kr.co.itid.cms.entity.cms.core.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
}

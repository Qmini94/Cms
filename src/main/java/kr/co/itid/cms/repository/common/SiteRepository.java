package kr.co.itid.cms.repository.common;

import kr.co.itid.cms.entity.cms.core.site.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
    Optional<Site> findBySiteHostName(String siteHostName);
}

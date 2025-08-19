package kr.co.itid.cms.repository.cms.core.site;

import kr.co.itid.cms.entity.cms.core.site.Site;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
    List<Site> findByIsDeletedFalse();

    Optional<Site> findBySiteHostName(String siteHostName);

    Optional<Site> findByIdx(Long siteIdx);

    boolean existsBySiteHostName(String siteHostName);
}

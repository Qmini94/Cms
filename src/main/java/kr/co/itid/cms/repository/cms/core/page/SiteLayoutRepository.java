package kr.co.itid.cms.repository.cms.core.page;

import kr.co.itid.cms.entity.cms.core.page.SiteLayout;
import kr.co.itid.cms.entity.cms.core.site.Site;
import kr.co.itid.cms.enums.LayoutKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteLayoutRepository extends JpaRepository<SiteLayout, Long> {

    // published 최신
    Optional<SiteLayout> findFirstBySiteAndKindAndIsPublishedTrueOrderByVersionDesc(Site site, LayoutKind kind);

    // draft 최신
    Optional<SiteLayout> findFirstBySiteAndKindAndIsPublishedFalseOrderByVersionDesc(Site site, LayoutKind kind);

    // 특정 버전
    Optional<SiteLayout> findBySiteAndKindAndVersion(Site site, LayoutKind kind, Integer version);

    // 같은 site/kind 에서 특정 version보다 큰 published 존재여부 등도 필요하면 추가
}

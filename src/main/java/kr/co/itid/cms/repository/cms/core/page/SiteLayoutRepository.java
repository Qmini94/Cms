package kr.co.itid.cms.repository.cms.core.page;

import kr.co.itid.cms.entity.cms.core.page.SiteLayout;
import kr.co.itid.cms.enums.LayoutKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteLayoutRepository extends JpaRepository<SiteLayout, Long> {
    Optional<SiteLayout> findFirstBySite_IdxAndKindAndIsPublishedTrueOrderByUpdatedAtDesc(Long siteId, LayoutKind kind);
}

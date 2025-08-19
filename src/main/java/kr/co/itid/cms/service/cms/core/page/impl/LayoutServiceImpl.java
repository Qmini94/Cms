package kr.co.itid.cms.service.cms.core.page.impl;

import kr.co.itid.cms.enums.LayoutKind;
import kr.co.itid.cms.repository.cms.core.page.SiteLayoutRepository;
import kr.co.itid.cms.repository.cms.core.site.SiteRepository;
import kr.co.itid.cms.service.cms.core.page.LayoutService;
import kr.co.itid.cms.entity.cms.core.page.SiteLayout;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LayoutServiceImpl implements LayoutService {

    private final SiteRepository siteRepo;
    private final SiteLayoutRepository layoutRepo;

    @Override
    @Transactional(readOnly = true)
    public String getPublishedHtml(String site, LayoutKind kind) {
        Long siteIdx = resolveSiteIdx(site);
        String html = layoutRepo
                .findFirstBySite_IdxAndKindAndIsPublishedTrueOrderByUpdatedAtDesc(siteIdx, kind)
                .map(SiteLayout::getHtml)
                .orElse("<main><cms-slot name=\"content\"></cms-slot></main>");
        return html;
    }

    @Override
    @Transactional(readOnly = true)
    public String getPublishedCss(String site, LayoutKind kind) {
        Long siteIdx = resolveSiteIdx(site);
        return layoutRepo
                .findFirstBySite_IdxAndKindAndIsPublishedTrueOrderByUpdatedAtDesc(siteIdx, kind)
                .map(SiteLayout::getCss)
                .orElse("");
    }

    private Long resolveSiteIdx(String siteKey) {
        return siteRepo.findBySiteHostName(siteKey)
                .orElseThrow(() -> new IllegalArgumentException("Unknown site: " + siteKey))
                .getIdx();
    }
}

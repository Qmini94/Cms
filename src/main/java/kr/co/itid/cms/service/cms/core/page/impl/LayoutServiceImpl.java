package kr.co.itid.cms.service.cms.core.page.impl;

import kr.co.itid.cms.dto.cms.core.render.request.LayoutSaveRequest;
import kr.co.itid.cms.entity.cms.core.site.Site;
import kr.co.itid.cms.enums.LayoutKind;
import kr.co.itid.cms.repository.cms.core.page.SiteLayoutRepository;
import kr.co.itid.cms.repository.cms.core.site.SiteRepository;
import kr.co.itid.cms.service.cms.core.page.LayoutService;
import kr.co.itid.cms.entity.cms.core.page.SiteLayout;
import kr.co.itid.cms.util.HtmlSanitizerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LayoutServiceImpl implements LayoutService {

    private final SiteRepository siteRepository;
    private final SiteLayoutRepository layoutRepository;

    @Override
    @Transactional(readOnly = true)
    public String getPublishedHtml(String site, LayoutKind kind) {
        Long siteIdx = resolveSiteIdx(site);
        String html = layoutRepository
                .findFirstBySite_IdxAndKindAndIsPublishedTrueOrderByUpdatedAtDesc(siteIdx, kind)
                .map(SiteLayout::getHtml)
                .orElse("<main><cms-slot name=\"content\"></cms-slot></main>");
        return html;
    }

    @Override
    @Transactional
    public boolean save(LayoutSaveRequest req) {
        Site site = siteRepository.findByIdx(req.getSiteIdx())
                .orElseThrow(() -> new IllegalArgumentException("Unknown site idx"));

        String safeHtml = HtmlSanitizerUtil.sanitizeLayout(req.getHtml());

        if (Boolean.TRUE.equals(req.getPublishNow())) {
            layoutRepository.findFirstBySite_IdxAndKindAndIsPublishedTrueOrderByUpdatedAtDesc(site.getIdx(), req.getKind())
                    .ifPresent(old -> {
                        old.setIsPublished(false);
                        layoutRepository.save(old);
                    });
        }

        SiteLayout layout = new SiteLayout();
        layout.setSite(site);
        layout.setKind(req.getKind());
        layout.setHtml(safeHtml);
        layout.setVersion(req.getVersion() != null ? req.getVersion() : 1);
        layout.setIsPublished(Boolean.TRUE.equals(req.getPublishNow()));
        layout.setUpdatedAt(LocalDateTime.now());

        layoutRepository.save(layout);
        return true;
    }

    private Long resolveSiteIdx(String siteKey) {
        return siteRepository.findBySiteHostName(siteKey)
                .orElseThrow(() -> new IllegalArgumentException("Unknown site: " + siteKey))
                .getIdx();
    }
}

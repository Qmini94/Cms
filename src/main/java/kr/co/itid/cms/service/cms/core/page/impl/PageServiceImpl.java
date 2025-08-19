package kr.co.itid.cms.service.cms.core.page.impl;

import kr.co.itid.cms.repository.cms.core.page.PageRepository;
import kr.co.itid.cms.repository.cms.core.site.SiteRepository;
import kr.co.itid.cms.service.cms.core.page.PageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {

    private final SiteRepository siteRepo;
    private final PageRepository pageRepo;

    @Override
    @Transactional(readOnly = true)
    public String getCurrentHtml(String site, String path) {
        Long siteIdx = siteRepo.findBySiteHostName(site)
                .orElseThrow(() -> new IllegalArgumentException("Unknown site: " + site))
                .getIdx();

        return pageRepo.findBySite_IdxAndPath(siteIdx, path)
                .map(p -> {
                    var v = p.getCurrentVersion();
                    return (v != null) ? v.getContentHtml() : "";
                })
                .orElse("");
    }
}

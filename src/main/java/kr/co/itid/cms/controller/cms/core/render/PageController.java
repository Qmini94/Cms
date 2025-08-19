package kr.co.itid.cms.controller.cms.core.render;

import kr.co.itid.cms.dto.cms.core.render.request.PageSaveRequest;
import kr.co.itid.cms.entity.cms.core.page.Page;
import kr.co.itid.cms.entity.cms.core.page.PageVersion;
import kr.co.itid.cms.entity.cms.core.site.Site;
import kr.co.itid.cms.repository.cms.core.page.PageRepository;
import kr.co.itid.cms.repository.cms.core.page.PageVersionRepository;
import kr.co.itid.cms.repository.cms.core.site.SiteRepository;
import kr.co.itid.cms.util.HtmlSanitizerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/back-api/admin/pages")
@RequiredArgsConstructor
public class PageController {

    private final SiteRepository siteRepo;
    private final PageRepository pageRepo;
    private final PageVersionRepository versionRepo;

    @PostMapping(value="/save", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<?> save(@RequestBody PageSaveRequest req) {
        Site site = siteRepo.findBySiteHostName(req.getSite())
                .orElseThrow(() -> new IllegalArgumentException("Unknown site"));

        // 페이지 upsert
        Page page = pageRepo.findBySite_IdxAndPath(site.getIdx(), req.getPath())
                .orElseGet(() -> {
                    Page p = new Page();
                    p.setSite(site);
                    p.setPath(req.getPath());
                    return p;
                });

        // 저장 시에도 페이지 정책으로 1차 Sanitizer
        String safeContent = HtmlSanitizerUtil.sanitizePage(req.getContentHtml());

        PageVersion v = new PageVersion();
        v.setPage(page);
        v.setContentHtml(safeContent);
        v.setCreatedBy(req.getAuthor());
        versionRepo.save(v);

        if (Boolean.TRUE.equals(req.getPublishNow())) {
            page.setCurrentVersion(v);
        }
        pageRepo.save(page);

        // 렌더 캐시 무효화(site|path)
        return ResponseEntity.ok(Map.of("code", 200));
    }
}

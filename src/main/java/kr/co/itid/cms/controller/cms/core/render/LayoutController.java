package kr.co.itid.cms.controller.cms.core.render;

import kr.co.itid.cms.dto.cms.core.render.request.LayoutSaveRequest;
import kr.co.itid.cms.entity.cms.core.page.SiteLayout;
import kr.co.itid.cms.entity.cms.core.site.Site;
import kr.co.itid.cms.repository.cms.core.page.SiteLayoutRepository;
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
@RequestMapping("/back-api/admin/layouts")
@RequiredArgsConstructor
public class LayoutController {

    private final SiteRepository siteRepo;
    private final SiteLayoutRepository layoutRepo;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<?> save(@RequestBody LayoutSaveRequest req) {
        Site site = siteRepo.findBySiteHostName(req.getSite())
                .orElseThrow(() -> new IllegalArgumentException("Unknown site"));

        // 저장 시에도 1차 Sanitizer(레이아웃 정책) 적용 권장
        String safeHtml = HtmlSanitizerUtil.sanitizeLayout(req.getHtml());
        String safeCss  = req.getCss(); // CSS는 별도 정책(원하면 sanitize)

        // 새 버전 행
        SiteLayout layout = new SiteLayout();
        layout.setSite(site);
        layout.setKind(req.getKind());
        layout.setHtml(safeHtml);
        layout.setCss(safeCss);
        layout.setVersion(req.getVersion() != null ? req.getVersion() : 1);
        layout.setIsPublished(Boolean.TRUE.equals(req.getPublishNow()));
        layout.setUpdatedAt(java.time.LocalDateTime.now());

        // 퍼블리시라면 기존 공개본 unpublish
        if (Boolean.TRUE.equals(req.getPublishNow())) {
            layoutRepo.findFirstBySite_IdxAndKindAndIsPublishedTrueOrderByUpdatedAtDesc(site.getIdx(), req.getKind())
                    .ifPresent(old -> { old.setIsPublished(false); layoutRepo.save(old); });
        }
        layoutRepo.save(layout);

        // 렌더 캐시 무효화
        // @CacheEvict(cacheNames="rendered", allEntries=true) 또는 site|kind 기준 부분무효화
        return ResponseEntity.ok().body(Map.of("code", 200));
    }
}
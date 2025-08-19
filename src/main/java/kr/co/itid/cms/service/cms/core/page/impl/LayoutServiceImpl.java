package kr.co.itid.cms.service.cms.core.page.impl;

import kr.co.itid.cms.dto.cms.core.render.request.LayoutPreviewRequest;
import kr.co.itid.cms.dto.cms.core.render.request.LayoutSaveRequest;
import kr.co.itid.cms.entity.cms.core.site.Site;
import kr.co.itid.cms.enums.LayoutKind;
import kr.co.itid.cms.repository.cms.core.page.SiteLayoutRepository;
import kr.co.itid.cms.repository.cms.core.site.SiteRepository;
import kr.co.itid.cms.service.cms.core.page.LayoutService;
import kr.co.itid.cms.entity.cms.core.page.SiteLayout;
import kr.co.itid.cms.service.cms.core.page.WidgetService;
import kr.co.itid.cms.util.HtmlComposerUtil;
import kr.co.itid.cms.util.HtmlSanitizerUtil;
import kr.co.itid.cms.util.WidgetCtx;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LayoutServiceImpl implements LayoutService {

    private final WidgetService widgetService;
    private final SiteRepository siteRepository;
    private final SiteLayoutRepository layoutRepository;

    @Override
    public String renderPreview(LayoutPreviewRequest req, HttpServletRequest httpReq) {
        // 1) 사이트 존재 체크 (권한/폐쇄망 환경 고려)
        Site site = siteRepository.findByIdx(req.getSiteIdx())
                .orElseThrow(() -> new IllegalArgumentException("Unknown site idx"));

        LayoutKind kind = req.getKind();

        // 2) 저장시와 동일하게 1차/2차 Sanitizer 적용
        String safeLayout = HtmlSanitizerUtil.sanitizeLayout(req.getLayoutHtml());
        String safePage   = HtmlSanitizerUtil.sanitizePage(
                req.getPageHtml() != null ? req.getPageHtml() : "<p>미리보기</p>"
        );

        // 3) 위젯 치환
        String renderedBody = widgetService.render(safePage, new WidgetCtx(site.getSiteName(), req.getPath(), httpReq));

        // 4) 슬롯 합성
        String body = HtmlComposerUtil.compose(safeLayout, Map.of("content", renderedBody));

        // 5) head 자산 태그 생성 (미리보기이므로 ‘허용 목록’만 주입)
        String inlineCss = (req.getInlineCss() != null && !req.getInlineCss().isEmpty()) ? "<style>"+req.getInlineCss()+"</style>" : "";
        String links = buildLinks(req.getCssUrls());
        String scripts = buildScripts(req.getJsUrls());

        // 6) 완성 HTML 반환
        return """
          <!doctype html><html><head><meta charset="utf-8">
          <meta name="viewport" content="width=device-width,initial-scale=1">%s%s%s
          <title>%s - PREVIEW</title></head><body>%s</body></html>
          """.formatted(inlineCss, links, scripts, site.getSiteName().toUpperCase(), body);
    }

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

    private String buildLinks(List<String> urls) {
        if (urls == null || urls.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String u : urls) {
            if (u == null || u.isBlank()) continue;
            sb.append("<link rel=\"stylesheet\" href=\"").append(u).append("\">");
        }
        return sb.toString();
    }

    private String buildScripts(List<String> urls) {
        if (urls == null || urls.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String u : urls) {
            if (u == null || u.isBlank()) continue;
            sb.append("<script src=\"").append(u).append("\"></script>");
        }
        return sb.toString();
    }
}

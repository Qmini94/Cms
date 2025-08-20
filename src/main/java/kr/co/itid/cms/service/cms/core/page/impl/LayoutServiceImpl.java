package kr.co.itid.cms.service.cms.core.page.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.itid.cms.dto.cms.core.render.LayoutResolveResult;
import kr.co.itid.cms.dto.cms.core.render.request.LayoutPreviewRequest;
import kr.co.itid.cms.dto.cms.core.render.request.LayoutSaveRequest;
import kr.co.itid.cms.entity.cms.core.page.SiteLayout;
import kr.co.itid.cms.entity.cms.core.site.Site;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.enums.LayoutKind;
import kr.co.itid.cms.repository.cms.core.page.SiteLayoutRepository;
import kr.co.itid.cms.repository.cms.core.site.SiteRepository;
import kr.co.itid.cms.service.cms.core.page.LayoutService;
import kr.co.itid.cms.service.cms.core.page.WidgetService;
import kr.co.itid.cms.util.HtmlComposerUtil;
import kr.co.itid.cms.util.HtmlSanitizerUtil;
import kr.co.itid.cms.util.LoggingUtil;
import kr.co.itid.cms.util.WidgetCtx;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("layoutService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LayoutServiceImpl extends EgovAbstractServiceImpl implements LayoutService {

    private final LoggingUtil loggingUtil;
    private final SiteRepository siteRepository;
    private final SiteLayoutRepository siteLayoutRepository;
    private final WidgetService widgetService;
    private final ObjectMapper objectMapper;

    @Override
    public LayoutResolveResult resolveForRender(String siteCode, String path, Long layoutVersion, String mode) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE,
                "layout.resolve attempt: site=" + siteCode + ", path=" + path + ", ver=" + layoutVersion + ", mode=" + mode);
        try {
            String normPath = (path == null || path.isBlank()) ? "/" : path.trim();
            String normMode = (mode == null || mode.isBlank()) ? "published" : mode.trim();

            Site site = siteRepository.findBySiteHostName(siteCode)
                    .orElseThrow(() -> processException("layout.resolve.site.notfound"));

            LayoutKind kind = resolveKind(normPath);

            SiteLayout layout;
            if (layoutVersion != null) {
                layout = siteLayoutRepository
                        .findBySiteAndKindAndVersion(site, kind, layoutVersion.intValue())
                        .orElseThrow(() -> processException("layout.resolve.version.notfound"));
            } else if ("published".equalsIgnoreCase(normMode)) {
                layout = siteLayoutRepository
                        .findFirstBySiteAndKindAndIsPublishedTrueOrderByVersionDesc(site, kind)
                        .orElseThrow(() -> processException("layout.resolve.published.notfound"));
            } else {
                layout = siteLayoutRepository
                        .findFirstBySiteAndKindAndIsPublishedFalseOrderByVersionDesc(site, kind)
                        .orElseThrow(() -> processException("layout.resolve.draft.notfound"));
            }

            List<String> cssUrls = parseJsonArray(layout.getExtraCssUrlsJson());
            List<String> jsUrls  = parseJsonArray(layout.getExtraJsUrlsJson());

            LayoutResolveResult result = LayoutResolveResult.builder()
                    .htmlTemplate(layout.getHtml())
                    .cssUrl(cssUrls)
                    .jsUrl(jsUrls)
                    .kind(layout.getKind())
                    .layoutId(layout.getIdx())
                    .version(layout.getVersion())
                    .build();

            loggingUtil.logSuccess(Action.RETRIEVE, "layout.resolve ok: id=" + layout.getIdx());
            return result;

        } catch (DataAccessException dae) {
            loggingUtil.logFail(Action.RETRIEVE, "layout.resolve db error: " + dae.getMessage());
            throw processException("layout.resolve.db.error", dae);
        } catch (EgovBizException ebe) {
            throw ebe;
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "layout.resolve unexpected: " + e.getMessage());
            throw processException("layout.resolve.unexpected", e);
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public String renderPreview(LayoutPreviewRequest req) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE,
                "layout.preview attempt: siteIdx=" + req.getSiteIdx() + ", path=" + req.getPath() + ", kind=" + req.getKind());
        try {
            // DTO에서 정규화/검증 끝난 값 사용
            Long siteIdx = req.getSiteIdx();
            LayoutKind kind = req.getKind();
            String path = req.normalizedPath();

            Site site = siteRepository.findById(siteIdx)
                    .orElseThrow(() -> processException("layout.preview.site.notfound"));

            String siteIdent = site.getSiteHostName(); // or site.getCode()
            WidgetCtx ctx = widgetService.buildContext(siteIdent, path, kind);

            String layoutHtml = req.safeLayoutHtml();
            String pageHtml   = req.safePageHtml();
            List<String> cssUrls = req.normalizedCssUrls();
            List<String> jsUrls  = req.normalizedJsUrls();
            String inlineCss     = req.safeInlineCss();

            String merged = HtmlComposerUtil.composeLayout(layoutHtml, pageHtml, ctx);
            String withWidgets = widgetService.render(merged, ctx);
            String withAssets = HtmlComposerUtil.injectHeadAssets(withWidgets, cssUrls, jsUrls, inlineCss);

            String sanitized = HtmlSanitizerUtil.clean(withAssets);
            String finalHtml = HtmlComposerUtil.applyAccessibilityFix(sanitized);

            loggingUtil.logSuccess(Action.RETRIEVE, "layout.preview ok");
            return finalHtml;

        } catch (DataAccessException dae) {
            loggingUtil.logFail(Action.RETRIEVE, "layout.preview db error: " + dae.getMessage());
            throw processException("layout.preview.db.error", dae);
        } catch (EgovBizException ebe) {
            throw ebe;
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "layout.preview unexpected: " + e.getMessage());
            throw processException("layout.preview.unexpected", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void save(LayoutSaveRequest req) throws Exception {
        loggingUtil.logAttempt(Action.CREATE,
                "layout.save attempt: siteIdx=" + req.getSiteIdx() + ", kind=" + req.getKind() + ", publishNow=" + req.getPublishNow());
        try {
            Site site = siteRepository.findById(req.getSiteIdx())
                    .orElseThrow(() -> processException("layout.save.site.notfound"));

            LayoutKind kind = req.getKind();

            Integer latestPubVer = siteLayoutRepository
                    .findFirstBySiteAndKindAndIsPublishedTrueOrderByVersionDesc(site, kind)
                    .map(SiteLayout::getVersion).orElse(0);

            Integer latestDraftVer = siteLayoutRepository
                    .findFirstBySiteAndKindAndIsPublishedFalseOrderByVersionDesc(site, kind)
                    .map(SiteLayout::getVersion).orElse(0);

            int nextVersion = Math.max(latestPubVer, latestDraftVer) + 1;

            SiteLayout layout = new SiteLayout();
            layout.setSite(site);
            layout.setKind(kind);
            layout.setHtml(req.getHtml());
            layout.setVersion(nextVersion);
            layout.setIsPublished(req.isPublishNow());
            layout.setExtraCssUrlsJson(toJsonArray(req.normalizedCssUrls()));
            layout.setExtraJsUrlsJson(toJsonArray(req.normalizedJsUrls()));

            siteLayoutRepository.save(layout);

            loggingUtil.logSuccess(Action.CREATE, "layout.save ok: siteIdx=" + req.getSiteIdx());

        } catch (Exception e) {
            loggingUtil.logFail(Action.CREATE, "layout.save unexpected: " + e.getMessage());
            throw processException("layout.save.unexpected", e);
        }
    }

    private LayoutKind resolveKind(String path) {
        return (path == null || "/".equals(path.trim())) ? LayoutKind.MAIN : LayoutKind.SUB;
    }

    private List<String> parseJsonArray(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
        } catch (Exception ignore) {
            return List.of(json.trim());
        }
    }

    private String toJsonArray(List<String> urls) {
        try {
            return objectMapper.writeValueAsString(urls == null ? List.of() : urls);
        } catch (Exception e) {
            return "[]";
        }
    }
}
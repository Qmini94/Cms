package kr.co.itid.cms.service.cms.core.template.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.itid.cms.dto.cms.core.template.LayoutResolveResult;
import kr.co.itid.cms.dto.cms.core.template.request.LayoutPreviewRequest;
import kr.co.itid.cms.dto.cms.core.template.request.LayoutSaveRequest;
import kr.co.itid.cms.dto.cms.core.template.response.LayoutResponse;
import kr.co.itid.cms.entity.cms.core.template.SiteLayout;
import kr.co.itid.cms.entity.cms.core.site.Site;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.enums.LayoutKind;
import kr.co.itid.cms.repository.cms.core.template.SiteLayoutRepository;
import kr.co.itid.cms.repository.cms.core.site.SiteRepository;
import kr.co.itid.cms.service.cms.core.template.LayoutService;
import kr.co.itid.cms.service.cms.core.template.WidgetService;
import kr.co.itid.cms.service.cms.core.template.widget.model.WidgetCtx;
import kr.co.itid.cms.util.HtmlComposerUtil;
import kr.co.itid.cms.util.HtmlSanitizerUtil;
import kr.co.itid.cms.util.LoggingUtil;
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

    /**
     * (편집용 조회) 이제는 published=true 한 건만 반환.
     */
    @Override
    public LayoutResponse getTemplateBySiteIdxAndKind(Long siteIdx, LayoutKind kind) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE,
                "layout.get attempt: siteIdx=" + siteIdx + ", kind=" + kind);
        try {
            final Site site = siteRepository.findById(siteIdx)
                    .orElseThrow(() -> processException("layout.get.site.notfound"));

            final SiteLayout layout = siteLayoutRepository
                    .findFirstBySiteAndKindAndIsPublishedTrueOrderByVersionDesc(site, kind)
                    .orElseThrow(() -> processException("layout.get.published.notfound"));

            final List<String> cssUrls = parseJsonArray(layout.getExtraCssUrlsJson());
            final List<String> jsUrls  = parseJsonArray(layout.getExtraJsUrlsJson());

            LayoutResponse resp = LayoutResponse.builder()
                    .siteIdx(siteIdx)
                    .kind(layout.getKind())
                    .layoutId(layout.getIdx())
                    .version(layout.getVersion())
                    .published(Boolean.TRUE.equals(layout.getIsPublished()))
                    .html(layout.getHtml())
                    .cssUrls(cssUrls)
                    .jsUrls(jsUrls)
                    .build();

            loggingUtil.logSuccess(Action.RETRIEVE,
                    "layout.get ok: id=" + layout.getIdx() + ", ver=" + layout.getVersion());
            return resp;

        } catch (DataAccessException dae) {
            loggingUtil.logFail(Action.RETRIEVE, "layout.get db error: " + dae.getMessage());
            throw processException("layout.get.db.error", dae);
        } catch (EgovBizException ebe) {
            throw ebe;
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "layout.get unexpected: " + e.getMessage());
            throw processException("layout.get.unexpected", e);
        }
    }

    /**
     * (프론트 렌더용) 간소화: version/mode 무시하고 published=true 한 건만 사용.
     * TODO: 이후 요구 시 version/mode 분기 복원.
     */
    @Override
    public LayoutResolveResult resolveForRender(String siteCode, String path, Long layoutVersion, String mode) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE,
                "layout.resolve attempt: site=" + siteCode + ", path=" + path);
        try {
            String normPath = (path == null || path.isBlank()) ? "/" : path.trim();

            Site site = siteRepository.findBySiteHostName(siteCode)
                    .orElseThrow(() -> processException("layout.resolve.site.notfound"));

            LayoutKind kind = resolveKind(normPath);

            SiteLayout layout = siteLayoutRepository
                    .findFirstBySiteAndKindAndIsPublishedTrueOrderByVersionDesc(site, kind)
                    .orElseThrow(() -> processException("layout.resolve.published.notfound"));

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

    /**
     * 저장 시: 동일 (site, kind) 기존 모두 언퍼블리시 → 새 레코드만 published=true
     */
    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void save(LayoutSaveRequest req) throws Exception {
        loggingUtil.logAttempt(Action.CREATE,
                "layout.save attempt: siteIdx=" + req.getSiteIdx() + ", kind=" + req.getKind());
        try {
            Site site = siteRepository.findById(req.getSiteIdx())
                    .orElseThrow(() -> processException("layout.save.site.notfound"));

            LayoutKind kind = req.getKind();

            // 다음 버전 계산(현행 데이터 기준 최대값 + 1)
            Integer latestPubVer = siteLayoutRepository
                    .findFirstBySiteAndKindAndIsPublishedTrueOrderByVersionDesc(site, kind)
                    .map(SiteLayout::getVersion).orElse(0);
            Integer latestDraftVer = siteLayoutRepository
                    .findFirstBySiteAndKindAndIsPublishedFalseOrderByVersionDesc(site, kind)
                    .map(SiteLayout::getVersion).orElse(0);
            int nextVersion = Math.max(latestPubVer, latestDraftVer) + 1;

            // 1) 기존 published 전부 언퍼블리시(폴백 루프 방식)
            unpublishAll(site, kind);

            // 2) 새 레코드 저장(+ published=true 한 건 유지)
            SiteLayout layout = new SiteLayout();
            layout.setSite(site);
            layout.setKind(kind);
            layout.setHtml(req.getHtml());
            layout.setVersion(nextVersion);
            layout.setIsPublished(true); // 규칙: 새로 저장되는 이 한 건만 true
            layout.setExtraCssUrlsJson(toJsonArray(req.normalizedCssUrls()));
            layout.setExtraJsUrlsJson(toJsonArray(req.normalizedJsUrls()));

            siteLayoutRepository.save(layout);

            loggingUtil.logSuccess(Action.CREATE, "layout.save ok: siteIdx=" + req.getSiteIdx()
                    + ", id=" + layout.getIdx() + ", ver=" + nextVersion);

        } catch (DataAccessException dae) {
            loggingUtil.logFail(Action.CREATE, "layout.save db error: " + dae.getMessage());
            throw processException("layout.save.db.error", dae);
        } catch (EgovBizException ebe) {
            throw ebe;
        } catch (Exception e) {
            loggingUtil.logFail(Action.CREATE, "layout.save unexpected: " + e.getMessage());
            throw processException("layout.save.unexpected", e);
        }
    }

    /** path로 MAIN/SUB 간단 판정 */
    private LayoutKind resolveKind(String path) {
        return (path == null || "/".equals(path.trim())) ? LayoutKind.MAIN : LayoutKind.SUB;
    }

    /** (site, kind) 조합의 published=true 전부를 false로 전환 */
    @Transactional(rollbackFor = EgovBizException.class)
    public void unpublishAll(Site site, LayoutKind kind) {
        while (true) {
            java.util.Optional<SiteLayout> opt =
                    siteLayoutRepository.findFirstBySiteAndKindAndIsPublishedTrueOrderByVersionDesc(site, kind);
            if (opt.isEmpty()) {
                break;
            }
            SiteLayout current = opt.get();
            if (Boolean.FALSE.equals(current.getIsPublished())) {
                break; // 안전 가드
            }
            current.setIsPublished(false);
            siteLayoutRepository.save(current);
        }
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
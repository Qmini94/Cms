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
import java.util.Objects;

@Service("layoutService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LayoutServiceImpl extends EgovAbstractServiceImpl implements LayoutService {

    private final LoggingUtil loggingUtil;
    private final SiteRepository siteRepository;
    private final SiteLayoutRepository siteLayoutRepository;
    private final WidgetService widgetService; // 미리보기에서도 위젯 치환 필요

    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON ←→ List 파싱/직렬화

    @Override
    public LayoutResolveResult resolveForRender(String siteCode, String path, Long layoutVersion, String mode) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE,
                "layout.resolve attempt: site=" + siteCode + ", path=" + path + ", ver=" + layoutVersion + ", mode=" + mode);
        try {
            final String normPath = (path == null || path.isBlank()) ? "/" : path.trim();
            final String normMode = (mode == null || mode.isBlank()) ? "published" : mode.trim();

            Site site = siteRepository.findBySiteHostName(siteCode).orElseThrow(
                    () -> processException("layout.resolve.site.notfound"));

            LayoutKind kind = resolveKind(normPath);

            final SiteLayout layout;
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
            // 0) 입력 정규화 / 필수 체크 (DTO에 @NotNull 있으나 방어)
            final Long siteIdx = req.getSiteIdx();
            final LayoutKind kind = req.getKind();
            final String path = (req.getPath() == null || req.getPath().isBlank()) ? "/preview" : req.getPath().trim();

            // 1) Site 조회 (idx 기반)
            final Site site = siteRepository.findById(siteIdx)
                    .orElseThrow(() -> processException("layout.preview.site.notfound"));

            // 2) 컨텍스트 구성 (WidgetCtx는 site 식별자로 hostName/코드 중 하나를 사용)
            //    Site 엔티티에 맞춰 적절한 식별자 사용: getSiteHostName() 또는 getCode()
            final String siteIdent = site.getSiteHostName(); // ← 필드명에 맞춰 조정
            final WidgetCtx ctx = widgetService.buildContext(siteIdent, path, kind);

            // 3) 템플릿/페이지/자산 소스
            final String layoutHtml = (req.getLayoutHtml() == null) ? "" : req.getLayoutHtml();
            final String pageHtml   = (req.getPageHtml()   == null) ? "" : req.getPageHtml();
            final List<String> cssUrls = (req.getCssUrls() == null) ? List.of() : req.getCssUrls();
            final List<String> jsUrls  = (req.getJsUrls()  == null) ? List.of() : req.getJsUrls();
            final String inlineCss     = (req.getInlineCss() == null) ? "" : req.getInlineCss();

            // 4) 레이아웃 병합 (슬롯/섹션 + 페이지 본문 삽입)
            String merged = HtmlComposerUtil.composeLayout(layoutHtml, pageHtml, ctx);

            // 5) 위젯 치환
            String withWidgets = widgetService.render(merged, ctx);

            // 6) head 자산 주입 (CSS/JS 목록 + 미리보기 전용 inline CSS 지원)
            String withAssets = HtmlComposerUtil.injectHeadAssets(withWidgets, cssUrls, jsUrls, inlineCss);

            // 7) Sanitizer + 접근성 보정
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
            // 1) Site 조회 (idx 기반)
            Site site = siteRepository.findById(req.getSiteIdx())
                    .orElseThrow(() -> processException("layout.save.site.notfound"));

            LayoutKind kind = req.getKind();

            // 최신 published/draft 버전 가져오기
            Integer latestPubVer = siteLayoutRepository
                    .findFirstBySiteAndKindAndIsPublishedTrueOrderByVersionDesc(site, kind)
                    .map(SiteLayout::getVersion).orElse(0);

            Integer latestDraftVer = siteLayoutRepository
                    .findFirstBySiteAndKindAndIsPublishedFalseOrderByVersionDesc(site, kind)
                    .map(SiteLayout::getVersion).orElse(0);

            int nextVersion = Math.max(latestPubVer, latestDraftVer) + 1;

            // 저장 엔티티 생성
            SiteLayout layout = new SiteLayout();
            layout.setSite(site);
            layout.setKind(kind);
            layout.setHtml(req.getHtml());
            layout.setVersion(nextVersion);
            layout.setIsPublished(Boolean.TRUE.equals(req.getPublishNow()));
            layout.setExtraCssUrlsJson(toJsonArray(req.getCssUrls()));
            layout.setExtraJsUrlsJson(toJsonArray(req.getJsUrls()));

            siteLayoutRepository.save(layout);

            loggingUtil.logSuccess(Action.CREATE, "layout.save ok: siteIdx=" + req.getSiteIdx());

        } catch (Exception e) {
            loggingUtil.logFail(Action.CREATE, "layout.save unexpected: " + e.getMessage());
            throw processException("layout.save.unexpected", e);
        }
    }

    /* "/" → MAIN, 그 외 → SUB */
    private LayoutKind resolveKind(String path) {
        return (path == null || "/".equals(path.trim())) ? LayoutKind.MAIN : LayoutKind.SUB;
    }

    /** ["a.css","b.css"] 같은 JSON 배열 → List<String>, null/공백/비배열 허용 */
    private List<String> parseJsonArray(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ignore) {
            // 포맷이 배열이 아닌 경우: 단일 URL 문자열일 수 있으므로 하나짜리 리스트로 반환
            return List.of(json.trim());
        }
    }

    /** List<String> → JSON 배열 문자열 ([], ["..."]) */
    private String toJsonArray(List<String> urls) {
        try {
            return objectMapper.writeValueAsString(urls == null ? List.of() : urls);
        } catch (Exception e) {
            // 실패 시 안전하게 빈 배열로
            return "[]";
        }
    }
}
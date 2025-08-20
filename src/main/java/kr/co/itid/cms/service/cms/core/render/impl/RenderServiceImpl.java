package kr.co.itid.cms.service.cms.core.render.impl;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.dto.auth.UserPermissionResponse;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterResponse;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuTypeValueResponse;
import kr.co.itid.cms.dto.cms.core.render.LayoutResolveResult;
import kr.co.itid.cms.dto.cms.core.render.response.RenderResponse;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.enums.LayoutKind;
import kr.co.itid.cms.service.auth.PermissionService;
import kr.co.itid.cms.service.cms.core.board.BoardMasterService;
import kr.co.itid.cms.service.cms.core.menu.MenuService;
import kr.co.itid.cms.service.cms.core.page.LayoutService;
import kr.co.itid.cms.service.cms.core.page.WidgetService;
import kr.co.itid.cms.service.cms.core.render.RenderService;
import kr.co.itid.cms.util.HtmlComposerUtil;
import kr.co.itid.cms.util.HtmlSanitizerUtil;
import kr.co.itid.cms.util.LoggingUtil;
import kr.co.itid.cms.util.SecurityUtil;
import kr.co.itid.cms.util.WidgetCtx;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RenderServiceImpl extends EgovAbstractServiceImpl implements RenderService {

    private final MenuService menuService;
    private final LayoutService layoutService;
    private final WidgetService widgetService;
    private final PermissionService permissionService;
    private final BoardMasterService boardMasterService;
    private final LoggingUtil loggingUtil;

    /* =========================================================
       권한/옵션 포함 렌더 데이터
     ========================================================= */
    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public RenderResponse getRenderData() throws Exception {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        Long menuId = user.menuId();

        loggingUtil.logAttempt(Action.RETRIEVE, "render.data attempt: menuId=" + menuId);
        try {
            MenuTypeValueResponse menu = menuService.getMenuRenderById(menuId);
            if (menu.getType() == null || menu.getValue() == null) {
                throw processException("render.data.menu.invalid");
            }

            Object option;
            String value = menu.getValue();
            UserPermissionResponse perm = permissionService.getPermissionByMenu(user);

            switch (menu.getType()) {
                case "board": {
                    BoardMasterResponse board = boardMasterService.getBoardByIdx(Long.valueOf(value));
                    option = board;
                    break;
                }
                case "content":
                case "script":
                case "drive":
                case "folder":
                case "link":
                    option = null;
                    break;
                default:
                    throw processException("render.data.type.unsupported");
            }

            loggingUtil.logSuccess(Action.RETRIEVE, "render.data ok: type=" + menu.getType());
            return RenderResponse.builder()
                    .type(menu.getType())
                    .value(value)
                    .option(option)
                    .permission(perm)
                    .build();

        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "render.data db error: menuId=" + menuId);
            throw processException("render.data.db.error", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "render.data unexpected");
            throw processException("render.data.unexpected", e);
        }
    }

    /* =========================================================
       HTML: composed (레이아웃 → 병합 → 위젯 → 자산 → Sanitizer)
     ========================================================= */
    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public String composePage(String site, String path, Long layoutVersion, String mode) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE,
                "render.compose attempt: site=" + site + ", path=" + path + ", ver=" + layoutVersion + ", mode=" + mode);

        try {
            // 1) 레이아웃 해석
            final LayoutResolveResult layout = layoutService.resolveForRender(site, path, layoutVersion, mode);
            if (layout == null || layout.getHtmlTemplate() == null) {
                loggingUtil.logFail(Action.RETRIEVE, "render.compose layout not found");
                throw processException("render.compose.layout.notfound");
            }

            final String templateHtml = layout.getHtmlTemplate();
            final List<String> cssUrls = layout.getCssUrl();
            final List<String> jsUrls  = layout.getJsUrl();
            final LayoutKind kind      = layout.getKind();

            // 2) 위젯 컨텍스트
            final WidgetCtx widgetCtx = widgetService.buildContext(site, path, kind);

            // 3) 템플릿 병합(슬롯/섹션)
            String merged = HtmlComposerUtil.merge(templateHtml, widgetCtx);

            // 4) 위젯 치환
            String withWidgets = widgetService.render(merged, widgetCtx);

            // 5) head 자산 주입 (여러 URL)
            String withAssets = HtmlComposerUtil.injectHeadAssets(withWidgets, cssUrls, jsUrls);

            // 6) Sanitizer
            String sanitized = HtmlSanitizerUtil.clean(withAssets);

            // 7) 접근성 보정
            String finalHtml = HtmlComposerUtil.applyAccessibilityFix(sanitized);

            loggingUtil.logSuccess(Action.RETRIEVE, "render.compose ok");
            return finalHtml;

        } catch (DataAccessException dae) {
            loggingUtil.logFail(Action.RETRIEVE, "render.compose db error: " + dae.getMessage());
            throw processException("render.compose.db.error", dae);
        } catch (EgovBizException ebe) {
            throw ebe;
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "render.compose unexpected: " + e.getMessage());
            throw processException("render.compose.unexpected", e);
        }
    }

    /* =========================================================
       HTML: shell (Teleport 컨테이너만, 거의 정적)
     ========================================================= */
    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public String buildShell(String site, String path, String mode) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE,
                "render.shell attempt: site=" + site + ", path=" + path + ", mode=" + mode);

        try {
            String shell = HtmlComposerUtil.minimalShell(builder -> builder
                    .title("Shell")
                    .addContainer("#app")
                    .addContainer("#teleport-gnb")
                    .addContainer("#teleport-footer")
                    .putData("site", site)
                    .putData("path", path)
                    .putData("mode", mode)
            );

            // Sanitizer (방어 한 번 더)
            String finalShell = HtmlSanitizerUtil.clean(shell);

            loggingUtil.logSuccess(Action.RETRIEVE, "render.shell ok");
            return finalShell;

        } catch (DataAccessException dae) {
            loggingUtil.logFail(Action.RETRIEVE, "render.shell db error: " + dae.getMessage());
            throw processException("render.shell.db.error", dae);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "render.shell unexpected: " + e.getMessage());
            throw processException("render.shell.unexpected", e);
        }
    }
}
package kr.co.itid.cms.service.cms.core.page.impl;

import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.enums.LayoutKind;
import kr.co.itid.cms.service.cms.core.page.WidgetService;
import kr.co.itid.cms.service.cms.core.page.widget.engine.WidgetEngine;
import kr.co.itid.cms.service.cms.core.page.widget.model.WidgetCtx;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("widgetService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WidgetServiceImpl extends EgovAbstractServiceImpl implements WidgetService {

    private final WidgetEngine widgetEngine;
    private final LoggingUtil loggingUtil;

    @Override
    public WidgetCtx buildContext(String site, String path, LayoutKind kind) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE,
                "widget.ctx attempt: site=" + site + ", path=" + path + ", kind=" + kind);
        try {
            String normPath = (path == null || path.isBlank()) ? "/" : path.trim();
            // TODO: 사용자/권한/로케일 등 필요시 확장
            WidgetCtx ctx = WidgetCtx.builder()
                    .siteIdent(site)
                    .path(normPath)
                    .kind(kind)
                    .mode("preview") // 운영 렌더에서 published/draft로 설정
                    .build();

            loggingUtil.logSuccess(Action.RETRIEVE, "widget.ctx ok");
            return ctx;
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "widget.ctx unexpected: " + e.getMessage());
            throw processException("widget.ctx.unexpected", e);
        }
    }

    @Override
    public String render(String html, WidgetCtx ctx) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE,
                "widget.render attempt: site=" + ctx.getSiteIdent() + ", path=" + ctx.getPath()
                        + ", kind=" + ctx.getKind() + ", mode=" + ctx.getMode());
        try {
            if (html == null || html.isBlank()) {
                loggingUtil.logSuccess(Action.RETRIEVE, "widget.render ok: empty input");
                return "";
            }

            String out = widgetEngine.process(html, ctx);

            loggingUtil.logSuccess(Action.RETRIEVE, "widget.render ok");
            return out;

        } catch (DataAccessException dae) {
            loggingUtil.logFail(Action.RETRIEVE, "widget.render db error: " + dae.getMessage());
            throw processException("widget.render.db.error", dae);
        } catch (EgovBizException ebe) {
            throw ebe;
        } catch (IllegalArgumentException iae) {
            // 위젯 속성 검증 실패 등
            loggingUtil.logFail(Action.RETRIEVE, "widget.render invalid: " + iae.getMessage());
            throw processException("widget.render.invalid", iae);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "widget.render unexpected: " + e.getMessage());
            throw processException("widget.render.unexpected", e);
        }
    }
}
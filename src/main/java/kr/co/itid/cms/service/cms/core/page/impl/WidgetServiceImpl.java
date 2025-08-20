package kr.co.itid.cms.service.cms.core.page.impl;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.enums.LayoutKind;
import kr.co.itid.cms.service.cms.core.page.WidgetService;
import kr.co.itid.cms.util.LoggingUtil;
import kr.co.itid.cms.util.SecurityUtil;
import kr.co.itid.cms.util.WidgetCtx;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("widgetService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WidgetServiceImpl extends EgovAbstractServiceImpl implements WidgetService {

    private final LoggingUtil loggingUtil;

    // 위젯 핸들러 등록소 (이후 단계에서 실제 핸들러 빈을 주입 받아 채워도 됨)
    private final Map<String, WidgetHandler> handlers = new HashMap<>();

    @Override
    public WidgetCtx buildContext(String site, String path, LayoutKind kind) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE,
                "widget.ctx attempt: site=" + site + ", path=" + path + ", kind=" + kind);

        try {
            final JwtAuthenticatedUser user = SecurityUtil.getCurrentUser(); // 익명일 수 있음
            // 로케일/타임존은 필요시 RequestContext에서 가져오도록 확장 가능
            final Locale locale = Locale.KOREA;
            final String tz = "Asia/Seoul";
            final ZonedDateTime now = ZonedDateTime.now(ZoneId.of(tz));

            // vars: 추후 메뉴/카테고리/쿼리파라미터 등 자유롭게 추가
            final Map<String, Object> vars = new HashMap<>();
            vars.put("site", site);
            vars.put("path", path);
            vars.put("kind", kind == null ? null : kind.name());
            vars.put("now", now.toString());

            WidgetCtx ctx = WidgetCtx.builder()
                    .site(site)
                    .path(path)
                    .kind(kind)
                    .userId(user.userIdx())
                    .userLevel(user.userLevel())
                    .anonymous(user.isGuest())
                    .locale(locale)
                    .timeZone(tz)
                    .now(now)
                    .vars(Collections.unmodifiableMap(vars))
                    .build();

            loggingUtil.logSuccess(Action.RETRIEVE, "widget.ctx ok");
            return ctx;

        } catch (DataAccessException dae) {
            loggingUtil.logFail(Action.RETRIEVE, "widget.ctx db error: " + dae.getMessage());
            throw processException("widget.ctx.db.error", dae);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "widget.ctx unexpected: " + e.getMessage());
            throw processException("widget.ctx.unexpected", e);
        }
    }

    @Override
    public String render(String html, WidgetCtx ctx) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "widget.replace attempt");
        try {
            if (html == null || html.isBlank()) return html;

            // {{ widget:name key="value" ... }} 패턴
            Pattern token = Pattern.compile("\\{\\{\\s*widget:([a-zA-Z0-9_\\-]+)([^}]*)}}");
            Matcher m = token.matcher(html);
            StringBuffer sb = new StringBuffer();

            while (m.find()) {
                String widgetName = m.group(1);
                String rawArgs    = m.group(2); // 예: ' id="hero" size="lg"'
                Map<String, String> args = parseArgs(rawArgs);

                // 등록된 핸들러가 있으면 치환, 없으면 토큰 제거 혹은 그대로 유지
                WidgetHandler handler = handlers.get(widgetName);
                String replacement;
                if (handler != null) {
                    replacement = handler.render(args, ctx);
                } else {
                    // 미등록 위젯 정책: 빈 문자열로 제거 (원하면 경고 주석으로 남겨도 됨)
                    replacement = "";
                }

                // 안전하게 이스케이프
                m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            m.appendTail(sb);

            loggingUtil.logSuccess(Action.RETRIEVE, "widget.replace ok");
            return sb.toString();

        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "widget.replace unexpected: " + e.getMessage());
            throw processException("widget.replace.unexpected", e);
        }
    }

    /* ===== 내부 유틸 ===== */

    /** 공백/따옴표 허용: key="value" key2='v2' key3=v3 */
    private Map<String, String> parseArgs(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw == null || raw.isBlank()) return map;

        Pattern p = Pattern.compile("([a-zA-Z0-9_\\-]+)\\s*=\\s*(\"([^\"]*)\"|'([^']*)'|([^\\s\"']+))");
        Matcher m = p.matcher(raw);
        while (m.find()) {
            String key = m.group(1);
            String val = m.group(3) != null ? m.group(3)
                    : m.group(4) != null ? m.group(4)
                    : m.group(5);
            map.put(key, val);
        }
        return map;
    }

    /* ===== 확장 포인트: 핸들러 인터페이스 ===== */

    /**
     * 개별 위젯 렌더러 계약.
     * 이후 배너/공지/탭 등 위젯마다 구현체를 만들어 handlers에 주입하면 된다.
     */
    public interface WidgetHandler {
        String render(Map<String, String> args, WidgetCtx ctx) throws Exception;
    }
}
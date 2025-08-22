package kr.co.itid.cms.service.cms.core.template.widget.handlers.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.itid.cms.service.cms.core.template.widget.engine.WidgetHandler;
import kr.co.itid.cms.service.cms.core.template.widget.model.WidgetCtx;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 클라이언트 전용 메뉴 트리 위젯
 *
 * 서버는 Vue 마운트를 위한 스텁만 반환하고,
 * 실제 렌더링은 프론트(MenuStore/컴포넌트)에서 수행한다.
 *
 * 사용 예)
 *   <cms-widget type="menu.tree" root="/" depth="2" class="gnb"/>
 *
 * 출력 예)
 *   <div class="gnb"
 *        data-cms-widget="menu.tree"
 *        data-props='{"root":"/","depth":2,"currentPath":"/dept/intro"}'></div>
 */
@Component
@RequiredArgsConstructor
public class MenuTreeClientOnlyWidgetHandler implements WidgetHandler {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(String type) {
        return "menu.tree".equalsIgnoreCase(type);
    }

    @Override
    public String render(Element widgetTag, WidgetCtx ctx) throws Exception {
        // 전달할 최소 props 구성 (스토어 기반이면 root/depth/currentPath 정도면 충분)
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("root", widgetTag.hasAttr("root") ? widgetTag.attr("root") : "/");
        props.put("depth", parseInt(widgetTag.hasAttr("depth") ? widgetTag.attr("depth") : "2", 1, 10, 2));
        // 현재 경로는 프론트 라우터가 있으면 생략 가능하지만, SSR 미사용 환경 대비해서 내려줌
        props.put("currentPath", nz(ctx.getPath()));

        String propsJson = objectMapper.writeValueAsString(props);
        String klass = widgetTag.hasAttr("class") ? widgetTag.attr("class") : "";

        return "<div class=\"" + esc(klass) + "\""
                + " data-cms-widget=\"menu.tree\""
                + " data-props='" + esc(propsJson) + "'></div>";
    }

    /* ----------------------- helpers ----------------------- */

    private static String nz(String s) { return s == null ? "" : s; }

    private static String esc(String s) {
        return StringEscapeUtils.escapeHtml4(s == null ? "" : s);
    }

    private static int parseInt(String s, int min, int max, int def) {
        try {
            int v = Integer.parseInt(s.trim());
            if (v < min) return min;
            if (v > max) return max;
            return v;
        } catch (Exception ignore) {
            return def;
        }
    }
}
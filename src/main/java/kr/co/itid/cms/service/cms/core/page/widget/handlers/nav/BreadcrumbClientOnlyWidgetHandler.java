package kr.co.itid.cms.service.cms.core.page.widget.handlers.nav;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.itid.cms.service.cms.core.page.widget.engine.WidgetHandler;
import kr.co.itid.cms.service.cms.core.page.widget.model.WidgetCtx;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 클라이언트 전용 브레드크럼 위젯
 *
 * 서버에서는 실제 HTML을 그리지 않고, Vue가 마운트할 수 있는 스텁만 반환한다.
 *
 * 사용 예)
 *   <cms-widget type="nav.breadcrumb" class="breadcrumb" homeLabel="홈" baseUrl="/" />
 *
 * 출력 예)
 *   <div class="breadcrumb"
 *        data-cms-widget="nav.breadcrumb"
 *        data-props='{"path":"/notice/list","homeLabel":"홈","baseUrl":"/"}'></div>
 *
 * 프론트:
 *   mountCmsWidgets() 가 data-cms-widget="nav.breadcrumb" 요소를 찾아
 *   Breadcrumb.vue 컴포넌트를 마운트한다.
 */
@Component
@RequiredArgsConstructor
public class BreadcrumbClientOnlyWidgetHandler implements WidgetHandler {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(String type) {
        return "nav.breadcrumb".equalsIgnoreCase(type);
    }

    @Override
    public String render(Element widgetTag, WidgetCtx ctx) throws Exception {
        // 전달할 최소 props 구성 (없어도 되지만 유용한 값만 전달)
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("path", nz(ctx.getPath())); // 라우트 미사용 환경에서도 동작하도록
        if (widgetTag.hasAttr("homeLabel")) props.put("homeLabel", widgetTag.attr("homeLabel"));
        if (widgetTag.hasAttr("baseUrl"))   props.put("baseUrl", widgetTag.attr("baseUrl"));

        String propsJson = objectMapper.writeValueAsString(props);

        String klass = widgetTag.hasAttr("class") ? widgetTag.attr("class") : "";

        // data-* 속성은 Sanitizer 화이트리스트에 포함되어 있어 유지됨
        return "<div class=\"" + esc(klass) + "\""
                + " data-cms-widget=\"nav.breadcrumb\""
                + " data-props='" + esc(propsJson) + "'></div>";
    }

    private static String nz(String s){ return s == null ? "" : s; }
    private static String esc(String s){ return StringEscapeUtils.escapeHtml4(s == null ? "" : s); }
}
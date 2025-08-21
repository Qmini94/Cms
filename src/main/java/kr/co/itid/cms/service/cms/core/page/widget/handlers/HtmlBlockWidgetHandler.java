package kr.co.itid.cms.service.cms.core.page.widget.handlers;

import kr.co.itid.cms.service.cms.core.page.widget.engine.WidgetHandler;
import kr.co.itid.cms.service.cms.core.page.widget.model.WidgetCtx;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * HTML 블록 위젯
 *
 * 사용 예)
 *   <cms-widget type="html.block">
 *     <![CDATA[
 *       <div class="banner"><img src="/assets/banner.png"/></div>
 *     ]]>
 *   </cms-widget>
 *
 * 출력 예)
 *   <div class="banner"><img src="/assets/banner.png"/></div>
 *
 * 특징
 * - 개발자/관리자가 직접 작성한 HTML 조각을 그대로 삽입.
 * - XSS 위험이 있으므로 Sanitizer 단계에서 필터링됨.
 * - 주로 정적 배너/홍보 영역에 사용.
 */
@Component
public class HtmlBlockWidgetHandler implements WidgetHandler {

    @Override
    public boolean supports(String type) {
        return "html.block".equalsIgnoreCase(type);
    }

    @Override
    public String render(Element widgetTag, WidgetCtx ctx) {
        // <cms-widget> 태그 안쪽의 raw HTML 추출
        String inner = widgetTag.html();
        if (inner == null || inner.isBlank()) {
            return "<!-- empty html.block -->";
        }

        // 그대로 반환 → 후속 Sanitizer에서 위험 요소 제거
        return inner;
    }
}
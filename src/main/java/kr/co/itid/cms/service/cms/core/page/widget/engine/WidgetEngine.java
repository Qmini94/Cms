package kr.co.itid.cms.service.cms.core.page.widget.engine;

import kr.co.itid.cms.service.cms.core.page.widget.model.WidgetCtx;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * 위젯 엔진
 * - HTML 내 <cms-slot>, <cms-widget> 태그를 순회하며 위젯 핸들러로 치환한다.
 * - Preview/Composed 렌더링 모두에서 호출된다.
 */
@Component
@RequiredArgsConstructor
public class WidgetEngine {

    private final WidgetRegistry registry;

    /**
     * HTML 내의 모든 위젯 태그를 실제 HTML로 변환한다.
     *
     * @param html 원본 템플릿/페이지 병합 HTML
     * @param ctx  렌더 컨텍스트
     * @return 위젯이 치환된 최종 HTML
     */
    public String process(String html, WidgetCtx ctx) throws Exception {
        if (html == null || html.isBlank()) return html;

        Document doc = Jsoup.parse(html);

        // 1) <cms-slot> 은 wrapper 제거 (안쪽 컨텐츠만 유지)
        for (Element slot : doc.select("cms-slot")) {
            slot.unwrap();
        }

        // 2) <cms-widget> 은 핸들러로 치환
        for (Element widgetTag : doc.select("cms-widget")) {
            String type = widgetTag.hasAttr("type") ? widgetTag.attr("type").trim() : "";
            if (type.isEmpty()) {
                widgetTag.remove();
                continue;
            }
            try {
                String rendered = registry.get(type).render(widgetTag, ctx);
                Element frag = Jsoup.parseBodyFragment(rendered).body();
                if (frag.childrenSize() > 0) {
                    widgetTag.replaceWith(frag.child(0));
                } else {
                    widgetTag.remove();
                }
            } catch (IllegalArgumentException iae) {
                // 속성 검증 실패 → 태그 제거
                widgetTag.remove();
            }
        }

        return doc.outerHtml();
    }
}
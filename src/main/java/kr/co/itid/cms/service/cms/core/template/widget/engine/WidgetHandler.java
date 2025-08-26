package kr.co.itid.cms.service.cms.core.template.widget.engine;

import kr.co.itid.cms.service.cms.core.template.widget.model.WidgetCtx;
import org.jsoup.nodes.Element;

/**
 * 개별 위젯 타입의 렌더러 SPI.
 * - supports(type): 위젯 타입 매칭 (예: "board.list", "auth.status")
 * - render(tag, ctx): &lt;cms-widget ...&gt; 태그를 안전한 HTML 조각으로 변환
 *
 * 구현 규칙
 * - 필수 속성 누락/유효성 실패 시 IllegalArgumentException 던질 것
 * - 반환 HTML은 최종 Sanitizer를 통과하므로, 기본적으로 escape를 적용하고
 *   really-raw가 필요하면 제한된 범위에서만 |html 사용(핸들러 내부에서 주의)
 */
public interface WidgetHandler {
    /** 이 핸들러가 처리하는 위젯 타입인지 여부 */
    boolean supports(String type);

    /**
     * 단일 위젯 태그를 HTML로 렌더링
     * @param widgetTag &lt;cms-widget ...&gt; 요소 (속성/내부 템플릿 접근 가능)
     * @param ctx       사이트/경로/레이아웃/사용자 등 렌더 컨텍스트
     * @return 안전한 HTML 조각(루트 script/style 금지)
     * @throws Exception 데이터 조회/템플릿 처리 중 오류
     */
    String render(Element widgetTag, WidgetCtx ctx) throws Exception;
}
package kr.co.itid.cms.service.cms.core.page;

import kr.co.itid.cms.enums.LayoutKind;
import kr.co.itid.cms.util.WidgetCtx;

/**
 * 위젯 처리 서비스 인터페이스
 * <p>
 * 레이아웃/페이지 HTML 내에 포함된 위젯 토큰을
 * 실제 HTML 콘텐츠로 치환하는 기능을 제공합니다.
 * </p>
 */
public interface WidgetService {

    /**
     * 위젯 치환에 필요한 컨텍스트를 구성합니다.
     * <p>
     * 컨텍스트에는 사이트/경로/레이아웃 종류, 사용자 정보, 권한, 로케일 등
     * 위젯 렌더링에 필요한 모든 런타임 데이터를 포함합니다.
     * </p>
     *
     * @param site 사이트 식별자 (예: "admin", "www")
     * @param path 요청 경로 (예: "/", "/notice/list")
     * @param kind 레이아웃 종류 (MAIN 또는 SUB)
     * @return WidgetCtx 위젯 치환에 사용될 컨텍스트 객체
     * @throws Exception 컨텍스트 생성 중 오류 발생 시
     */
    WidgetCtx buildContext(String site, String path, LayoutKind kind) throws Exception;

    /**
     * 주어진 HTML 문자열에서 모든 위젯 토큰을 실제 HTML로 치환합니다.
     * <p>
     * 예시:
     * <pre>
     *   입력: "&lt;div&gt;{{ widget:banner id="hero" }}&lt;/div&gt;"
     *   출력: "&lt;div&gt;&lt;section class='banner'&gt;...&lt;/section&gt;&lt;/div&gt;"
     * </pre>
     * </p>
     *
     * @param pageHtml 템플릿 HTML (위젯 토큰 포함)
     * @param ctx 위젯 컨텍스트 (사이트, 경로, 사용자, 권한, 시간 등)
     * @return String 위젯이 치환된 최종 HTML 문자열
     * @throws Exception 치환 처리 중 오류 발생 시
     */
    String render(String pageHtml, WidgetCtx ctx) throws Exception;
}
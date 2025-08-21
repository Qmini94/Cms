package kr.co.itid.cms.service.cms.core.page;

import kr.co.itid.cms.enums.LayoutKind;
import kr.co.itid.cms.service.cms.core.page.widget.model.WidgetCtx;

/**
 * 위젯 처리 서비스 (파사드)
 *
 * - 컨트롤러/다른 서비스는 본 인터페이스만 의존한다.
 * - 내부적으로 WidgetEngine/WidgetHandler들로 위젯 태그(<cms-widget>, <cms-slot>)를 치환한다.
 */
public interface WidgetService {

    /**
     * 위젯 치환에 필요한 컨텍스트를 구성한다.
     * 컨텍스트에는 사이트/경로/레이아웃 종류, 사용자/권한/로케일 등 런타임 데이터를 포함한다.
     *
     * @param site 사이트 식별자 (예: "www.example.com" or "siteCode")
     * @param path 요청 경로 (예: "/", "/notice/list")
     * @param kind 레이아웃 종류 (MAIN | SUB)
     */
    WidgetCtx buildContext(String site, String path, LayoutKind kind) throws Exception;

    /**
     * 주어진 HTML에서 <cms-widget>, <cms-slot> 태그를 실제 HTML로 치환한다.
     *
     * 입력 예시:
     *   <div>
     *     <cms-widget type="auth.status" loginText="로그인" logoutText="로그아웃"/>
     *   </div>
     *
     * 출력 예시:
     *   <div>
     *     <div class="auth"><a href="/auth/login">로그인</a></div>
     *   </div>
     *
     * @param html 템플릿 HTML (위젯 태그 포함)
     * @param ctx  위젯 컨텍스트
     * @return 위젯 치환된 HTML
     */
    String render(String html, WidgetCtx ctx) throws Exception;
}
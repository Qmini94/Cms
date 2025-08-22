package kr.co.itid.cms.service.cms.core.template.widget.handlers;

import kr.co.itid.cms.service.cms.core.template.widget.engine.WidgetHandler;
import kr.co.itid.cms.service.cms.core.template.widget.model.WidgetCtx;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * 인증 상태 위젯
 *
 * 사용 예)
 *   <cms-widget type="auth.status" loginText="로그인" logoutText="로그아웃" class="auth-box"/>
 *
 * 출력 예)
 *   비로그인: <div class="auth-box"><a href="/auth/login">로그인</a></div>
 *   로그인  : <div class="auth-box"><span class="user">홍길동</span> <a href="/auth/logout">로그아웃</a></div>
 *
 * 주의: 인라인 스크립트 금지, 텍스트는 escape 처리
 */
@Component
public class AuthStatusWidgetHandler implements WidgetHandler {

    @Override
    public boolean supports(String type) {
        return "auth.status".equalsIgnoreCase(type);
    }

    @Override
    public String render(Element widgetTag, WidgetCtx ctx) {
        // 속성 파싱(기본값 제공)
        String loginText  = attrOr(widgetTag, "loginText", "로그인");
        String logoutText = attrOr(widgetTag, "logoutText", "로그아웃");
        String extraClass = widgetTag.hasAttr("class") ? widgetTag.attr("class") : "";

        // 로그인 여부 (WidgetCtx에 사용자 정보를 붙이면 여기서 판단)
        boolean loggedIn = false;
        String userName = "";
        // TODO: ctx.getUser() 가 있다면 여기서 읽어 처리
        // loggedIn = ctx.getUser() != null && !"GUEST".equals(ctx.getUser().getUsername());
        // userName = ctx.getUser() != null ? ctx.getUser().getName() : "";

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"")
                .append(escape(extraClass))
                .append("\">");

        if (loggedIn) {
            sb.append("<span class=\"user\">")
                    .append(escape(userName))
                    .append("</span> ")
                    .append("<a class=\"logout\" href=\"/auth/logout\">")
                    .append(escape(logoutText))
                    .append("</a>");
        } else {
            sb.append("<a class=\"login\" href=\"/auth/login\">")
                    .append(escape(loginText))
                    .append("</a>");
        }

        sb.append("</div>");
        return sb.toString();
    }

    private static String attrOr(Element el, String name, String defVal) {
        String v = el.hasAttr(name) ? el.attr(name).trim() : "";
        return v.isEmpty() ? defVal : v;
    }

    private static String escape(String s) {
        return StringEscapeUtils.escapeHtml4(s == null ? "" : s);
    }
}
package kr.co.itid.cms.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import java.util.regex.Pattern;

public final class HtmlSanitizerUtil {

    private HtmlSanitizerUtil() {}

    private static final Pattern UNSAFE_PATTERN = Pattern.compile(
            "(?i)<script|javascript:|on[a-z]+\\s*=|<iframe|<object|<embed|<svg|data:text/html"
    );

    private static final PolicyFactory POLICY_LAYOUT = new HtmlPolicyBuilder()
            .allowCommonBlockElements()
            .allowCommonInlineFormattingElements()
            .allowElements("header","footer","main","nav","section","article","aside","figure","figcaption")
            .allowElements("table","thead","tbody","tfoot","tr","th","td","colgroup","col","ul","ol","li","dl","dt","dd","hr")
            .allowElements("a","img")
            .allowAttributes("href","target","rel","title").onElements("a")
            .allowAttributes("src","alt","title","width","height").onElements("img")
            .allowUrlProtocols("http","https","data")
            .allowAttributes("class","id").globally()
            .allowElements("cms-slot")
            .allowAttributes("name").onElements("cms-slot")
            .toFactory();

    private static final PolicyFactory POLICY_PAGE = new HtmlPolicyBuilder()
            .allowCommonBlockElements()
            .allowCommonInlineFormattingElements()
            .allowElements("table","thead","tbody","tfoot","tr","th","td","colgroup","col",
                    "ul","ol","li","dl","dt","dd","hr",
                    "header","footer","section","article","aside","figure","figcaption")
            .allowElements("a","img")
            .allowAttributes("href","target","rel","title").onElements("a")
            .allowAttributes("src","alt","title","width","height").onElements("img")
            .allowUrlProtocols("http","https","data")
            .allowAttributes("class","id").globally()
            .allowElements("cms-board-list","cms-board-detail","cms-weather","cms-include")
            .allowAttributes("board","limit","template","id").onElements("cms-board-list","cms-board-detail")
            .allowAttributes("city","unit","ttl").onElements("cms-weather")
            .allowAttributes("path").onElements("cms-include")
            .toFactory();

    public static String clean(String html) { return sanitizePage(html); }

    public static String sanitize(String html) { return sanitizePage(html); }

    public static String sanitizeLayout(String html) {
        if (html == null) return null;
        if (UNSAFE_PATTERN.matcher(html).find()) throw new IllegalArgumentException("허용되지 않은 태그/속성이 포함되어 있습니다.");
        return POLICY_LAYOUT.sanitize(html);
    }

    public static String sanitizePage(String html) {
        if (html == null) return null;
        if (UNSAFE_PATTERN.matcher(html).find()) throw new IllegalArgumentException("허용되지 않은 태그/속성이 포함되어 있습니다.");
        return POLICY_PAGE.sanitize(html);
    }
}
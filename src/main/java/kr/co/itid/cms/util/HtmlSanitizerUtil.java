package kr.co.itid.cms.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import java.util.regex.Pattern;

public class HtmlSanitizerUtil {

    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
            .allowElements("p", "div", "b", "i", "ul", "li", "br", "strong", "em", "span", "a", "img")
            .allowUrlProtocols("http", "https")
            .allowAttributes("href").onElements("a")
            .allowAttributes("src", "alt").onElements("img")
            .allowAttributes("style").onElements("span", "div", "p")
            .toFactory();

    private static final Pattern UNSAFE_PATTERN = Pattern.compile(
            "(?i)<script|javascript:|onerror=|onload=|<iframe|<object|<embed|<svg|data:text/html",
            Pattern.CASE_INSENSITIVE
    );

    public static String sanitize(String html) {
        if (html == null) return null;

        if (UNSAFE_PATTERN.matcher(html).find()) {
            throw new IllegalArgumentException("입력값에 허용되지 않은 태그나 속성이 포함되어 있습니다.");
        }

        return POLICY.sanitize(html);
    }
}

package kr.co.itid.cms.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import java.util.regex.Pattern;

public final class HtmlSanitizerUtil {

    private HtmlSanitizerUtil() {}

    //공통: 위험 패턴 1차 차단 (추가 방어)
    private static final Pattern UNSAFE_PATTERN = Pattern.compile(
            "(?i)<script|javascript:|on[a-z]+\\s*=|<iframe|<object|<embed|<svg|data:text/html"
    );

    /*
     * 1) 레이아웃용 정책
     * - 헤더/푸터/메인 등 시맨틱 태그 허용
     * - 슬롯 태그 <cms-slot name="content"> 허용
     * - 표/목록/링크/이미지 등 일반 마크업 허용
     * - 스크립트/이벤트/iframes 등 일절 불가
     */
    private static final PolicyFactory POLICY_LAYOUT = new HtmlPolicyBuilder()
            // 블록/인라인 기본
            .allowCommonBlockElements()
            .allowCommonInlineFormattingElements()

            // 시맨틱 태그
            .allowElements("header","footer","main","nav","section","article","aside",
                    "figure","figcaption")

            // 표/리스트/기타 자주 쓰는 태그
            .allowElements("table","thead","tbody","tfoot","tr","th","td","colgroup","col",
                    "ul","ol","li","dl","dt","dd","hr")

            // 링크/이미지
            .allowElements("a","img")
            .allowAttributes("href","target","rel","title").onElements("a")
            .allowAttributes("src","alt","title","width","height").onElements("img")
            .allowUrlProtocols("http","https","data") // data:는 img만 쓰길 권장

            // 클래스/아이디/스타일 (필요 시만 사용 권장)
            .allowAttributes("class","id").globally()
            // 스타일은 위험할 수 있으니 정말 필요할 때만 열기
            // .allowAttributes("style").globally()

            .allowElements("cms-slot")
            .allowAttributes("name").onElements("cms-slot")

            .toFactory();

    /*
     * 2) 페이지 본문용 정책
     * - 위젯 토큰 <cms-*> 허용
     * - 일반 마크업 허용
     * - 스크립트/이벤트 금지
     */
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
            // .allowAttributes("style").globally()

            .allowElements("cms-board-list","cms-board-detail","cms-weather","cms-include")
            .allowAttributes("board","limit","template","id").onElements("cms-board-list","cms-board-detail")
            .allowAttributes("city","unit","ttl").onElements("cms-weather")
            .allowAttributes("path").onElements("cms-include")

            .toFactory();

    /** (하위호환) 기존 메서드: 범용 sanitize – 페이지 본문에 준해 처리 */
    public static String sanitize(String html) {
        return sanitizePage(html);
    }

    /** 레이아웃 HTML 정화 */
    public static String sanitizeLayout(String html) {
        if (html == null) return null;
        if (UNSAFE_PATTERN.matcher(html).find()) {
            throw new IllegalArgumentException("허용되지 않은 태그/속성이 포함되어 있습니다.");
        }
        return POLICY_LAYOUT.sanitize(html);
    }

    /** 페이지 본문 HTML 정화 (위젯 토큰 포함) */
    public static String sanitizePage(String html) {
        if (html == null) return null;
        if (UNSAFE_PATTERN.matcher(html).find()) {
            throw new IllegalArgumentException("허용되지 않은 태그/속성이 포함되어 있습니다.");
        }
        return POLICY_PAGE.sanitize(html);
    }
}
package kr.co.itid.cms.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * HTML 새니타이저 유틸리티
 * 사용자 입력 HTML을 안전하게 처리하여 XSS 공격을 방어
 */
@Slf4j
@Component
public class HtmlSanitizer {

    // 허용된 HTML 태그들 (화이트리스트 방식)
    private static final String[] ALLOWED_TAGS = {
        "p", "br", "strong", "b", "em", "i", "u", "h1", "h2", "h3", "h4", "h5", "h6",
        "ul", "ol", "li", "blockquote", "a", "img", "table", "thead", "tbody", "tr", "td", "th"
    };

    // 허용된 속성들
    private static final String[] ALLOWED_ATTRIBUTES = {
        "href", "src", "alt", "title", "class", "id", "target"
    };

    // 위험한 패턴들
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern VBSCRIPT_PATTERN = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern ONLOAD_PATTERN = Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern STYLE_EXPRESSION_PATTERN = Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE);

    /**
     * HTML 콘텐츠를 안전하게 새니타이즈
     * 
     * @param html 원본 HTML 문자열
     * @return 새니타이즈된 안전한 HTML 문자열
     */
    public String sanitize(String html) {
        if (html == null || html.trim().isEmpty()) {
            return html;
        }

        try {
            // 1단계: 위험한 스크립트 패턴 제거
            String cleanHtml = removeScriptPatterns(html);
            
            // 2단계: 위험한 이벤트 핸들러 제거
            cleanHtml = removeEventHandlers(cleanHtml);
            
            // 3단계: 위험한 스타일 제거
            cleanHtml = removeDangerousStyles(cleanHtml);
            
            // 4단계: 허용되지 않은 태그 제거 (기본적인 처리)
            cleanHtml = removeUnallowedTags(cleanHtml);
            
            log.debug("[HTML 새니타이즈] 처리 완료");
            return cleanHtml;
            
        } catch (Exception e) {
            log.error("[HTML 새니타이즈] 처리 중 오류 발생: {}", e.getMessage());
            // 오류 발생 시 모든 HTML 태그 제거하여 안전하게 처리
            return html.replaceAll("<[^>]*>", "");
        }
    }

    /**
     * 스크립트 패턴 제거
     */
    private String removeScriptPatterns(String html) {
        html = SCRIPT_PATTERN.matcher(html).replaceAll("");
        html = JAVASCRIPT_PATTERN.matcher(html).replaceAll("");
        html = VBSCRIPT_PATTERN.matcher(html).replaceAll("");
        return html;
    }

    /**
     * 이벤트 핸들러 제거 (onclick, onload 등)
     */
    private String removeEventHandlers(String html) {
        return ONLOAD_PATTERN.matcher(html).replaceAll("");
    }

    /**
     * 위험한 CSS 스타일 제거
     */
    private String removeDangerousStyles(String html) {
        return STYLE_EXPRESSION_PATTERN.matcher(html).replaceAll("");
    }

    /**
     * 허용되지 않은 태그 제거 (기본적인 처리)
     * 실제 운영에서는 더 정교한 HTML 파서 라이브러리 사용 권장
     */
    private String removeUnallowedTags(String html) {
        // 위험한 태그들 제거
        String[] dangerousTags = {
            "script", "object", "embed", "link", "meta", "iframe", "frame", "frameset",
            "form", "input", "button", "textarea", "select", "option"
        };
        
        for (String tag : dangerousTags) {
            Pattern pattern = Pattern.compile("<" + tag + "[^>]*>.*?</" + tag + ">", 
                                            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
            html = pattern.matcher(html).replaceAll("");
            
            // 단일 태그도 제거
            Pattern singlePattern = Pattern.compile("<" + tag + "[^>]*/>", Pattern.CASE_INSENSITIVE);
            html = singlePattern.matcher(html).replaceAll("");
        }
        
        return html;
    }

    /**
     * 텍스트만 추출 (모든 HTML 태그 제거)
     * 
     * @param html HTML 문자열
     * @return 순수 텍스트
     */
    public String extractText(String html) {
        if (html == null) {
            return null;
        }
        
        // 모든 HTML 태그 제거
        String text = html.replaceAll("<[^>]*>", "");
        
        // HTML 엔티티 디코딩
        text = text.replace("&lt;", "<")
                  .replace("&gt;", ">")
                  .replace("&quot;", "\"")
                  .replace("&#x27;", "'")
                  .replace("&#x2F;", "/")
                  .replace("&amp;", "&"); // 마지막에 처리
        
        return text.trim();
    }

    /**
     * HTML 이스케이프 처리
     * 
     * @param text 일반 텍스트
     * @return 이스케이프된 HTML 안전 텍스트
     */
    public String escapeHtml(String text) {
        if (text == null) {
            return null;
        }
        
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;")
                  .replace("/", "&#x2F;");
    }
}

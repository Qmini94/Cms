package kr.co.itid.cms.config.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.regex.Pattern;

/**
 * XSS 공격 방어를 위한 HttpServletRequest 래퍼 클래스
 * 요청 파라미터의 악성 스크립트를 제거하거나 이스케이프 처리
 */
public class XssProtectionRequestWrapper extends HttpServletRequestWrapper {

    // XSS 공격에 사용되는 패턴들을 미리 컴파일
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern VBSCRIPT_PATTERN = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);
    private static final Pattern ONLOAD_PATTERN = Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern ONERROR_PATTERN = Pattern.compile("onerror(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern ONCLICK_PATTERN = Pattern.compile("onclick(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
    private static final Pattern EVAL_PATTERN = Pattern.compile("eval\\(", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("expression\\(", Pattern.CASE_INSENSITIVE);

    public XssProtectionRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (values == null) {
            return null;
        }

        // 각 파라미터 값에 대해 XSS 방어 처리 적용
        String[] cleanValues = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            cleanValues[i] = cleanXssString(values[i]);
        }
        return cleanValues;
    }

    @Override
    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        return cleanXssString(value);
    }

    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return cleanXssString(value);
    }

    /**
     * XSS 공격 패턴을 제거하는 메서드
     * HTML 태그와 자바스크립트 코드를 안전하게 처리
     */
    private String cleanXssString(String value) {
        if (value == null) {
            return null;
        }

        // XSS 공격 패턴 제거
        value = SCRIPT_PATTERN.matcher(value).replaceAll("");
        value = JAVASCRIPT_PATTERN.matcher(value).replaceAll("");
        value = VBSCRIPT_PATTERN.matcher(value).replaceAll("");
        value = ONLOAD_PATTERN.matcher(value).replaceAll("");
        value = ONERROR_PATTERN.matcher(value).replaceAll("");
        value = ONCLICK_PATTERN.matcher(value).replaceAll("");
        value = EVAL_PATTERN.matcher(value).replaceAll("");
        value = EXPRESSION_PATTERN.matcher(value).replaceAll("");

        // HTML 특수문자 이스케이프 처리
        value = value.replaceAll("<", "&lt;")
                    .replaceAll(">", "&gt;")
                    .replaceAll("\"", "&quot;")
                    .replaceAll("'", "&#x27;")
                    .replaceAll("/", "&#x2F;");

        return value;
    }
}

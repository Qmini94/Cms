package kr.co.itid.cms.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * XSS 공격 방어 필터
 * 요청 파라미터와 헤더에서 악성 스크립트를 탐지하고 차단
 */
@Slf4j
@Component
public class XssProtectionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // XSS 방어를 위한 래퍼 요청 객체 생성
        XssProtectionRequestWrapper wrappedRequest = new XssProtectionRequestWrapper(request);
        
        // XSS 공격 패턴 탐지
        if (containsXssPattern(request)) {
            log.warn("[XSS 탐지] 의심스러운 요청이 차단되었습니다. IP: {}, URI: {}", 
                    getClientIp(request), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"잘못된 요청입니다.\"}");
            return;
        }
        
        filterChain.doFilter(wrappedRequest, response);
    }

    /**
     * XSS 공격 패턴 탐지
     */
    private boolean containsXssPattern(HttpServletRequest request) {
        // 요청 파라미터 검사
        if (request.getParameterMap() != null) {
            for (String[] values : request.getParameterMap().values()) {
                for (String value : values) {
                    if (value != null && containsXssKeywords(value)) {
                        return true;
                    }
                }
            }
        }
        
        // 헤더 검사 (User-Agent, Referer 등)
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && containsXssKeywords(userAgent)) {
            return true;
        }
        
        return false;
    }

    /**
     * XSS 키워드 패턴 검사
     */
    private boolean containsXssKeywords(String value) {
        if (value == null) return false;
        
        String lowerValue = value.toLowerCase();
        
        // 기본적인 XSS 패턴들
        String[] xssPatterns = {
            "<script", "</script>", "javascript:", "vbscript:", "onload=", "onerror=", 
            "onclick=", "onmouseover=", "onfocus=", "onblur=", "onchange=", "onsubmit=",
            "<iframe", "<object", "<embed", "<link", "<meta", "expression(",
            "alert(", "confirm(", "prompt(", "document.cookie", "document.write"
        };
        
        for (String pattern : xssPatterns) {
            if (lowerValue.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

package kr.co.itid.cms.util;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import static kr.co.itid.cms.constanrt.SecurityConstants.*;

/** 인증/세션 관련 쿠키/헤더 통합 유틸 (상수는 SecurityConstants에서만 관리) */
public final class AuthCookieUtil {

    private AuthCookieUtil() {}

    private static final Duration ZERO = Duration.ZERO;

    // -------------------- SESSION-EXPIRES --------------------
    public static void setSessionExpires(HttpServletResponse res, long expEpochSec) {
        setSessionExpires(res, expEpochSec, COOKIE_DOMAIN);
    }

    public static void setSessionExpires(HttpServletResponse res, long expEpochSec, String domain) {
        long nowSec = System.currentTimeMillis() / 1000;
        long remainSec = Math.max(0, expEpochSec - nowSec);

        ResponseCookie cookie = baseCookie(SESSION_EXPIRES_COOKIE_NAME, String.valueOf(expEpochSec), domain)
                .httpOnly(false) // 프론트에서 읽어 타이머 설정
                .maxAge(Duration.ofSeconds(remainSec))
                .build();

        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        res.setHeader(X_SESSION_EXPIRES_HEADER, String.valueOf(expEpochSec));
    }

    public static void clearSessionExpires(HttpServletResponse res) {
        clearSessionExpires(res, COOKIE_DOMAIN);
    }

    public static void clearSessionExpires(HttpServletResponse res, String domain) {
        ResponseCookie cookie = expiredCookie(SESSION_EXPIRES_COOKIE_NAME, domain, false);
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        res.setHeader(X_SESSION_EXPIRES_HEADER, "0");
    }

    // -------------------- ACCESS TOKEN --------------------
    public static void setAccessToken(HttpServletResponse res, String token, Duration ttl) {
        setAccessToken(res, token, ttl, COOKIE_DOMAIN);
    }

    public static void setAccessToken(HttpServletResponse res, String token, Duration ttl, String domain) {
        ResponseCookie.ResponseCookieBuilder b = baseCookie(ACCESS_TOKEN_COOKIE_NAME, safeValue(token), domain)
                .httpOnly(true);
        if (ttl != null) {
            b.maxAge(ttl); // ttl 없으면 세션쿠키(= maxAge 미설정)
        }
        res.addHeader(HttpHeaders.SET_COOKIE, b.build().toString());
    }

    public static void clearAccessToken(HttpServletResponse res) {
        clearAccessToken(res, COOKIE_DOMAIN);
    }

    public static void clearAccessToken(HttpServletResponse res, String domain) {
        ResponseCookie cookie = expiredCookie(ACCESS_TOKEN_COOKIE_NAME, domain, true);
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // -------------------- REFRESH TOKEN --------------------
    public static void setRefreshToken(HttpServletResponse res, String token, Duration ttl) {
        setRefreshToken(res, token, ttl, COOKIE_DOMAIN);
    }

    public static void setRefreshToken(HttpServletResponse res, String token, Duration ttl, String domain) {
        ResponseCookie.ResponseCookieBuilder b = baseCookie(REFRESH_TOKEN_COOKIE_NAME, safeValue(token), domain)
                .httpOnly(true);
        if (ttl != null) {
            b.maxAge(ttl);
        }
        res.addHeader(HttpHeaders.SET_COOKIE, b.build().toString());
    }

    public static void clearRefreshToken(HttpServletResponse res) {
        clearRefreshToken(res, COOKIE_DOMAIN);
    }

    public static void clearRefreshToken(HttpServletResponse res, String domain) {
        ResponseCookie cookie = expiredCookie(REFRESH_TOKEN_COOKIE_NAME, domain, true);
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // -------------------- 일괄 삭제(과거 속성 포함) --------------------
    /** 기본 도메인/경로로 표준 삭제 */
    public static void clearAll(HttpServletResponse res) {
        // COOKIE_DOMAIN이 null/빈값이면 'host-only' 삭제를 위해 null 원소를 가진 리스트를 사용
        clearAll(res, defaultDomains(), defaultPaths());
    }

    private static List<String> defaultDomains() {
        if (COOKIE_DOMAIN != null && !COOKIE_DOMAIN.isEmpty()) {
            return List.of(COOKIE_DOMAIN); // null 아님: 안전
        }
        // null 허용 리스트: host-only 쿠키 삭제 시도
        return Arrays.asList((String) null);
    }

    private static List<String> defaultPaths() {
        String path = (COOKIE_PATH != null && !COOKIE_PATH.isEmpty()) ? COOKIE_PATH : "/";
        return List.of(path);
    }

    /** 과거 도메인/경로까지 함께 삭제 */
    public static void clearAll(HttpServletResponse res, List<String> domains, List<String> paths) {
        clearOne(res, ACCESS_TOKEN_COOKIE_NAME,  true,  domains, paths);
        clearOne(res, REFRESH_TOKEN_COOKIE_NAME, true,  domains, paths);
        clearOne(res, SESSION_EXPIRES_COOKIE_NAME, false, domains, paths);
        res.setHeader(X_SESSION_EXPIRES_HEADER, "0");
    }

    // -------------------- 내부 공통 --------------------
    private static ResponseCookie.ResponseCookieBuilder baseCookie(String name, String value, String domain) {
        ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, value)
                .path(COOKIE_PATH)
                .secure(SECURE)
                .sameSite(SAME_SITE_LAX);
        if (domain != null && !domain.isEmpty()) {
            b.domain(domain);
        }
        return b;
    }

    private static ResponseCookie expiredCookie(String name, String domain, boolean httpOnly) {
        return baseCookie(name, "", domain)
                .httpOnly(httpOnly)
                .maxAge(ZERO) // 삭제
                .build();
    }

    private static void clearOne(HttpServletResponse res, String name, boolean httpOnly,
                                 List<String> domains, List<String> paths) {
        for (String path : paths) {
            for (String domain : domains) {
                ResponseCookie.ResponseCookieBuilder b = ResponseCookie.from(name, "")
                        .path(path != null ? path : COOKIE_PATH)
                        .secure(SECURE)
                        .sameSite(SAME_SITE_LAX)
                        .httpOnly(httpOnly)
                        .maxAge(ZERO);
                if (domain != null && !domain.isEmpty()) {
                    b.domain(domain);
                }
                res.addHeader(HttpHeaders.SET_COOKIE, b.build().toString());
            }
        }
    }

    private static String safeValue(String v) {
        if (v == null) return "";
        byte[] bytes = v.getBytes(StandardCharsets.UTF_8);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
package kr.co.itid.cms.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import kr.co.itid.cms.entity.cms.core.member.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static kr.co.itid.cms.constanrt.RedisConstants.DEFAULT_CACHE_TTL;
import static kr.co.itid.cms.constanrt.SecurityConstants.*;

/**
 * 요구조건 반영:
 * - Authorization 헤더 미사용, 쿠키 기반 ACCESS/REFRESH
 * - 블랙리스트 미사용(세션 삭제로 즉시 철회)
 * - REFRESH 발급/검증/쿠키, 삭제 쿠키 추가
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity; // seconds

    @Value("${jwt.fallback-token-validity}")
    private long fallbackTokenValidity; // seconds

    // REFRESH 토큰 만료 (초) - 기본 1일
    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    // 블랙리스트/Redis 의존성 제거
    private Key key;


    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /** ACCESS 토큰 생성 (기존 유지) */
    public String createToken(String userId, Map<String, Object> claims) {
        return createToken(userId, claims, false);
    }

    /**
     * JWT 토큰 생성
     * @param userId 사용자 ID
     * @param claims 토큰에 포함할 클레임
     * @param isRedisDown Redis 장애 여부 (true면 긴 만료 시간 사용)
     */
    public String createToken(String userId, Map<String, Object> claims, boolean isRedisDown) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        Date issuedAt = Date.from(now.toInstant());

        long validity = isRedisDown ? fallbackTokenValidity : accessTokenValidity;
        Date expiration = Date.from(now.plusSeconds(validity).toInstant());

        // jti 설정(표준 필드와 커스텀 둘 다 세팅)
        String jti = Optional.ofNullable((String) claims.get("jti")).orElse(UUID.randomUUID().toString());
        claims.put("jti", jti);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setId(jti)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** 로그인 시 기본 클레임 생성 (기존 유지) */
    public Map<String, Object> getClaims(Member member, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        Long idx = member.getIdx();
        int userLevel = member.getUserLevel();
        String userName = member.getUserName();

        claims.put("userLevel", userLevel);
        claims.put("userName", userName);
        claims.put("idx", idx);
        claims.put("sid", sessionId); // ★ sid 포함(세션 권위)
        claims.put("exp", DEFAULT_CACHE_TTL); // 커스텀 만료(기존 유지)
        return claims;
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** Refresh 토큰 만들기 (최소 클레임만) */
    public String createRefreshToken(String userId, String sessionId) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        Date iat = Date.from(now.toInstant());
        Date exp = Date.from(now.plusSeconds(refreshTokenValidity).toInstant());
        String jti = UUID.randomUUID().toString();

        Map<String, Object> claims = new HashMap<>();
        claims.put("sid", sessionId);
        claims.put("jti", jti);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setId(jti)
                .setIssuedAt(iat)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Refresh 토큰 파싱 */
    public Claims getClaimsFromRefreshToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** Refresh 토큰 검증 (만료/서명/스큐) */
    public void validateRefreshToken(String token) throws Exception {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .setAllowedClockSkewSeconds(60)
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw e; // 만료는 그대로 던짐
        } catch (JwtException | IllegalArgumentException e) {
            throw new Exception("Refresh token invalid", e);
        }
    }

    public String extractAccessTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /** Refresh 토큰 추출 */
    public String extractRefreshTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, token)
                .httpOnly(HTTP_ONLY)
                .secure(SECURE)
                .path("/")
                .maxAge(Duration.ofSeconds(accessTokenValidity))
                .sameSite(SAME_SITE_NONE) // ACCESS: SameSite=None
                .build();
    }

    /** Refresh 토큰 쿠키 생성 (SameSite=Strict 권장) */
    public ResponseCookie createRefreshTokenCookie(String token) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, token)
                .httpOnly(HTTP_ONLY)
                .secure(SECURE)
                .path("/") // 자동 재발급 고려 시 "/" 유지
                .maxAge(Duration.ofSeconds(refreshTokenValidity))
                .sameSite("Strict") // REFRESH: SameSite=Strict
                .build();
    }

    // 삭제 쿠키 (로그아웃 시 발급 속성과 동일하게 Max-Age=0)
    public ResponseCookie deleteAccessTokenCookie() {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
                .httpOnly(HTTP_ONLY)
                .secure(SECURE)
                .path("/")
                .maxAge(0)
                .sameSite(SAME_SITE_NONE)
                .build();
    }

    // 삭제 쿠키 (REFRESH)
    public ResponseCookie deleteRefreshTokenCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(HTTP_ONLY)
                .secure(SECURE)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
    }

    /**
     * ACCESS 검증: 서명/만료 검사만 수행
     * - 블랙리스트 미사용(요구사항: 세션 삭제로 즉시 철회)
     * - 만료는 ExpiredJwtException 그대로 던져 필터에서 재발급 분기
     */
    public void validateToken(String token) throws Exception {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .setAllowedClockSkewSeconds(60)
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw e; // 만료는 그대로 전달
        } catch (JwtException | IllegalArgumentException e) {
            throw new Exception("Token invalid", e);
        }
    }
}

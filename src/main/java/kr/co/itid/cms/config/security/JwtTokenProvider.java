package kr.co.itid.cms.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import kr.co.itid.cms.config.security.model.JwtProperties;
import kr.co.itid.cms.entity.cms.core.member.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static kr.co.itid.cms.constanrt.SecurityConstants.ACCESS_TOKEN_COOKIE_NAME;
import static kr.co.itid.cms.constanrt.SecurityConstants.REFRESH_TOKEN_COOKIE_NAME;

/**
 * 요구조건:
 * - Authorization 헤더 미사용, 쿠키 기반 ACCESS/REFRESH
 * - 블랙리스트 미사용(세션 삭제로 즉시 철회)
 * - 쿠키 생성/삭제는 AuthCookieUtil에서 처리 (여기선 JWT 발급/검증만)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private final JwtProperties props;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /** ACCESS 토큰 생성 (기본) */
    public String createToken(String userId, Map<String, Object> claims) {
        return createToken(userId, claims, false);
    }

    /**
     * ACCESS 토큰 생성
     * @param isRedisDown true면 fallback TTL 사용
     */
    public String createToken(String userId, Map<String, Object> claims, boolean isRedisDown) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        long validity = isRedisDown ? props.getFallbackTokenValidity() : props.getAccessTokenValidity();

        Date issuedAt = Date.from(now.toInstant());
        Date expiration = Date.from(now.plusSeconds(validity).toInstant());

        // jti
        String jti = Optional.ofNullable((String) claims.get("jti")).orElse(UUID.randomUUID().toString());
        claims.put("jti", jti);

        // 커스텀 만료(epoch seconds) - 프론트 타이머/헤더 동기화를 위해
        claims.put("exp", expiration.toInstant().getEpochSecond());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setId(jti)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration) // 표준 exp(Date)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** 로그인 시 기본 클레임 생성 */
    public Map<String, Object> getClaims(Member member, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("idx", member.getIdx());
        claims.put("userLevel", member.getUserLevel());
        claims.put("userName", member.getUserName());
        claims.put("sid", sessionId);      // 세션 식별자
        claims.put("cacheTtlSec", props.getSessionTtlSeconds());
        return claims;
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** Refresh 토큰 만들기 (필수 최소 클레임만) */
    public String createRefreshToken(String userId, String sessionId) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        Date iat = Date.from(now.toInstant());
        Date exp = Date.from(now.plusSeconds(props.getRefreshTokenValidity()).toInstant());
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
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            throw new Exception("Refresh token invalid", e);
        }
    }

    /** ACCESS 토큰 추출 (쿠키 기반) */
    public String extractAccessTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /** REFRESH 토큰 추출 (쿠키 기반) */
    public String extractRefreshTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * ACCESS 검증: 서명/만료 검사만 수행
     * - 블랙리스트 미사용(세션 삭제로 즉시 철회)
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

    /* ========= 헬퍼 ========= */

    /** 현재 시각 기준 ACCESS 만료 epoch(초) 계산 (Redis 장애 고려용) */
    public long computeAccessExpiryEpoch(boolean isRedisDown) {
        long validity = isRedisDown ? props.getFallbackTokenValidity() : props.getAccessTokenValidity();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        return now.plusSeconds(validity).toEpochSecond();
    }

    /** 현재 시각 기준 REFRESH 만료 epoch(초) 계산 */
    public long computeRefreshExpiryEpoch() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        return now.plusSeconds(props.getRefreshTokenValidity()).toEpochSecond();
    }
}
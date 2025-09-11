package kr.co.itid.cms.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.entity.cms.core.member.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static kr.co.itid.cms.constanrt.RedisConstants.BLACKLIST_KEY_PREFIX;
import static kr.co.itid.cms.constanrt.SecurityConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;
    
    @Value("${jwt.fallback-token-validity}")
    private long fallbackTokenValidity;

    private final StringRedisTemplate redisTemplate;
    private Key key;


    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(String userId, Map<String, Object> claims) {
        return createToken(userId, claims, false);
    }
    
    /**
     * JWT 토큰 생성
     * @param userId 사용자 ID
     * @param claims 토큰에 포함할 클레임
     * @param isRedisDown Redis 장애 여부 (true면 긴 만료 시간 사용)
     * @return JWT 토큰
     */
    public String createToken(String userId, Map<String, Object> claims, boolean isRedisDown) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        Date issuedAt = Date.from(now.toInstant());
        
        // Redis 장애 시 긴 만료 시간 사용
        long validity = isRedisDown ? fallbackTokenValidity : accessTokenValidity;
        Date expiration = Date.from(now.plusSeconds(validity).toInstant());

        claims.putIfAbsent("jti", UUID.randomUUID().toString());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 기존 토큰에서 새로운 토큰 생성 (만료 시간 갱신)
     */
    public String recreateTokenFrom(String oldToken) {
        Claims oldClaims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(oldToken)
                .getBody();

        String userId = oldClaims.getSubject();

        // 예약 필드는 제외하고 필요한 claims만 복사
        Map<String, Object> claims = new HashMap<>();
        for (Map.Entry<String, Object> entry : oldClaims.entrySet()) {
            String key = entry.getKey();
            if (!Set.of("sub", "iat", "exp", "nbf", "iss", "aud").contains(key)) {
                claims.put(key, entry.getValue());
            }
        }

        return createToken(userId, claims);
    }

    public Map<String, Object> getClaims(Member member, String sessionId) {
        Map<String, Object> claims = new HashMap<>();

        Long idx = member.getIdx();
        int userLevel = member.getUserLevel();
        String userName = member.getUserName();

        claims.put("userLevel", userLevel);
        claims.put("userName", userName);
        claims.put("idx", idx);
        claims.put("sid", sessionId); // 세션 ID 추가

        return claims;
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractAccessTokenFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
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
                .sameSite(SAME_SITE_NONE)  //Strict, Lax
                .build();
    }

    public String getJti(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("jti", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void validateToken(String token) throws Exception {
        if (isBlacklisted(token)) {
            throw new Exception("Token is blacklisted");
        }

        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .setAllowedClockSkewSeconds(60)
                    .build()
                    .parseClaimsJws(token);
        } catch (Exception e) {
            throw new Exception("Token invalid", e);
        }
    }

    public boolean isBlacklisted(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String jti = claims.get("jti", String.class);
            return jti != null && redisTemplate.hasKey(BLACKLIST_KEY_PREFIX + jti);
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }

    public void addTokenToBlacklist(String userJti) {
        redisTemplate.opsForValue().set(BLACKLIST_KEY_PREFIX + userJti, "true", accessTokenValidity, TimeUnit.SECONDS);
    }
}
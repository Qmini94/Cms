package kr.co.itid.cms.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.entity.cms.core.member.Member;
import kr.co.itid.cms.repository.cms.core.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static kr.co.itid.cms.config.common.redis.RedisConstants.BLACKLIST_KEY_PREFIX;
import static kr.co.itid.cms.config.security.SecurityConstants.*;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    private final StringRedisTemplate redisTemplate;
    private final MemberRepository memberService; // TODO: 나중에 service로 변경해야함
    private Key key;


    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(String userId, Map<String, Object> claims) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        Date issuedAt = Date.from(now.toInstant());
        Date expiration = Date.from(now.plusSeconds(accessTokenValidity).toInstant());

        claims.putIfAbsent("jti", UUID.randomUUID().toString());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

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

    public Map<String, Object> getClaims(Member member) {
        Map<String, Object> claims = new HashMap<>();

        Long idx = member.getIdx();
        int userLevel = member.getUserLevel();
        String userName = member.getUserName();

        claims.put("userLevel", userLevel);
        claims.put("userName", userName);
        claims.put("idx", idx);

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

    public void refreshIfNeeded(JwtAuthenticatedUser user) {
        if (user.isGuest() || user.isDev()) return;

        // 1. 항상 accessToken 재발급 (슬라이딩 유지)
        String newToken = recreateTokenFrom(user.token());
        ResponseCookie cookie = createAccessTokenCookie(newToken);

        HttpServletResponse response = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getResponse();

        if (response != null) {
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        // 2. 새 토큰을 기반으로 SecurityContext 갱신
        Claims claims = getClaimsFromToken(newToken);

        JwtAuthenticatedUser refreshedUser = new JwtAuthenticatedUser(
                claims.get("idx", Long.class),
                claims.getSubject(),
                claims.get("userName", String.class),
                claims.get("userLevel", Integer.class),
                newToken,
                user.hostname(),
                user.menuId()
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(refreshedUser, null, null);

        SecurityContextHolder.getContext().setAuthentication(authentication);
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
            return jti != null && Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_KEY_PREFIX + jti));
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }

    public void addTokenToBlacklist(String userJti) {
        redisTemplate.opsForValue().set(BLACKLIST_KEY_PREFIX + userJti, "true", accessTokenValidity, TimeUnit.SECONDS);
    }
}
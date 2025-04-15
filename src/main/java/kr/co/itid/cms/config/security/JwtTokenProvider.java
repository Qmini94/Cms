package kr.co.itid.cms.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import kr.co.itid.cms.entity.cms.base.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static kr.co.itid.cms.config.security.SecurityConstants.ACCESS_TOKEN_COOKIE_NAME;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    private final StringRedisTemplate redisTemplate;
    private Key key;

    public JwtTokenProvider(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(String userId, Map<String, Object> claims) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        Date issuedAt = Date.from(now.toInstant());
        Date expiration = Date.from(now.plusSeconds(accessTokenValidity).toInstant());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String recreateTokenFrom(String oldToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(oldToken)
                .getBody();

        return createToken(claims.getSubject(), claims);
    }

    public Map<String, Object> getClaims(Member member) {
        Map<String, Object> claims = new HashMap<>();

        int idx = member.getIdx();
        int userLevel = member.getUserLevel();
        String userName = member.getUserName();

        claims.put("userLevel", userLevel);
        claims.put("userName", userName);
        claims.put("idx", idx);

        return claims;
    }

    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    public ResponseCookie createAccessTokenCookie(String token) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofSeconds(accessTokenValidity))
                .sameSite("Strict")
                .build();
    }

    public boolean validateToken(String token) {
        try {
            if (isBlacklisted(token)) {
                System.err.println("블랙리스트에 등록된 토큰입니다.");
                return false;
            }

            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .setAllowedClockSkewSeconds(60)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.err.println("토큰 만료: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("지원하지 않는 토큰 형식: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.err.println("잘못된 JWT 구조: " + e.getMessage());
        } catch (io.jsonwebtoken.security.SecurityException e) {
            System.err.println("잘못된 서명(Security): " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("잘못된 인자: " + e.getMessage());
        }
        return false;
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
    }

    public String getUserId(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            return claims.getBody().getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getExpiration(String token) {
        Date exp = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getExpiration();
        return exp.getTime() - System.currentTimeMillis();
    }

    public void addToBlacklist(String token) {
        long remainingTime = getExpiration(token);
        redisTemplate.opsForValue().set("blacklist:" + token, "true", remainingTime, TimeUnit.MILLISECONDS);
    }
}
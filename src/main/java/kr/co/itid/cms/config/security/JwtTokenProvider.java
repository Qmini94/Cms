package kr.co.itid.cms.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import kr.co.itid.cms.entity.cms.core.Member;
import lombok.RequiredArgsConstructor;
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

import static kr.co.itid.cms.config.security.SecurityConstants.ACCESS_TOKEN_COOKIE_NAME;
import static kr.co.itid.cms.config.security.SecurityConstants.SAME_SITE_NONE;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    private final StringRedisTemplate redisTemplate;
    private Key key;


    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(String userId, Map<String, Object> claims) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        Date issuedAt = Date.from(now.toInstant());
        Date expiration = Date.from(now.plusSeconds(accessTokenValidity).toInstant());
        String jti = (String) claims.get("jti");
        if (jti != null && !jti.isEmpty()) {
            storeJtiToRedis(userId, jti, accessTokenValidity);
        }

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
        String newJti = UUID.randomUUID().toString();
        // 새로운 claims 복사 후 jti 갱신
        Map<String, Object> newClaims = new HashMap<>(oldClaims);
        newClaims.put("jti", newJti);

        String userId = oldClaims.getSubject();

        return createToken(userId, newClaims);
    }

    public Map<String, Object> getClaims(Member member) {
        Map<String, Object> claims = new HashMap<>();

        int idx = member.getIdx();
        int userLevel = member.getUserLevel();
        String userName = member.getUserName();
        String jti = UUID.randomUUID().toString();

        claims.put("jti", jti);
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
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofSeconds(accessTokenValidity))
                .sameSite(SAME_SITE_NONE)  //Strict, Lax
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
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String jti = claims.get("jti", String.class);
            return jti != null && Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + jti));

        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
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

    public void addAllTokensToBlacklist(String userId) {
        String userTokenKey = "user_tokens:" + userId;
        Set<String> jtiSet = redisTemplate.opsForSet().members(userTokenKey);

        if (jtiSet == null || jtiSet.isEmpty()) {
            return;
        }
        //TTL 오버헤드는 매우 낮기 때문에 토큰마다의 만료시간 계산하기보단 그냥 엑세스토큰의 만료시간을 다시 설정.
        for (String jti : jtiSet) {
            redisTemplate.opsForValue().set("blacklist:" + jti, "true", accessTokenValidity, TimeUnit.SECONDS);
        }
        redisTemplate.delete(userTokenKey);
    }


    private void storeJtiToRedis(String userId, String jti, long expirationMillis) {
        String key = "user_tokens:" + userId;
        redisTemplate.opsForSet().add(key, jti);
        redisTemplate.expire(key, expirationMillis, TimeUnit.SECONDS);

        Long size = redisTemplate.opsForSet().size(key);
        if (size != null && size > 20) {
            Set<String> allJtis = redisTemplate.opsForSet().members(key);
            int toRemove = size.intValue() - 20;

            if (allJtis != null && toRemove > 0) {
                Iterator<String> iter = allJtis.iterator();
                while (iter.hasNext() && toRemove-- > 0) {
                    redisTemplate.opsForSet().remove(key, iter.next());
                }
            }
        }
    }
}
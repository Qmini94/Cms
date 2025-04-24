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

    /* TODO: [보완 예정] user_tokens:{userId} 에서 제거된 jti를 별도 expired_jti:{jti} 키에 백업하도록 개선 고려
           - TTL(accessTokenValidity)과 함께 저장하여 만료되도록 설정
           - isBlacklisted() 메서드에서 blacklist:{jti} 외에 expired_jti:{jti}도 함께 검사
           - 현재는 최대 20개 jti만 유지되므로, 오래된 토큰이 살아있을 수 있음
           - 실무적으로 큰 문제는 없지만, 보안 커버리지를 높이기 위한 선택 사항
           - 장기적으로 Set → List 구조로 변경하여 jti 정렬 및 관리 방식 개선 여부도 판단 필요 */
    private void storeJtiToRedis(String userId, String jti, long expirationMillis) {
        String tokenKey = "user_tokens:" + userId;
        redisTemplate.opsForSet().add(tokenKey, jti);
        redisTemplate.expire(tokenKey, expirationMillis, TimeUnit.SECONDS);

        Long size = redisTemplate.opsForSet().size(tokenKey);
        if (size != null && size > 20) {
            Set<String> allJtis = redisTemplate.opsForSet().members(tokenKey);
            int toRemove = size.intValue() - 20;

            if (allJtis != null && toRemove > 0) {
                Iterator<String> iter = allJtis.iterator();
                while (iter.hasNext() && toRemove-- > 0) {
                    redisTemplate.opsForSet().remove(tokenKey, iter.next());
                }
            }
        }
    }
}
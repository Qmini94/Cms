package kr.co.itid.cms.config.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import kr.co.itid.cms.entity.cms.Member;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    private final StringRedisTemplate redisTemplate;
    private Key key;

    public JwtTokenProvider(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String userId, Map<String, String> claims, boolean isRefresh) {
        long now = System.currentTimeMillis();
        Date validity = new Date(now + (isRefresh ? refreshTokenValidity : accessTokenValidity));
        return Jwts.builder()
                .setSubject(userId)
                .setClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Map<String, String> getClaims(Member member) {
        Map<String, String> claims = new HashMap<>();

        int userLevel = member.getUserLevel();
        String userName = member.getUserName();

        claims.put("role", resolveRoles(userLevel));
        claims.put("userName", userName);

        return claims;
    }

    private String resolveRoles(Integer level) {
        if (level == 1) return "ROLE_ADMIN";
        else if (level == 6) return "ROLE_STAFF";
        else return "ROLE_USER";
    }

    public String getUserIdFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰에서 사용자 ID를 추출할 수 있음
            return e.getClaims().getSubject();
        } catch (Exception ex) {
            throw new RuntimeException("토큰 파싱 오류: " + ex.getMessage());
        }
    }

    public void storeRefreshToken(String userId, String refreshToken) {
        try {
            redisTemplate.opsForValue().set("refresh:" + userId, refreshToken, 1, TimeUnit.DAYS);
        } catch (Exception e) {
            throw new RuntimeException("리프레시 토큰 저장 중 오류 발생: " + e.getMessage());
        }
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public boolean validateRefreshToken(String userId, String refreshToken) {
        String storedRefreshToken = redisTemplate.opsForValue().get("refresh:" + userId);
        return storedRefreshToken != null && storedRefreshToken.equals(refreshToken);
    }

    public boolean validateToken(String token) {
        if (isBlacklisted(token)) return false;
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token));
    }

    public String getUserId(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    public long getExpiration(String token) {
        Date exp = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getExpiration();
        return exp.getTime() - System.currentTimeMillis();
    }
}


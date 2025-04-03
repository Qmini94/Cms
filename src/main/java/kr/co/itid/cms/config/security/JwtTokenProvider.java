package kr.co.itid.cms.config.security;

import io.jsonwebtoken.*;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    public String generateToken(String userId, Map<String, Object> claims, boolean isRefresh) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
        Date issuedAt = Date.from(now.toInstant());

        ZonedDateTime expirationTime = now.plusSeconds(isRefresh ? refreshTokenValidity : accessTokenValidity);
        Date validity = Date.from(expirationTime.toInstant());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(issuedAt)  // 발급 시간 (KST)
                .setExpiration(validity)  // 만료 시간 (KST)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Map<String, Object> getClaims(Member member) {
        Map<String, Object> claims = new HashMap<>();

        int idx = member.getIdx();
        int userLevel = member.getUserLevel();
        String userName = member.getUserName();

        claims.put("role", resolveRoles(userLevel));
        claims.put("userName", userName);
        claims.put("idx", idx);  // 숫자형 필드도 그대로 저장

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
        try {
            if (isBlacklisted(token)) {
                System.err.println("블랙리스트에 등록된 토큰입니다.");
                return false;
            }

            Jwts.parserBuilder()
                    .setSigningKey(key)  // 키 확인
                    .setAllowedClockSkewSeconds(300) // 허용 시간차: 5분 (300초)
                    .build()
                    .parseClaimsJws(token);
            System.out.println("토큰 유효성 검사 성공");
            return true;
        } catch (ExpiredJwtException e) {
            System.err.println("토큰 만료: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("지원하지 않는 토큰 형식: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.err.println("잘못된 JWT 구조: " + e.getMessage());
        } catch (SignatureException e) {
            System.err.println("잘못된 서명: " + e.getMessage());
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

            String userId = claims.getBody().getSubject();

            return userId;
        } catch (Exception e) {
            return null;
        }
    }

    public long getExpiration(String token) {
        Date exp = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getExpiration();
        return exp.getTime() - System.currentTimeMillis();
    }
}


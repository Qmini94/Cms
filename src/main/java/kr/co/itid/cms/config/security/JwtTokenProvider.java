package kr.co.itid.cms.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import kr.co.itid.cms.entity.cms.core.Member;
import kr.co.itid.cms.repository.cms.core.MemberRepository;
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

        // 먼저 changedClaim:{userId} Redis에 있는지 확인
        Boolean needFreshClaims = redisTemplate.hasKey("changedClaim:" + userId);

        Map<String, Object> claims;

        if (Boolean.TRUE.equals(needFreshClaims)) {
            //Redis에 changedClaim 있으면, DB에서 새로 조회해서 claims 만들기
            Optional<Member> optionalMember = memberService.findByUserId(userId);
            Member member = optionalMember.orElseThrow(() -> new RuntimeException("회원 정보를 찾을 수 없습니다."));
            claims = getClaims(member);

            //changedClaim Redis 키 삭제
            redisTemplate.delete("changedClaim:" + userId);
        } else {
            claims = new HashMap<>(oldClaims);
        }

        return createToken(userId, claims);
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

            String userId = claims.getSubject();
            return userId != null && Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + userId));
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

    public void addUserToBlacklist(String userId) {
        redisTemplate.opsForValue().set("blacklist:" + userId, "true", accessTokenValidity, TimeUnit.SECONDS);
    }

    public void deleteUserToBlacklist(String userId) {
        redisTemplate.delete("blacklist:" + userId);
    }
}
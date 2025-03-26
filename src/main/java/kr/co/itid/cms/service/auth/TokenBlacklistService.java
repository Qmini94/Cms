package kr.co.itid.cms.service.auth;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token, long expirationMillis) {
        redisTemplate.opsForValue().set("blacklist:" + token, "true", Duration.ofMillis(expirationMillis));
    }

    public void removeRefreshToken(String userId) {
        redisTemplate.delete("refresh:" + userId);
    }

    public void blockUserSession(String token, String userId, long expirationMillis) {
        blacklistToken(token, expirationMillis);
        removeRefreshToken(userId);
    }
}


package kr.co.itid.cms.service.auth.impl;

import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.service.auth.TokenBlacklistService;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service("tokenBlacklistService")
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl extends EgovAbstractServiceImpl implements TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private final LoggingUtil loggingUtil;

    @Override
    public void blacklistToken(String token, long expirationMillis) throws Exception {
        loggingUtil.logAttempt(Action.UPDATE, "Blacklisting token: " + token + ", expirationMillis=" + expirationMillis);
        try {
            redisTemplate.opsForValue().set("blacklist:" + token, "true", Duration.ofMillis(expirationMillis));
            loggingUtil.logSuccess(Action.UPDATE, "Token blacklisted: " + token);
        } catch (org.springframework.dao.DataAccessException e) {
            loggingUtil.logFail(Action.UPDATE, "Data access error while blacklisting token: " + e.getMessage());
            throw processException("Data access error while blacklisting token", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "Unexpected error while blacklisting token: " + e.getMessage());
            throw processException("Unexpected error while blacklisting token", e);
        }
    }

    @Override
    public void removeRefreshToken(String userId) throws Exception {
        loggingUtil.logAttempt(Action.DELETE, "Removing refresh token for userId: " + userId);
        try {
            redisTemplate.delete("refresh:" + userId);
            loggingUtil.logSuccess(Action.DELETE, "Refresh token removed for userId: " + userId);
        } catch (org.springframework.dao.DataAccessException e) {
            loggingUtil.logFail(Action.DELETE, "Data access error while removing refresh token: " + e.getMessage());
            throw processException("Data access error while removing refresh token", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.DELETE, "Unexpected error while removing refresh token: " + e.getMessage());
            throw processException("Unexpected error while removing refresh token", e);
        }
    }

    @Override
    public void blockUserSession(String token, String userId, long expirationMillis) throws Exception {
        loggingUtil.logAttempt(Action.UPDATE, "Blocking user session: userId=" + userId + ", token=" + token);
        try {
            blacklistToken(token, expirationMillis);
            removeRefreshToken(userId);
            loggingUtil.logSuccess(Action.UPDATE, "User session blocked for userId: " + userId);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "Error while blocking user session: " + e.getMessage());
            throw processException("Error while blocking user session", e);
        }
    }
}
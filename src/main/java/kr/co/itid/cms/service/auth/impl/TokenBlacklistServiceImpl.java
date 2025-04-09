package kr.co.itid.cms.service.auth.impl;

import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.service.auth.TokenBlacklistService;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.dao.DataAccessException;
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
        loggingUtil.logAttempt(Action.UPDATE, "Try to save token to blacklist: " + token);
        try {
            redisTemplate.opsForValue().set("blacklist:" + token, "true", Duration.ofMillis(expirationMillis));
            loggingUtil.logSuccess(Action.UPDATE, "Token saved to blacklist: " + token);
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.UPDATE, "Failed to save token to blacklist");
            throw processException("Cannot access database", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "Unexpected error while saving token");
            throw processException("Unexpected error", e);
        }
    }

    @Override
    public void removeRefreshToken(String userId) throws Exception {
        loggingUtil.logAttempt(Action.DELETE, "Try to delete refresh token: " + userId);
        try {
            redisTemplate.delete("refresh:" + userId);
            loggingUtil.logSuccess(Action.DELETE, "Refresh token deleted: " + userId);
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.DELETE, "Failed to delete refresh token");
            throw processException("Cannot access database", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.DELETE, "Unexpected error while deleting token");
            throw processException("Unexpected error", e);
        }
    }

    @Override
    public void blockUserSession(String token, String userId, long expirationMillis) throws Exception {
        loggingUtil.logAttempt(Action.UPDATE, "Try to block user session: " + userId);
        try {
            blacklistToken(token, expirationMillis);
            removeRefreshToken(userId);
            loggingUtil.logSuccess(Action.UPDATE, "User session blocked: " + userId);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "Failed to block user session: " + userId);
            throw processException("Failed to block user session", e);
        }
    }
}
package kr.co.itid.cms.service.auth.impl;

import kr.co.itid.cms.config.common.redis.RedisHealthChecker;
import kr.co.itid.cms.config.security.model.SessionData;
import kr.co.itid.cms.entity.cms.core.member.Member;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.service.auth.SessionManager;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static kr.co.itid.cms.constanrt.RedisConstants.DEFAULT_CACHE_TTL;
import static kr.co.itid.cms.constanrt.RedisConstants.SESSION_KEY_PREFIX;

/**
 * Redis 기반 세션 관리 서비스 구현체 (eGov 스타일 로깅/예외 통일)
 */
@Service("sessionManager")
@RequiredArgsConstructor
public class SessionManagerImpl extends EgovAbstractServiceImpl implements SessionManager {

    @Qualifier("sessionRedisTemplate")
    private final RedisTemplate<String, SessionData> redisTemplate;
    private final RedisHealthChecker redisHealthChecker;
    private final LoggingUtil loggingUtil;

    @Override
    public String createSession(Member member, String hostname) throws Exception {
        final String sid = UUID.randomUUID().toString();
        final SessionData sessionData = SessionData.builder()
                .userId(member.getUserId())
                .userLevel(member.getUserLevel())
                .idx(member.getIdx())
                .userName(member.getUserName())
                .hostname(hostname)
                .lastActivity(System.currentTimeMillis())
                .build();

        final String key = SESSION_KEY_PREFIX + sid;
        loggingUtil.logAttempt(Action.CREATE, "[Session] 세션 생성 시도: userId=" + member.getUserId());

        try {
            // DEFAULT_CACHE_TTL 초로 만료 설정
            redisTemplate.opsForValue().set(key, sessionData, DEFAULT_CACHE_TTL);
            loggingUtil.logSuccess(Action.CREATE, "[Session] 세션 생성 완료: sid=" + sid + ", userId=" + member.getUserId());
            return sid;
        } catch (Exception e) {
            loggingUtil.logFail(Action.CREATE, "[Session] 세션 생성 실패: userId=" + member.getUserId() + ", error=" + e.getMessage());
            throw processException("세션 생성 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public Optional<SessionData> getSession(String sid) {
        if (!redisHealthChecker.isCachedRedisHealthy()) {
            // 장애 시 조회 생략
            return Optional.empty();
        }

        final String key = SESSION_KEY_PREFIX + sid;
        loggingUtil.logAttempt(Action.RETRIEVE, "[Session] 세션 조회 시도: sid=" + sid);

        try {
            SessionData data = redisTemplate.opsForValue().get(key);
            if (data != null) {
                loggingUtil.logSuccess(Action.RETRIEVE, "[Session] 세션 조회 성공: sid=" + sid);
                return Optional.of(data);
            } else {
                loggingUtil.logFail(Action.RETRIEVE, "[Session] 세션 조회 결과 없음: sid=" + sid);
                return Optional.empty();
            }
        } catch (Exception e) {
            redisHealthChecker.forceUpdateHealthStatus(false);
            loggingUtil.logFail(Action.RETRIEVE, "[Session] 세션 조회 실패: sid=" + sid + ", error=" + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void deleteSession(String sid) throws Exception {
        final String key = SESSION_KEY_PREFIX + sid;
        loggingUtil.logAttempt(Action.DELETE, "[Session] 세션 삭제 시도: sid=" + sid);

        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                loggingUtil.logSuccess(Action.DELETE, "[Session] 세션 삭제 완료: sid=" + sid);
            } else {
                loggingUtil.logFail(Action.DELETE, "[Session] 삭제할 세션 없음: sid=" + sid);
            }
        } catch (Exception e) {
            redisHealthChecker.forceUpdateHealthStatus(false);
            loggingUtil.logFail(Action.DELETE, "[Session] 세션 삭제 실패: sid=" + sid + ", error=" + e.getMessage());
            throw processException("세션 삭제 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public long getSessionTtl(String sid) {
        if (!redisHealthChecker.isCachedRedisHealthy()) {
            return -1;
        }

        final String key = SESSION_KEY_PREFIX + sid;
        loggingUtil.logAttempt(Action.RETRIEVE, "[Session] 세션 TTL 조회 시도: sid=" + sid);

        try {
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            long seconds = (ttl == null ? -1 : ttl);
            loggingUtil.logSuccess(Action.RETRIEVE, "[Session] 세션 TTL 조회 성공: sid=" + sid + ", ttlSec=" + seconds);
            return seconds;
        } catch (Exception e) {
            redisHealthChecker.forceUpdateHealthStatus(false);
            loggingUtil.logFail(Action.RETRIEVE, "[Session] 세션 TTL 조회 실패: sid=" + sid + ", error=" + e.getMessage());
            return -1;
        }
    }

    @Override
    public void touchSession(String sid) throws Exception {
        // 슬라이딩 TTL: 세션 존재 시 lastActivity 갱신 + TTL 연장
        if (!redisHealthChecker.isCachedRedisHealthy()) {
            // 장애 상황에서는 조용히 패스 (요청 흐름 막지 않음)
            return;
        }

        final String key = SESSION_KEY_PREFIX + sid;
        loggingUtil.logAttempt(Action.UPDATE, "[Session] 세션 touch 시도: sid=" + sid);

        try {
            SessionData data = redisTemplate.opsForValue().get(key);
            if (data == null) {
                loggingUtil.logFail(Action.UPDATE, "[Session] touch 대상 세션 없음: sid=" + sid);
                return;
            }

            // lastActivity 갱신
            data.setLastActivity(System.currentTimeMillis());

            // 값 갱신 + TTL 재설정(슬라이딩)
            redisTemplate.opsForValue().set(key, data, DEFAULT_CACHE_TTL);
            // 또는 expire만 갱신하려면 아래 한 줄로도 충분:
            // redisTemplate.expire(key, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);

            loggingUtil.logSuccess(Action.UPDATE, "[Session] 세션 touch 완료: sid=" + sid + ", ttlSec=" + DEFAULT_CACHE_TTL);
        } catch (Exception e) {
            redisHealthChecker.forceUpdateHealthStatus(false);
            loggingUtil.logFail(Action.UPDATE, "[Session] 세션 touch 실패: sid=" + sid + ", error=" + e.getMessage());
            throw processException("세션 갱신 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public boolean isRedisHealthy() {
        // 상태 조회는 단순 위임 (로깅 불필요)
        return redisHealthChecker.isRedisHealthy();
    }
}
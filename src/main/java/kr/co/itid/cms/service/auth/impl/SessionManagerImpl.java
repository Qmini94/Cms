package kr.co.itid.cms.service.auth.impl;

import kr.co.itid.cms.config.common.redis.RedisHealthChecker;
import kr.co.itid.cms.config.security.model.SessionData;
import kr.co.itid.cms.entity.cms.core.member.Member;
import kr.co.itid.cms.service.auth.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static kr.co.itid.cms.constanrt.RedisConstants.SESSION_KEY_PREFIX;

/**
 * Redis 기반 세션 관리 서비스 구현체
 */
@Service("sessionManager")
@RequiredArgsConstructor
@Slf4j
public class SessionManagerImpl implements SessionManager {
    
    @Qualifier("sessionRedisTemplate")
    private final RedisTemplate<String, SessionData> redisTemplate;
    private final RedisHealthChecker redisHealthChecker;
    
    private static final Duration SESSION_TTL = Duration.ofHours(1); // 1시간
    
    @Override
    public String createSession(Member member, String hostname) {
        String sid = UUID.randomUUID().toString();
        
        SessionData sessionData = SessionData.builder()
                .userId(member.getUserId())
                .userLevel(member.getUserLevel())
                .idx(member.getIdx())
                .userName(member.getUserName())
                .hostname(hostname)
                .lastActivity(System.currentTimeMillis())
                .build();
        
        try {
            String key = SESSION_KEY_PREFIX + sid;
            redisTemplate.opsForValue().set(key, sessionData, SESSION_TTL);
            log.info("[Session] 세션 생성 완료: sid={}, userId={}", sid, member.getUserId());
            return sid;
            
        } catch (Exception e) {
            log.error("[Session] 세션 생성 실패: userId={}, error={}", member.getUserId(), e.getMessage());
            throw new RuntimeException("세션 생성에 실패했습니다", e);
        }
    }
    
    @Override
    public boolean extendSession(String sid) {
        if (!redisHealthChecker.isCachedRedisHealthy()) {
            log.debug("[Session] Redis 장애로 세션 연장 스킵: sid={}", sid);
            return false; // Redis 장애 시 JWT 만료 시간 연장 전략 사용
        }
        
        try {
            String key = SESSION_KEY_PREFIX + sid;
            
            // 세션 존재 확인
            if (!redisTemplate.hasKey(key)) {
                log.debug("[Session] 존재하지 않는 세션: sid={}", sid);
                return false;
            }
            
            // TTL 연장
            redisTemplate.expire(key, SESSION_TTL);
            log.debug("[Session] 세션 TTL 연장 완료: sid={}", sid);
            return true;
            
        } catch (Exception e) {
            log.warn("[Session] 세션 연장 실패: sid={}, error={}", sid, e.getMessage());
            redisHealthChecker.forceUpdateHealthStatus(false);
            return false;
        }
    }
    
    @Override
    public Optional<SessionData> getSession(String sid) {
        if (!redisHealthChecker.isCachedRedisHealthy()) {
            return Optional.empty();
        }
        
        try {
            String key = SESSION_KEY_PREFIX + sid;
            SessionData sessionData = redisTemplate.opsForValue().get(key);
            
            if (sessionData != null) {
                log.debug("[Session] 세션 조회 성공: sid={}, userId={}", sid, sessionData.getUserId());
            }
            
            return Optional.ofNullable(sessionData);
            
        } catch (Exception e) {
            log.warn("[Session] 세션 조회 실패: sid={}, error={}", sid, e.getMessage());
            redisHealthChecker.forceUpdateHealthStatus(false);
            return Optional.empty();
        }
    }
    
    @Override
    public void deleteSession(String sid) {
        try {
            String key = SESSION_KEY_PREFIX + sid;
            Boolean deleted = redisTemplate.delete(key);
            
            if (deleted) {
                log.info("[Session] 세션 삭제 완료: sid={}", sid);
            } else {
                log.debug("[Session] 삭제할 세션이 없음: sid={}", sid);
            }
            
        } catch (Exception e) {
            log.error("[Session] 세션 삭제 실패: sid={}, error={}", sid, e.getMessage());
            redisHealthChecker.forceUpdateHealthStatus(false);
        }
    }
    
    @Override
    public long getSessionTtl(String sid) {
        if (!redisHealthChecker.isCachedRedisHealthy()) {
            return -1;
        }
        
        try {
            String key = SESSION_KEY_PREFIX + sid;
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            log.warn("[Session] TTL 조회 실패: sid={}, error={}", sid, e.getMessage());
            redisHealthChecker.forceUpdateHealthStatus(false);
            return -1;
        }
    }
    
    @Override
    public boolean isRedisHealthy() {
        return redisHealthChecker.isRedisHealthy();
    }
}

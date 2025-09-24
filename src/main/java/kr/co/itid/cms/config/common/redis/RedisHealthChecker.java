package kr.co.itid.cms.config.common.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static kr.co.itid.cms.constanrt.RedisConstants.*;

/**
 * Redis 서버 상태 확인 컴포넌트
 * 장애 감지 시 JWT 만료 시간 연장 등의 대응 전략 지원
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisHealthChecker {
    
    private final StringRedisTemplate redisTemplate;
    private final AtomicBoolean isRedisHealthy = new AtomicBoolean(true);

    private static final Duration HEALTH_CHECK_TIMEOUT = Duration.ofMillis(500);
    
    /**
     * Redis 서버 상태 확인
     * @return Redis 서버가 정상이면 true, 장애 시 false
     */
    public boolean isRedisHealthy() {
        try {
            // 간단한 SET/GET 테스트로 Redis 상태 확인
            redisTemplate.opsForValue().set(HEALTH_CHECK_KEY, HEALTH_CHECK_VALUE, HEALTH_CHECK_TIMEOUT);
            String result = redisTemplate.opsForValue().get(HEALTH_CHECK_KEY);
            
            boolean healthy = HEALTH_CHECK_VALUE.equals(result);
            updateHealthStatus(healthy);
            return healthy;
            
        } catch (Exception e) {
            log.warn("[Redis Health] Redis 서버 연결 실패: {}", e.getMessage());
            updateHealthStatus(false);
            return false;
        }
    }
    
    /**
     * 캐시된 Redis 상태 반환 (성능 최적화)
     */
    public boolean isCachedRedisHealthy() {
        return isRedisHealthy.get();
    }
    
    /**
     * Redis 상태 업데이트 및 로깅
     */
    private void updateHealthStatus(boolean healthy) {
        boolean previousStatus = isRedisHealthy.getAndSet(healthy);
        
        // 상태 변경 시에만 로그 출력
        if (previousStatus != healthy) {
            if (healthy) {
                log.info("[Redis Health] Redis 서버 복구 완료");
            } else {
                log.error("[Redis Health] Redis 서버 장애 감지 - Fallback 모드 활성화");
            }
        }
    }
    
    /**
     * 강제로 Redis 상태를 업데이트 (테스트 용도)
     */
    public void forceUpdateHealthStatus(boolean healthy) {
        updateHealthStatus(healthy);
    }
}

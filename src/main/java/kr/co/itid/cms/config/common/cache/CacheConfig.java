package kr.co.itid.cms.config.common.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 캐시 설정
 * Spring Cache를 활성화하고 캐시 매니저를 설정합니다.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 캐시 매니저 설정
     * Simple Cache를 사용하여 메모리 기반 캐시를 제공합니다.
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // 캐시 이름들을 미리 정의
        cacheManager.setCacheNames(java.util.Arrays.asList(
            "sites",
            "boardMasters", 
            "boardMaster",
            "boardFieldDefinitions",
            "activeBoardMasters"
        ));
        
        // 캐시 생성 허용 (동적으로 캐시 생성 가능)
        cacheManager.setAllowNullValues(false);
        
        return cacheManager;
    }
}

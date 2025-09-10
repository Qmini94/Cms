package kr.co.itid.cms.config.common.redis;

import kr.co.itid.cms.config.security.model.SessionData;
import kr.co.itid.cms.service.auth.model.MenuPermissionData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정
 * SessionData, MenuPermissionData 객체의 직렬화/역직렬화를 위한 RedisTemplate 설정
 */
@Configuration
public class RedisConfig {
    
    /**
     * SessionData 전용 RedisTemplate
     * JSON 직렬화를 사용하여 가독성과 호환성 확보
     */
    @Bean("sessionRedisTemplate")
    public RedisTemplate<String, SessionData> sessionRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, SessionData> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Key는 String으로 직렬화
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Value는 JSON으로 직렬화 (SessionData 객체)
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
    
    /**
     * MenuPermissionData 전용 RedisTemplate (기존 권한 시스템용)
     * 기존 코드 호환성을 위해 유지
     */
    @Bean
    @Primary
    public RedisTemplate<String, MenuPermissionData> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, MenuPermissionData> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Key는 String으로 직렬화
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Value는 JSON으로 직렬화 (MenuPermissionData 객체)
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
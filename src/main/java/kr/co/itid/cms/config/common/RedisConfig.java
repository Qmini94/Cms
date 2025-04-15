package kr.co.itid.cms.config.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.itid.cms.service.auth.model.MenuPermissionData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, MenuPermissionData> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, MenuPermissionData> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Jackson Serializer 설정
        Jackson2JsonRedisSerializer<MenuPermissionData> serializer = new Jackson2JsonRedisSerializer<>(MenuPermissionData.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(mapper);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }
}
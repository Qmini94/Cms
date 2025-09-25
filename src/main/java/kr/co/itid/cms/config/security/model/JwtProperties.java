package kr.co.itid.cms.config.security.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    /** seconds */
    private long accessTokenValidity;
    /** seconds (Redis 장애 시 ACCESS fallback) */
    private long fallbackTokenValidity;
    /** seconds */
    private long refreshTokenValidity;
    /** seconds (세션 슬라이딩 TTL: SID 기준 만료) */
    private long sessionTtlSeconds;
}

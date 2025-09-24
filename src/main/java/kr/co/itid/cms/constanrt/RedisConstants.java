package kr.co.itid.cms.constanrt;

import java.time.Duration;

public final class RedisConstants {

    private RedisConstants() {} // 생성자 private 처리

    public static final String PERMISSION_KEY_PREFIX = "perm:menu:";
    public static final String SESSION_KEY_PREFIX = "sess:";
    public static final String HEALTH_CHECK_KEY = "health:check";

    public static final String HEALTH_CHECK_VALUE = "ok";

    public static final Duration DEFAULT_CACHE_TTL = Duration.ofHours(1); //로그인 만료시간.
    public static final Duration PERMISSION_TTL = Duration.ofDays(1);
}

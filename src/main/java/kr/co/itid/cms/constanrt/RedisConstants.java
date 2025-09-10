package kr.co.itid.cms.constanrt;

import java.time.Duration;

public final class RedisConstants {

    private RedisConstants() {} // 생성자 private 처리

    public static final String PERMISSION_KEY_PREFIX = "perm:menu:";
    public static final String BLACKLIST_KEY_PREFIX = "blacklist:";
    public static final String SESSION_KEY_PREFIX = "sess:";

    public static final Duration DEFAULT_CACHE_TTL = Duration.ofHours(1);
    public static final Duration PERMISSION_TTL = Duration.ofDays(1);
}

package kr.co.itid.cms.config.security;

import java.util.Set;

public class AdminBypassPaths {
    public static final Set<String> ALLOWED_PATHS = Set.of(
            "/api/site/list/all"
    );
}

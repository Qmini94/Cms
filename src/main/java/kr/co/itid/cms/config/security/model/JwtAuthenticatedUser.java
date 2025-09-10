package kr.co.itid.cms.config.security.model;

import java.io.Serializable;

public record JwtAuthenticatedUser(
        Long userIdx,
        String userId,
        String userName,
        int userLevel,
        Long exp,
        String token,
        String hostname,
        Long menuId,
        String sessionId  // 세션 ID 추가
) implements Serializable {

    public boolean isAdmin() {
        return userLevel == 1;
    }

    public boolean isGuest() {
        return userIdx != null && userIdx == -1;
    }

    public boolean isDev() {
        return userIdx != null && userIdx == 0;
    }
}
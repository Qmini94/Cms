package kr.co.itid.cms.dto.auth;

import lombok.*;

@Getter
@Builder
public class UserInfoResponse {
    private final String userName;
    private final String userId;
    private final String level;
    private final Integer idx;
}
package kr.co.itid.cms.dto.auth;

import lombok.*;

@Getter
@Builder
public class UserInfoResponse {
    private final String userName;
    private final String userId;
    private final String level;
    private final Integer idx;
    private final Long exp;
    private final Long idleRemainSec;  // Redis TTL 기반 idle 남은 시간 (초)
}
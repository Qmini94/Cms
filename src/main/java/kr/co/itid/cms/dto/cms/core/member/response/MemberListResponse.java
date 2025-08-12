package kr.co.itid.cms.dto.cms.core.member.response;

import lombok.*;
import java.time.LocalDateTime;

/**
 * 회원 목록 조회용 응답 DTO
 * - 리스트 화면에 필요한 최소 정보 위주
 */
@Getter
@Builder
public class MemberListResponse {
    private Long idx;
    private String userId;
    private String userName;
    private String email;
    private String tel;
    private Integer userLevel;        // 예: 1=ADMIN, 2=USER 등
    private Boolean isUse;            // 사용 여부
    private LocalDateTime createdDate;
    private LocalDateTime lastLoginDate;
}
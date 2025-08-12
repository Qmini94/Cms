package kr.co.itid.cms.dto.cms.core.member.response;

import lombok.*;
import java.time.LocalDateTime;

/**
 * 회원 단건 조회용 응답 DTO
 * - 상세 화면용
 */
@Getter
@Builder
public class MemberResponse {
    private Long idx;
    private String userId;
    private String userName;
    private String email;
    private String tel;
    private Integer userLevel;
    private Boolean isUse;

    // 선택 정보 (엔티티에 있다면 매핑)
    private LocalDateTime createdDate;
    private LocalDateTime lastLoginDate;
}
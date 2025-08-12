package kr.co.itid.cms.dto.cms.core.member.request;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * 회원 수정 요청
 * - 필요한 항목을 선택적으로 수정(전체 수정 가능)
 * - 삭제 기능은 제공하지 않음
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class MemberUpdateRequest {

    @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
    private String userName;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
    private String email;

    @Size(max = 50, message = "전화번호는 50자 이하여야 합니다.")
    private String tel;

    // 예: 1=ADMIN, 2=USER 등 프로젝트 정책에 맞춰 사용
    @Min(value = 0, message = "userLevel은 0 이상이어야 합니다.")
    private Integer userLevel;

    // 사용 여부
    private Boolean isUse;
}


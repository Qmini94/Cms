package kr.co.itid.cms.dto.cms.core.member.request;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 회원 생성 요청
 * - userId, userName만 입력
 * - 비밀번호는 userId와 동일한 평문을 서비스에서 암호화하여 저장
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class MemberCreateRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 4, max = 50, message = "아이디는 4~50자여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "아이디는 영문/숫자/.-_ 만 사용할 수 있습니다.")
    private String userId;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
    private String userName;
}

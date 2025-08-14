package kr.co.itid.cms.dto.auth.permission;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Builder
@Jacksonized
public class PermissionSubjectDto {
    private final String name;

    @NotBlank(message = "subject.value은 필수입니다.")
    @Size(max = 80, message = "subject.value은 80자 이내여야 합니다.")
    private final String value;

    @NotNull(message = "subject.type은 필수입니다.")
    private final SubjectType type;
}

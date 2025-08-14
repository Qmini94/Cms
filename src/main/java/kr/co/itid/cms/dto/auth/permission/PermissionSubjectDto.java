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
    @NotBlank(message = "subject.key는 필수입니다.")
    private final String key;

    @NotNull(message = "subject.id는 필수입니다.")
    private final Object id;

    @NotBlank(message = "subject.name은 필수입니다.")
    @Size(max = 80, message = "subject.name은 80자 이내여야 합니다.")
    private final String name;

    @NotNull(message = "subject.type은 필수입니다.")
    private final SubjectType type;
}

package kr.co.itid.cms.dto.auth.permission;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Builder
@Jacksonized
public class PermissionEntryDto {
    @Valid
    @NotNull(message = "subject는 필수입니다.")
    private final PermissionSubjectDto subject;

    @Min(value = 0, message = "sort는 0 이상이어야 합니다.")
    @Max(value = 100, message = "sort 값이 비정상적으로 큽니다.")
    private final int sort;

    @NotNull(message = "permissions는 필수입니다.")
    @Size(max = 32, message = "permissions 키가 너무 많습니다.")
    private final java.util.Map<String, Boolean> permissions;
}
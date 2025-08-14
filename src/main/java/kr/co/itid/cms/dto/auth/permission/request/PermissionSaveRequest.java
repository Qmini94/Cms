package kr.co.itid.cms.dto.auth.permission.request;

import kr.co.itid.cms.dto.auth.permission.PermissionEntryDto;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 현재 메뉴의 권한 일괄 저장(업서트) 요청
 * - menuId는 필수
 * - entries는 0~N개(정책에 맞게 max 값 조정)
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class PermissionSaveRequest {

    @NotNull(message = "menuId는 필수입니다.")
    private Long menuId;

    @Valid
    @NotNull(message = "entries는 null일 수 없습니다.")
    @Size(max = 100, message = "entries가 너무 많습니다.")
    private List<PermissionEntryDto> entries;
}
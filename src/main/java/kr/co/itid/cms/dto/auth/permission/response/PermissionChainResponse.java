package kr.co.itid.cms.dto.auth.permission.response;

import kr.co.itid.cms.dto.auth.permission.PermissionEntryDto;
import lombok.*;

import java.util.List;

@Getter
@Builder
public class PermissionChainResponse {
    private List<PermissionEntryDto> current;
    private List<PermissionEntryDto> inherited;
}

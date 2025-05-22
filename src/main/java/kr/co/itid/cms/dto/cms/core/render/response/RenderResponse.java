package kr.co.itid.cms.dto.cms.core.render.response;

import kr.co.itid.cms.dto.auth.UserPermissionResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RenderResponse {
    private String type;
    private String value;
    private Object data;
    private UserPermissionResponse permission;
}

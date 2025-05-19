package kr.co.itid.cms.dto.cms.core.render;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RenderResponse {
    private String type;
    private Object data;
}

package kr.co.itid.cms.dto.cms.core.render.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RenderResponse {
    private String type;
    private String boardId;
    private Object data;
}

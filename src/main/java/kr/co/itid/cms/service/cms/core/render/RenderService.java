package kr.co.itid.cms.service.cms.core.render;

import kr.co.itid.cms.dto.cms.core.render.response.RenderResponse;

public interface RenderService {
    RenderResponse getRenderData() throws Exception;
}

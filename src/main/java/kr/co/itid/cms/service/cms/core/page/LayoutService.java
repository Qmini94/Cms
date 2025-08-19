package kr.co.itid.cms.service.cms.core.page;

import kr.co.itid.cms.dto.cms.core.render.request.LayoutPreviewRequest;
import kr.co.itid.cms.dto.cms.core.render.request.LayoutSaveRequest;
import kr.co.itid.cms.enums.LayoutKind;

import javax.servlet.http.HttpServletRequest;

public interface LayoutService {
    String renderPreview(LayoutPreviewRequest req, HttpServletRequest httpReq);
    String getPublishedHtml(String siteCode, LayoutKind kind);
    boolean  save(LayoutSaveRequest req);
}

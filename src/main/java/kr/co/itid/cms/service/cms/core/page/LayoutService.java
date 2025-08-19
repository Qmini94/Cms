package kr.co.itid.cms.service.cms.core.page;

import kr.co.itid.cms.dto.cms.core.render.request.LayoutSaveRequest;
import kr.co.itid.cms.enums.LayoutKind;

public interface LayoutService {
    String getPublishedHtml(String siteCode, LayoutKind kind);
    boolean  save(LayoutSaveRequest req);
}

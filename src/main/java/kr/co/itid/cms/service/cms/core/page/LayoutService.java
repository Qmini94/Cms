package kr.co.itid.cms.service.cms.core.page;

import kr.co.itid.cms.enums.LayoutKind;

public interface LayoutService {
    String getPublishedHtml(String siteCode, LayoutKind kind);
    String getPublishedCss(String siteCode, LayoutKind kind);
}

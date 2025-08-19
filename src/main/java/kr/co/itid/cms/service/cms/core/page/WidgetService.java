package kr.co.itid.cms.service.cms.core.page;

import kr.co.itid.cms.util.WidgetCtx;

public interface WidgetService {
    String render(String pageHtml, WidgetCtx ctx);
}

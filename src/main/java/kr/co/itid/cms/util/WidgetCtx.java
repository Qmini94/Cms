package kr.co.itid.cms.util;

import javax.servlet.http.HttpServletRequest;

public record WidgetCtx(String site, String path, HttpServletRequest req) {
    public long extractIdFromPath() {
        try {
            String[] seg = path.split("/");
            return Long.parseLong(seg[seg.length-1]);
        } catch(Exception e) { return -1; }
    }
}

package kr.co.itid.cms.dto.cms.core.render.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageSaveRequest {
    private String site;            // siteName or domain or hostName
    private String path;            // e.g. /www or /notice/123
    private String contentHtml;     // LONGTEXT
    private String author;          // optional
    private Boolean publishNow;     // 현재버전으로 설정
}

package kr.co.itid.cms.dto.cms.core.render.request;

import kr.co.itid.cms.enums.LayoutKind;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LayoutSaveRequest {
    private String site;          // siteName or domain or hostName
    private LayoutKind kind;      // MAIN / SUB
    private String html;          // LONGTEXT
    private String css;           // LONGTEXT
    private Integer version;      // optional
    private Boolean publishNow;   // 즉시 공개여부
}

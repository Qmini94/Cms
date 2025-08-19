package kr.co.itid.cms.dto.cms.core.render.request;

import kr.co.itid.cms.enums.LayoutKind;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class LayoutSaveRequest {
    @NotNull
    private Long siteIdx;     // ★ site 식별자 (idx)

    @NotNull
    private LayoutKind kind;  // MAIN / SUB

    @NotNull
    private String html;      // 레이아웃 HTML (cms-slot 포함)      // siteName or domain or hostName

    private List<String> cssUrls;       // 허용 CSS 경로 목록
    private List<String> jsUrls;        // 허용 JS 경로 목록
    private Integer version;      // optional
    private Boolean publishNow;   // 즉시 공개여부
}

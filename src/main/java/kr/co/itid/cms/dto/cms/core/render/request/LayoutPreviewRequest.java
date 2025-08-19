package kr.co.itid.cms.dto.cms.core.render.request;

import kr.co.itid.cms.enums.LayoutKind;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class LayoutPreviewRequest {
    @NotNull
    private Long siteIdx;          // 어떤 사이트로 렌더할지

    @NotNull
    private LayoutKind kind;       // MAIN / SUB

    @NotNull
    private String layoutHtml;     // 레이아웃 HTML(슬롯 포함)

    private String inlineCss;               // 선택: 인라인 CSS
    private List<String> cssUrls;           // 허용 CSS 링크
    private List<String> jsUrls;            // 허용 JS 링크
    private String pageHtml;                // 페이지 본문(위젯 토큰 포함)
    private String path = "/preview";       // 위젯 ctx용 경로(상세 id 추출 등에 사용)
}

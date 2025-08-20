package kr.co.itid.cms.dto.cms.core.render.request;

import kr.co.itid.cms.enums.LayoutKind;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import javax.validation.constraints.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter @Setter @ToString
public class LayoutPreviewRequest {

    @NotNull(message = "siteIdx is required")
    private Long siteIdx;

    @NotNull(message = "kind is required")
    private LayoutKind kind;

    // 선택: 비면 "/preview"를 기본값으로 사용
    @Size(max = 255, message = "path too long")
    private String path;

    // 레이아웃 원본 HTML (빈 문자열 허용, null이면 "")
    @Size(max = 500_000, message = "layoutHtml too long")
    private String layoutHtml;

    // 선택(개발 편의). 운영에서 pageHtml 미사용이면 비워서 보내면 됨.
    @Size(max = 500_000, message = "pageHtml too long")
    private String pageHtml;

    // URL 리스트: null → 빈 리스트
    private List<@Size(max = 2048, message = "url too long") String> cssUrls;
    private List<@Size(max = 2048, message = "url too long") String> jsUrls;

    // 인라인 CSS (미리보기 한정)
    @Size(max = 200_000, message = "inlineCss too long")
    private String inlineCss;

    /* ----------------- 정규화/기본값 헬퍼 ----------------- */

    public String normalizedPath() {
        return (path == null || path.isBlank()) ? "/preview" : path.trim();
    }

    public String safeLayoutHtml() {
        return layoutHtml == null ? "" : layoutHtml;
    }

    public String safePageHtml() {
        return pageHtml == null ? "" : pageHtml;
    }

    public String safeInlineCss() {
        return inlineCss == null ? "" : inlineCss;
    }

    public List<String> normalizedCssUrls() {
        return normList(cssUrls);
    }
    public List<String> normalizedJsUrls() {
        return normList(jsUrls);
    }

    private static List<String> normList(List<String> in) {
        if (in == null) return Collections.emptyList();
        return in.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
package kr.co.itid.cms.dto.cms.core.template.request;

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
public class LayoutSaveRequest {

    @NotNull(message = "siteIdx is required")
    private Long siteIdx;

    @NotNull(message = "kind is required")
    private LayoutKind kind;

    @NotBlank(message = "html is required")
    @Size(max = 1_000_000, message = "html too long")
    private String html;

    @Size(max = 200_000, message = "css too long")
    private String css; // 선택(인라인)

    private List<@Size(max = 2048, message = "url too long") String> cssUrls;
    private List<@Size(max = 2048, message = "url too long") String> jsUrls;

    /* ----------------- 정규화/기본값 헬퍼 ----------------- */

    public String safeCss() { return css == null ? "" : css; }

    public List<String> normalizedCssUrls() { return normList(cssUrls); }
    public List<String> normalizedJsUrls()  { return normList(jsUrls); }

    private static List<String> normList(List<String> in) {
        if (in == null) return Collections.emptyList();
        return in.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
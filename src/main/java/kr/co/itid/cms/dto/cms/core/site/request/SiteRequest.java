package kr.co.itid.cms.dto.cms.core.site.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class SiteRequest {

    private Integer idx;

    @NotBlank(message = "사이트명은 필수입니다.")
    @Size(max = 60, message = "사이트명은 60자 이내여야 합니다.")
    private String siteName;

    @Size(max = 20, message = "호스트명은 20자 이내여야 합니다.")
    private String siteHostName;

    @NotBlank(message = "도메인은 필수입니다.")
    @Pattern(
            regexp = "^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "올바른 도메인 형식이어야 합니다. (예: example.com)"
    )
    @Size(max = 40, message = "도메인은 40자 이내여야 합니다.")
    private String siteDomain;

    @NotBlank(message = "운영방식은 필수입니다.")
    @Pattern(regexp = "open|close", message = "운영방식은 open 또는 close만 허용됩니다.")
    @Size(max = 10, message = "운영방식은 10자 이내여야 합니다.")
    private String siteOption;

    @Size(max = 65535, message = "허용 IP는 너무 길 수 없습니다.")
    private String allowIp;

    @Size(max = 65535, message = "차단 IP는 너무 길 수 없습니다.")
    private String denyIp;
}
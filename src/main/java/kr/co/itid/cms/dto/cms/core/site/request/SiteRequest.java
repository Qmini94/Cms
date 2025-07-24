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
    @Size(max = 100, message = "사이트명은 100자 이내여야 합니다.")
    private String siteName;

    private String siteHostName;

    @NotBlank(message = "도메인은 필수입니다.")
    @Pattern(
            regexp = "^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",
            message = "올바른 도메인 형식이어야 합니다. (예: example.com)"
    )
    private String siteDomain;
    private String sitePort;

    @NotBlank(message = "운영방식은 필수입니다.")
    @Pattern(regexp = "open|close", message = "운영방식은 open 또는 close만 허용됩니다.")
    private String siteOption;

    private String language;
    private String siren24Id;
    private String siren24No;
    private String umsId;
    private String umsKey;
    private String privacyCheck;

    @Size(max = 1000, message = "제한 단어 목록은 1000자 이내여야 합니다.")
    private String badText;

    @NotBlank(message = "제한단어차단 여부는 필수입니다.")
    @Pattern(regexp = "true|false", message = "제한단어차단 값은 true 또는 false만 허용됩니다.")
    private String badTextOption;

    private String naverApiKey;
    private String naverMapKey;
    private String googleMapKey;
    private String csApiUrl;
    private String csApiKey;
    private String digitomiDomain;
    private String digitomiApi;
    private String digitomiKey;
    private String digitomiReturnUrl;
}
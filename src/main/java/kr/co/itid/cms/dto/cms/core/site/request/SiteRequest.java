package kr.co.itid.cms.dto.cms.core.site.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class SiteRequest {

    private Integer idx;

    @NotNull(message = "사이트명은 필수입니다.")
    @Size(max = 60, message = "사이트명은 60자 이내여야 합니다.")
    private String siteName;

    @Size(max = 20, message = "호스트명은 20자 이내여야 합니다.")
    private String siteHostName;

    @NotNull(message = "도메인은 필수입니다.")
    @Size(max = 40, message = "도메인은 40자 이내여야 합니다.")
    private String siteDomain;

    @NotNull(message = "활성화 여부는 필수입니다.")
    private Boolean isOpen;

    @Size(max = 65535, message = "허용 IP는 너무 길 수 없습니다.")
    private String allowIp;

    @Size(max = 65535, message = "차단 IP는 너무 길 수 없습니다.")
    private String denyIp;
}
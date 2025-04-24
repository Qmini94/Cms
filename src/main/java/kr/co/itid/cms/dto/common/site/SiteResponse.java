package kr.co.itid.cms.dto.common.site;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SiteResponse {
    private Integer idx;
    private String siteName;
    private String siteDomain;
    private String sitePort;
    private String siteOption;
    private String language;
    private String siren24Id;
    private String siren24No;
    private String umsId;
    private String umsKey;
    private String privacyCheck;
    private String badText;
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

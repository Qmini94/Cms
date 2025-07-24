package kr.co.itid.cms.dto.cms.core.site.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SiteResponse {
    private Integer idx;
    private String siteName;
    private String siteHostName;
    private String siteDomain;
    private String isDeleted;
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

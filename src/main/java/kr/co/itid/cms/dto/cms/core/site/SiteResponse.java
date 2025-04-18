package kr.co.itid.cms.dto.cms.core.site;

import kr.co.itid.cms.entity.cms.core.Site;
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

    public static SiteResponse fromEntity(Site site) {
        return new SiteResponse(
                site.getIdx(), site.getSiteName(), site.getSiteDomain(), site.getSitePort(),
                site.getSiteOption(), site.getLanguage(),
                site.getSiren24Id(), site.getSiren24No(), site.getUmsId(),
                site.getUmsKey(), site.getPrivacyCheck(),
                site.getBadText(), site.getBadTextOption(),
                site.getNaverApiKey(), site.getNaverMapKey(), site.getGoogleMapKey(),
                site.getCsApiUrl(), site.getCsApiKey(), site.getDigitomiDomain(),
                site.getDigitomiApi(), site.getDigitomiKey(), site.getDigitomiReturnUrl()
        );
    }
}

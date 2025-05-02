package kr.co.itid.cms.mapper.common;

import kr.co.itid.cms.dto.common.site.SiteResponse;
import kr.co.itid.cms.entity.common.Site;

public class SiteMapper {
    public static SiteResponse toResponse(Site site) {
        return SiteResponse.builder()
                .idx(site.getIdx())
                .siteName(site.getSiteName())
                .siteDomain(site.getSiteDomain())
                .sitePort(site.getSitePort())
                .siteOption(site.getSiteOption())
                .language(site.getLanguage())
                .siren24Id(site.getSiren24Id())
                .siren24No(site.getSiren24No())
                .umsId(site.getUmsId())
                .umsKey(site.getUmsKey())
                .privacyCheck(site.getPrivacyCheck())
                .badText(site.getBadText())
                .badTextOption(site.getBadTextOption())
                .naverApiKey(site.getNaverApiKey())
                .naverMapKey(site.getNaverMapKey())
                .googleMapKey(site.getGoogleMapKey())
                .csApiUrl(site.getCsApiUrl())
                .csApiKey(site.getCsApiKey())
                .digitomiDomain(site.getDigitomiDomain())
                .digitomiApi(site.getDigitomiApi())
                .digitomiKey(site.getDigitomiKey())
                .digitomiReturnUrl(site.getDigitomiReturnUrl())
                .build();
    }
}
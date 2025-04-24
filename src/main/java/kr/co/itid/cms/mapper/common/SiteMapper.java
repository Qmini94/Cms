package kr.co.itid.cms.mapper.common;

import kr.co.itid.cms.dto.common.site.SiteResponse;
import kr.co.itid.cms.entity.common.Site;

public class SiteMapper {
    public static SiteResponse toResponse(Site site) {
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
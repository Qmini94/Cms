package kr.co.itid.cms.service.list;

import kr.co.itid.cms.dto.list.SiteResponse;
import kr.co.itid.cms.entity.cms.Site;
import kr.co.itid.cms.repository.list.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SiteService {
    private final SiteRepository siteRepository;

    public List<SiteResponse> getSiteAllData() {
        List<Site> sites = siteRepository.findAll();
        return sites.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private SiteResponse convertToResponse(Site site) {
        return new SiteResponse(
                site.getIdx(), site.getSiteName(), site.getSiteDomain(), site.getSitePort(),
                site.getSiteOption(), site.getLanguage(), site.getAdmin(),
                site.getSessionTimeout(), site.getSessionLayer(),
                site.getCompany(), site.getDdd(), site.getTel(),
                site.getSessionDupLogin(), site.getSslMode(),
                site.getSiren24Id(), site.getSiren24No(), site.getUmsId(),
                site.getUmsKey(), site.getTreeId(), site.getPrivacyCheck(),
                site.getBadText(), site.getBadTextOption(),
                site.getNaverApiKey(), site.getNaverMapKey(), site.getGoogleMapKey(),
                site.getCsApiUrl(), site.getCsApiKey(), site.getDigitomiDomain(),
                site.getDigitomiApi(), site.getDigitomiKey(), site.getDigitomiReturnUrl()
        );
    }
}

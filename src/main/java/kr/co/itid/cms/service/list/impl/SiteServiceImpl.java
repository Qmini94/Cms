package kr.co.itid.cms.service.list.impl;

import kr.co.itid.cms.dto.list.SiteResponse;
import kr.co.itid.cms.entity.cms.Site;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.repository.list.SiteRepository;
import kr.co.itid.cms.service.list.SiteService;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("siteService")
@RequiredArgsConstructor
public class SiteServiceImpl extends EgovAbstractServiceImpl implements SiteService {

    private final SiteRepository siteRepository;
    private final LoggingUtil loggingUtil;

    @Override
    public List<SiteResponse> getSiteAllData() throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get all site data");

        try {
            List<Site> sites = siteRepository.findAll();
            loggingUtil.logSuccess(Action.RETRIEVE, "Got all site data");
            return sites.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Error while getting site data");
            throw processException("Error while getting site data", e);
        }
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

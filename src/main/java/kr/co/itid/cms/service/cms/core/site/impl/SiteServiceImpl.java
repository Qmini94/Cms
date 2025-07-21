package kr.co.itid.cms.service.cms.core.site.impl;

import kr.co.itid.cms.dto.cms.core.site.SiteResponse;
import kr.co.itid.cms.entity.cms.core.site.Site;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.mapper.common.SiteMapper;
import kr.co.itid.cms.repository.common.SiteRepository;
import kr.co.itid.cms.service.cms.core.site.SiteService;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("siteService")
@RequiredArgsConstructor
public class SiteServiceImpl extends EgovAbstractServiceImpl implements SiteService {

    private final SiteRepository siteRepository;
    private final LoggingUtil loggingUtil;
    private final SiteMapper siteMapper;

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public List<SiteResponse> getSiteAllData() throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get all site data");

        try {
            List<Site> sites = siteRepository.findAll();
            loggingUtil.logSuccess(Action.RETRIEVE, "All site data loaded");
            return sites.stream()
                    .map(siteMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "Database error while loading site data");
            throw processException("Cannot access database", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unexpected error while loading site data");
            throw processException("Unexpected error", e);
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public String getSiteOptionByHostName(String siteHostName) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Get siteOption for host: " + siteHostName);

        try {
            return siteRepository.findBySiteHostName(siteHostName)
                    .map(Site::getSiteOption)
                    .orElse("open"); // 기본값은 open
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Failed to get siteOption for: " + siteHostName);
            throw processException("Failed to get siteOption", e);
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public List<String> getBadWordsByHostName(String siteHostName) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Get badWords for host: " + siteHostName);

        try {
            String badText = siteRepository.findBySiteHostName(siteHostName)
                    .map(Site::getBadText)
                    .orElse(null);

            if (badText == null || badText.trim().isEmpty()) {
                return List.of();
            }

            return List.of(badText.split(",")).stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Failed to get badWords for: " + siteHostName);
            throw processException("Failed to get badWords", e);
        }
    }

    @Override
    public boolean isClosedSite(String siteHostName) throws Exception {
        return "close".equalsIgnoreCase(getSiteOptionByHostName(siteHostName));
    }
}

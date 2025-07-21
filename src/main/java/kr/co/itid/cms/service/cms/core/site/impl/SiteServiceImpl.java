package kr.co.itid.cms.service.cms.core.site.impl;

import kr.co.itid.cms.dto.cms.core.site.SiteResponse;
import kr.co.itid.cms.entity.cms.core.menu.Menu;
import kr.co.itid.cms.entity.cms.core.site.Site;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.mapper.common.SiteMapper;
import kr.co.itid.cms.repository.common.SiteRepository;
import kr.co.itid.cms.service.cms.core.menu.MenuService;
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

    private final MenuService menuService;
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

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public SiteResponse updateSiteByHostName(String siteHostName, SiteResponse request) throws Exception {
        loggingUtil.logAttempt(Action.UPDATE, "Try to update site by hostName: " + siteHostName);

        try {
            Site site = siteRepository.findBySiteHostName(siteHostName)
                    .orElseThrow(() -> processException("해당 호스트명의 사이트가 존재하지 않습니다: " + siteHostName));

            String domain = request.getSiteDomain();
            String newHostName = domain != null && domain.contains(".")
                    ? domain.substring(0, domain.indexOf("."))
                    : domain;

            // 메뉴 이름 변경 위임
            try {
                menuService.updateDriveMenuName(siteHostName, newHostName, request.getSiteName());
            } catch (Exception menuEx) {
                loggingUtil.logFail(Action.UPDATE, "Menu update failed during site update: " + menuEx.getMessage());
                throw processException("사이트 수정 중 메뉴 이름 변경 실패", menuEx);
            }

            // 사이트 정보 수정
            site.setSiteName(request.getSiteName());
            site.setSiteDomain(domain);
            site.setSiteOption(request.getSiteOption());
            site.setBadTextOption(request.getBadTextOption());
            site.setBadText(request.getBadText());
            site.setSiteHostName(newHostName);

            Site saved = siteRepository.save(site);
            loggingUtil.logSuccess(Action.UPDATE, "Site updated successfully: " + newHostName);

            return siteMapper.toResponse(saved);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "Site update failed: " + e.getMessage());
            throw processException("사이트 수정 실패", e);
        }
    }
}

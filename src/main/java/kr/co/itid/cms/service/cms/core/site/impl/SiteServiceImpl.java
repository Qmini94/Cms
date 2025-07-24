package kr.co.itid.cms.service.cms.core.site.impl;

import kr.co.itid.cms.dto.cms.core.menu.request.MenuRequest;
import kr.co.itid.cms.dto.cms.core.site.request.SiteRequest;
import kr.co.itid.cms.dto.cms.core.site.response.SiteResponse;
import kr.co.itid.cms.entity.cms.core.menu.Menu;
import kr.co.itid.cms.entity.cms.core.site.Site;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.mapper.cms.core.site.SiteMapper;
import kr.co.itid.cms.repository.cms.core.site.SiteRepository;
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
    public List<SiteResponse> getSitesIsDeletedFalse() throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to retrieve active site data (isDeleted = false)");

        try {
            List<Site> sites = siteRepository.findByIsDeletedFalse();
            loggingUtil.logSuccess(Action.RETRIEVE, "Successfully loaded active site data: count=" + sites.size());
            return sites.stream()
                    .map(siteMapper::toResponse)
                    .collect(Collectors.toList());

        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "Database error while retrieving active site data");
            throw processException("Failed to retrieve site data from database", e);

        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unexpected error occurred while retrieving active site data");
            throw processException("Unexpected error while retrieving site data", e);
        }
    }

    @Override
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
    public Void saveSite(String siteHostName, SiteRequest request) throws Exception {
        boolean isNew = (siteHostName == null);
        String domain = request.getSiteDomain();
        String newHostName = domain != null && domain.contains(".")
                ? domain.substring(0, domain.indexOf("."))
                : domain;

        if (isNew) {
            loggingUtil.logAttempt(Action.CREATE, "Try to create site: " + newHostName);

            try {
                if (siteRepository.existsBySiteHostName(newHostName)) {
                    throw processException("이미 존재하는 호스트명입니다: " + newHostName);
                }

                Site entity = siteMapper.toEntity(request);
                entity.setSiteHostName(newHostName);
                siteRepository.save(entity);

                // 신규 드라이브 메뉴 생성
                try {
                    MenuRequest menuRequest = MenuRequest.builder()
                            .type("drive")
                            .level(1)
                            .name(newHostName)
                            .title(request.getSiteName())
                            .pathUrl(domain + "|")
                            .display(Menu.Display.show)
                            .position(0)
                            .build();

                    menuService.saveMenu(null, menuRequest);

                } catch (Exception menuEx) {
                    loggingUtil.logFail(Action.CREATE, "Menu insert failed during site creation: " + menuEx.getMessage());
                    throw processException("사이트 생성 중 메뉴 생성 실패", menuEx);
                }

                loggingUtil.logSuccess(Action.CREATE, "Site created successfully: " + newHostName);
                return null;

            } catch (Exception e) {
                loggingUtil.logFail(Action.CREATE, "Site creation failed: " + e.getMessage());
                throw processException("사이트 생성 실패", e);
            }

        } else {
            loggingUtil.logAttempt(Action.UPDATE, "Try to update site by hostName: " + siteHostName);

            try {
                Site site = siteRepository.findBySiteHostName(siteHostName)
                        .orElseThrow(() -> processException("해당 호스트명의 사이트가 존재하지 않습니다: " + siteHostName));

                if (!site.getSiteHostName().equals(newHostName)
                        && siteRepository.existsBySiteHostName(newHostName)) {
                    throw processException("이미 존재하는 호스트명입니다: " + newHostName);
                }

                // 메뉴 수정 처리
                try {
                    Menu existingMenu = menuService.getMenuByTypeAndName("drive", siteHostName)
                            .orElseThrow(() -> processException("기존 드라이브 메뉴를 찾을 수 없습니다: " + siteHostName));

                    MenuRequest menuRequest = MenuRequest.builder()
                            .id(existingMenu.getId())
                            .type("drive")
                            .level(existingMenu.getLevel())
                            .name(newHostName)
                            .title(request.getSiteName())
                            .display(existingMenu.getDisplay())
                            .position(existingMenu.getPosition())
                            .parentId(existingMenu.getParentId())
                            .pathUrl(domain + "|")
                            //TODO: 메뉴 옵션 처리 될때마다 사이트 수정시 누락안되도록 처리해야함.
                            .build();

                    menuService.saveMenu(1L, menuRequest);

                } catch (Exception menuEx) {
                    loggingUtil.logFail(Action.UPDATE, "Menu update failed during site update: " + menuEx.getMessage());
                    throw processException("사이트 수정 중 메뉴 수정 실패", menuEx);
                }

                // 사이트 정보 수정
                site.setSiteName(request.getSiteName());
                site.setSiteDomain(domain);
                site.setSiteOption(request.getSiteOption());
                site.setBadTextOption(request.getBadTextOption());
                site.setBadText(request.getBadText());
                site.setSiteHostName(newHostName);

                siteRepository.save(site);
                loggingUtil.logSuccess(Action.UPDATE, "Site updated successfully: " + newHostName);
                return null;

            } catch (Exception e) {
                loggingUtil.logFail(Action.UPDATE, "Site update failed: " + e.getMessage());
                throw processException("사이트 수정 실패", e);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void softDeleteSite(String siteHostName) throws Exception {
        loggingUtil.logAttempt(Action.DELETE, "Try to soft-delete site: " + siteHostName);

        try {
            Site site = siteRepository.findBySiteHostName(siteHostName)
                    .orElseThrow(() -> processException("존재하지 않는 사이트입니다: " + siteHostName));

            site.setIsDeleted(true);
            siteRepository.save(site);

            loggingUtil.logSuccess(Action.DELETE, "Soft-deleted site: " + siteHostName);
        } catch (Exception e) {
            loggingUtil.logFail(Action.DELETE, "Soft delete failed: " + e.getMessage());
            throw processException("사이트 삭제 실패", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void hardDeleteSite(String siteHostName) throws Exception {
        loggingUtil.logAttempt(Action.DELETE, "Try to hard-delete site: " + siteHostName);

        try {
            Site site = siteRepository.findBySiteHostName(siteHostName)
                    .orElseThrow(() -> processException("존재하지 않는 사이트입니다: " + siteHostName));

            siteRepository.delete(site);

            loggingUtil.logSuccess(Action.DELETE, "Hard-deleted site: " + siteHostName);
        } catch (Exception e) {
            loggingUtil.logFail(Action.DELETE, "Hard delete failed: " + e.getMessage());
            throw processException("사이트 완전 삭제 실패", e);
        }
    }
}

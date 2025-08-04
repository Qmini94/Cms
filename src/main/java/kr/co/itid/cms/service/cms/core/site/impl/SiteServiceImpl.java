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
import kr.co.itid.cms.util.IpUtil;
import kr.co.itid.cms.util.JsonFileWriterUtil;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("siteService")
@RequiredArgsConstructor
public class SiteServiceImpl extends EgovAbstractServiceImpl implements SiteService {

    private final MenuService menuService;
    private final SiteRepository siteRepository;
    private final SiteMapper siteMapper;
    private final LoggingUtil loggingUtil;
    private final JsonFileWriterUtil jsonFileWriterUtil;

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public boolean getSiteOpenStatusByHostName(String siteHostName) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Get isOpen for host: " + siteHostName);

        try {
            return siteRepository.findBySiteHostName(siteHostName)
                    .map(Site::getIsOpen)
                    .orElse(true); // 기본값은 활성 상태 (true)
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Failed to get isOpen for: " + siteHostName);
            throw processException("Failed to get site open status", e);
        }
    }

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
    public boolean isIpAllowed(String siteHostName, String clientIp) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "[IP 체크] site=" + siteHostName + ", clientIp=" + clientIp);

        try {
            Optional<Site> optionalSite = siteRepository.findBySiteHostName(siteHostName);

            if (!optionalSite.isPresent() || Boolean.TRUE.equals(optionalSite.get().getIsDeleted())) {
                loggingUtil.logSuccess(Action.RETRIEVE, "[IP 체크] 사이트 없음 또는 삭제됨 → 기본 허용");
                return true;
            }

            Site site = optionalSite.get();
            List<String> allowList = parseIpList(site.getAllowIp());
            List<String> denyList = parseIpList(site.getDenyIp());

            // 1. allow_ip: 우선 허용
            boolean isAllowed = allowList.stream()
                    .anyMatch(allowed -> allowed.equals(clientIp) || IpUtil.isInRange(clientIp, allowed));

            if (isAllowed) {
                loggingUtil.logSuccess(Action.RETRIEVE, "[IP 체크] allow_ip (직접 또는 CIDR) 포함 → 무조건 허용");
                return true;
            }

            // 2. deny_ip: 전체 차단 or 포함된 IP or 포함된 CIDR
            boolean isGlobalDeny = denyList.stream()
                    .anyMatch(ip -> ip.trim().startsWith("all"));

            boolean isDenied = denyList.stream()
                    .anyMatch(denied -> denied.equals(clientIp) || IpUtil.isInRange(clientIp, denied));

            if (isGlobalDeny || isDenied) {
                loggingUtil.logSuccess(Action.RETRIEVE, "[IP 체크] deny_ip (직접 또는 CIDR) 포함 또는 전체 차단 → 차단");
                return false;
            }

            // 기본 허용
            loggingUtil.logSuccess(Action.RETRIEVE, "[IP 체크] 정책 없음 → 기본 허용");
            return true;

        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "[IP 체크] DB 접근 오류");
            throw processException("Cannot access site data from database", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "[IP 체크] 예기치 않은 오류 발생");
            throw processException("Unexpected error while checking IP access", e);
        }
    }

    private List<String> parseIpList(String ipData) {
        if (ipData == null || ipData.trim().isEmpty()) return Collections.emptyList();

        return Arrays.stream(ipData.split("[,\\n]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.contains(":") ? s.substring(0, s.indexOf(":")).trim() : s) // ← IP만 추출
                .collect(Collectors.toList());
    }

    @Override
    public boolean isClosedSite(String siteHostName) throws Exception {
        return !getSiteOpenStatusByHostName(siteHostName);
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void saveSite(String siteHostName, SiteRequest request) throws Exception {
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
                    // 메뉴 생성
                    MenuRequest menuRequest = MenuRequest.builder()
                            .type("drive")
                            .level(1)
                            .name(newHostName)
                            .title(request.getSiteName())
                            .pathUrl(domain + "|")
                            .pathString(request.getSiteName())  // or 적절한 pathString 지정
                            .isShow(true)
                            .isUseSearch(false)
                            .isUseCount(false)
                            .position(0)
                            .build();

                    menuService.saveDriveMenu(null, menuRequest);

                } catch (Exception menuEx) {
                    loggingUtil.logFail(Action.CREATE, "Menu insert failed during site creation: " + menuEx.getMessage());
                    throw processException("사이트 생성 중 메뉴 생성 실패", menuEx);
                }

                loggingUtil.logSuccess(Action.CREATE, "Site created successfully: " + newHostName);
                jsonFileWriterUtil.writeJsonFile(
                        "site",
                        "site_list",
                        siteRepository.findAll(),
                        false
                );
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
                            .level(existingMenu.getLevel().intValue())
                            .name(newHostName)
                            .title(request.getSiteName())
                            .isShow(existingMenu.getIsShow())
                            .isUseSearch(existingMenu.getIsUseSearch())
                            .isUseCount(existingMenu.getIsUseCount())
                            .position(existingMenu.getPosition())
                            .parentId(existingMenu.getParentId())
                            .pathUrl(domain + "|")
                            .pathString(existingMenu.getPathString())
                            .build();

                    menuService.saveDriveMenu(existingMenu.getId(), menuRequest);

                } catch (Exception menuEx) {
                    loggingUtil.logFail(Action.UPDATE, "Menu update failed during site update: " + menuEx.getMessage());
                    throw processException("사이트 수정 중 메뉴 수정 실패", menuEx);
                }

                // 사이트 정보 수정
                site.setSiteName(request.getSiteName());
                site.setSiteDomain(domain);
                site.setIsOpen(request.getIsOpen());
                site.setSiteHostName(newHostName);
                site.setAllowIp(request.getAllowIp());
                site.setDenyIp(request.getDenyIp());

                siteRepository.save(site);
                jsonFileWriterUtil.writeJsonFile(
                        "site",
                        "site_list",
                        siteRepository.findAll(),
                        false
                );
                loggingUtil.logSuccess(Action.UPDATE, "Site updated successfully: " + newHostName);
            } catch (RuntimeException e) {
                if (e.getMessage() != null && e.getMessage().contains("JSON")) {
                    loggingUtil.logFail(Action.UPDATE, "JSON 파일 저장 실패 during saveSite for drive: " + siteHostName);
                    throw processException("사이트 JSON 파일 저장 실패" + e.getMessage(), e);
                } else {
                    loggingUtil.logFail(Action.UPDATE, "Runtime error during saveSite for drive: " + siteHostName);
                    throw processException("Unexpected runtime error", e);
                }
            } catch (Exception e) {
                loggingUtil.logFail(Action.UPDATE, "Site update failed: " + e.getMessage());
                throw processException("사이트 수정 실패", e);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void restoreSite(String siteHostName) throws Exception {
        loggingUtil.logAttempt(Action.UPDATE, "Try to restore site: " + siteHostName);

        try {
            Site site = siteRepository.findBySiteHostName(siteHostName)
                    .orElseThrow(() -> processException("존재하지 않는 사이트입니다: " + siteHostName));

            site.setIsDeleted(false);
            siteRepository.save(site);
            jsonFileWriterUtil.writeJsonFile(
                    "site",
                    "site_list",
                    siteRepository.findAll(),
                    false
            );

            loggingUtil.logSuccess(Action.UPDATE, "Restored site: " + siteHostName);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("JSON")) {
                loggingUtil.logFail(Action.UPDATE, "JSON 파일 저장 실패 during restoreSite for drive: " + siteHostName);
                throw processException("사이트 JSON 파일 저장 실패" + e.getMessage(), e);
            } else {
                loggingUtil.logFail(Action.UPDATE, "Runtime error during restoreSite for drive: " + siteHostName);
                throw processException("Unexpected runtime error", e);
            }
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "Restore failed: " + e.getMessage());
            throw processException("사이트 복구 실패", e);
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
            jsonFileWriterUtil.writeJsonFile(
                    "site",
                    "site_list",
                    siteRepository.findAll(),
                    false
            );

            loggingUtil.logSuccess(Action.DELETE, "Soft-deleted site: " + siteHostName);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("JSON")) {
                loggingUtil.logFail(Action.UPDATE, "JSON 파일 저장 실패 during softDeleteSite for drive: " + siteHostName);
                throw processException("사이트 JSON 파일 저장 실패" + e.getMessage(), e);
            } else {
                loggingUtil.logFail(Action.UPDATE, "Runtime error during softDeleteSite for drive: " + siteHostName);
                throw processException("Unexpected runtime error", e);
            }
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
            menuService.deleteDriveAndAllChildren(siteHostName);
            jsonFileWriterUtil.writeJsonFile(
                    "site",
                    "site_list",
                    siteRepository.findAll(),
                    false
            );

            loggingUtil.logSuccess(Action.DELETE, "Hard-deleted site: " + siteHostName);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("JSON")) {
                loggingUtil.logFail(Action.UPDATE, "JSON 파일 저장 실패 during hardDeleteSite for drive: " + siteHostName);
                throw processException("사이트 JSON 파일 저장 실패" + e.getMessage(), e);
            } else {
                loggingUtil.logFail(Action.UPDATE, "Runtime error during hardDeleteSite for drive: " + siteHostName);
                throw processException("Unexpected runtime error", e);
            }
        } catch (Exception e) {
            loggingUtil.logFail(Action.DELETE, "Hard delete failed: " + e.getMessage());
            throw processException("사이트 완전 삭제 실패", e);
        }
    }
}

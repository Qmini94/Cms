package kr.co.itid.cms.service.cms.core.site.impl;

import kr.co.itid.cms.config.security.port.SiteAccessChecker;
import kr.co.itid.cms.entity.cms.core.site.Site;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.repository.cms.core.site.SiteRepository;
import kr.co.itid.cms.util.IpUtil;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("siteAccessChecker")
@RequiredArgsConstructor
public class SiteAccessCheckerImpl extends EgovAbstractServiceImpl implements SiteAccessChecker {

    private final SiteRepository siteRepository;
    private final LoggingUtil loggingUtil;

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
        try {
            Boolean isOpen = siteRepository.findBySiteHostName(siteHostName)
                    .map(Site::getIsOpen)
                    .orElse(true);
            return !isOpen;
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Failed to get isOpen for: " + siteHostName);
            throw processException("Failed to get site open status", e);
        }
    }
}

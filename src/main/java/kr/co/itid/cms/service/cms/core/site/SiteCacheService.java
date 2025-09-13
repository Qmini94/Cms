package kr.co.itid.cms.service.cms.core.site;

import kr.co.itid.cms.dto.cms.core.site.response.SiteResponse;
import kr.co.itid.cms.entity.cms.core.site.Site;
import kr.co.itid.cms.mapper.cms.core.site.SiteMapper;
import kr.co.itid.cms.repository.cms.core.site.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사이트 캐시 관리 서비스
 */
@Service
@RequiredArgsConstructor
public class SiteCacheService {

    private final SiteRepository siteRepository;
    private final SiteMapper siteMapper;

    /**
     * 활성 사이트 목록 조회 (캐시 적용)
     */
    @Cacheable(value = "sites", key = "'active'")
    public List<SiteResponse> getActiveSites() {
        List<Site> sites = siteRepository.findByIsDeletedFalse();
        return sites.stream()
                .map(siteMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 전체 사이트 목록 조회 (캐시 적용)
     */
    @Cacheable(value = "sites", key = "'all'")
    public List<SiteResponse> getAllSites() {
        List<Site> sites = siteRepository.findAll();
        return sites.stream()
                .map(siteMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 사이트 캐시 무효화
     */
    @CacheEvict(value = "sites", allEntries = true)
    public void evictSiteCache() {
        // 캐시 무효화만 수행
    }

    /**
     * 특정 사이트 조회 (캐시 적용)
     */
    @Cacheable(value = "site", key = "#siteHostName")
    public SiteResponse getSiteByHostName(String siteHostName) {
        return siteRepository.findBySiteHostName(siteHostName)
                .map(siteMapper::toResponse)
                .orElse(null);
    }

    /**
     * 특정 사이트 캐시 무효화
     */
    @CacheEvict(value = "site", key = "#siteHostName")
    public void evictSiteCache(String siteHostName) {
        // 특정 사이트 캐시 무효화
    }
}

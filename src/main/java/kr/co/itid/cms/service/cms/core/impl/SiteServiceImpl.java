package kr.co.itid.cms.service.cms.core.impl;

import kr.co.itid.cms.dto.cms.core.site.SiteResponse;
import kr.co.itid.cms.entity.cms.core.Site;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.mapper.core.SiteMapper;
import kr.co.itid.cms.repository.cms.core.SiteRepository;
import kr.co.itid.cms.service.cms.core.SiteService;
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
            loggingUtil.logSuccess(Action.RETRIEVE, "All site data loaded");
            return sites.stream()
                    .map(SiteMapper::toResponse)
                    .collect(Collectors.toList());
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "Database error while loading site data");
            throw processException("Cannot access database", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unexpected error while loading site data");
            throw processException("Unexpected error", e);
        }
    }
}
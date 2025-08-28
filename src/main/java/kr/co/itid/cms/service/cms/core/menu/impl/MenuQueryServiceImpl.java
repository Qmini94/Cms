package kr.co.itid.cms.service.cms.core.menu.impl;

import kr.co.itid.cms.entity.cms.core.menu.Menu;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.repository.cms.core.menu.MenuRepository;
import kr.co.itid.cms.service.cms.core.menu.MenuQueryService;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("menuQueryService")
@RequiredArgsConstructor
public class MenuQueryServiceImpl extends EgovAbstractServiceImpl implements MenuQueryService {

    private final MenuRepository menuRepository;
    private final LoggingUtil loggingUtil;

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public String getPathIdById(Long menuId) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "try get pathId, menuId=" + menuId);
        try {
            String pathId = menuRepository.findById(menuId)
                    .map(Menu::getPathId)
                    .orElseThrow(() -> processException("menu not found: " + menuId));
            loggingUtil.logSuccess(Action.RETRIEVE, "got pathId");
            return pathId;
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "db error (pathId), menuId=" + menuId);
            throw processException("db error", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "error (pathId), menuId=" + menuId);
            throw processException("error while get pathId", e);
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public List<Long> getDescendantIdsByPathPrefix(String pathPrefix) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "try get descendants, prefix=" + preview(pathPrefix));
        try {
            List<Long> descendants = menuRepository.findDescendantIdsByPathPrefix(pathPrefix);
            loggingUtil.logSuccess(Action.RETRIEVE, "got descendants, count=" + (descendants == null ? 0 : descendants.size()));
            return descendants;
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "db error (descendants), prefix=" + preview(pathPrefix));
            throw processException("db error", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "error (descendants), prefix=" + preview(pathPrefix));
            throw processException("error while get descendants", e);
        }
    }

    private String preview(String s) {
        if (s == null) return "null";
        return s.length() <= 24 ? s : s.substring(0, 24) + "…";
    }
}
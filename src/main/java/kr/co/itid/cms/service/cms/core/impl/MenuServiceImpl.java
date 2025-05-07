package kr.co.itid.cms.service.cms.core.impl;

import kr.co.itid.cms.dto.cms.core.menu.MenuResponse;
import kr.co.itid.cms.dto.cms.core.menu.MenuTreeLiteResponse;
import kr.co.itid.cms.dto.cms.core.menu.MenuTreeResponse;
import kr.co.itid.cms.entity.cms.core.Menu;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.mapper.cms.core.menu.MenuMapper;
import kr.co.itid.cms.repository.cms.core.MenuRepository;
import kr.co.itid.cms.service.cms.core.MenuService;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service("menuService")
@RequiredArgsConstructor
public class MenuServiceImpl extends EgovAbstractServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final LoggingUtil loggingUtil;
    private final MenuMapper menuMapper;

    @Override
    public List<MenuResponse> getRootMenus() throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get all drives");

        try {
            List<Menu> menus = menuRepository.findByParentIdIsNull();
            loggingUtil.logSuccess(Action.RETRIEVE, "Got all drives");

            return menus.stream()
                    .map(menuMapper::toResponse)
                    .toList();
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "Database error while getting drives");
            throw processException("Cannot access database", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error while getting drives");
            throw processException("Unexpected error", e);
        }
    }

    @Override
    public List<MenuTreeLiteResponse> getMenuTreeLiteByName(String name) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get menu tree (lite) for: " + name);

        try {
            Menu rootMenu = menuRepository.findByNameOrderByPositionAsc(name)
                    .orElseThrow(() -> processException("Drive not found: " + name));

            loggingUtil.logSuccess(Action.RETRIEVE, "Got lite tree for: " + name);
            return buildMenuTreeLite(rootMenu.getId());
        } catch (NoSuchElementException e) {
            throw e;
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "Database error while getting tree: " + name);
            throw processException("Cannot access database", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error while getting tree: " + name);
            throw processException("Unexpected error", e);
        }
    }

    @Override
    public List<MenuTreeResponse> getMenuTreeByName(String name) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get menu tree for: " + name);

        try {
            Menu rootMenu = menuRepository.findByNameOrderByPositionAsc(name)
                    .orElseThrow(() -> processException("Drive not found: " + name));

            loggingUtil.logSuccess(Action.RETRIEVE, "Got full tree for: " + name);
            return buildMenuTreeResponse(rootMenu.getId());
        } catch (NoSuchElementException e) {
            throw e;
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "Database error while getting tree: " + name);
            throw processException("Cannot access database", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error while getting tree: " + name);
            throw processException("Unexpected error", e);
        }
    }

    private List<MenuTreeLiteResponse> buildMenuTreeLite(Long parentId) {
        return getChildren(parentId).stream()
                .map(menu -> menuMapper.toLiteTreeResponse(menu, buildMenuTreeLite(menu.getId())))
                .toList();
    }

    private List<MenuTreeResponse> buildMenuTreeResponse(Long parentId) {
        return getChildren(parentId).stream()
                .map(menu -> menuMapper.toFullResponse(menu, buildMenuTreeResponse(menu.getId())))
                .toList();
    }

    private List<Menu> getChildren(Long parentId) {
        return menuRepository.findByParentIdOrderByPositionAsc(parentId);
    }
}
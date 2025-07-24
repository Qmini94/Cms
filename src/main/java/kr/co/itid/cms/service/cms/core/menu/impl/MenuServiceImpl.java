package kr.co.itid.cms.service.cms.core.menu.impl;

import kr.co.itid.cms.dto.cms.core.menu.request.MenuRequest;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuResponse;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuTreeLiteResponse;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuTreeResponse;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuTypeValueResponse;
import kr.co.itid.cms.entity.cms.core.menu.Menu;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.mapper.cms.core.menu.MenuMapper;
import kr.co.itid.cms.repository.cms.core.menu.MenuRepository;
import kr.co.itid.cms.service.cms.core.menu.MenuService;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service("menuService")
@RequiredArgsConstructor
public class MenuServiceImpl extends EgovAbstractServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final LoggingUtil loggingUtil;
    private final MenuMapper menuMapper;

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public MenuResponse getMenuById(Long id) throws Exception {
        Menu menu = getMenuEntityById(id);
        return menuMapper.toResponse(menu);
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public MenuTypeValueResponse getMenuRenderById(Long id) throws Exception {
        Menu menu = getMenuEntityById(id);
        return menuMapper.toTypeValueResponse(menu);
    }

    private Menu getMenuEntityById(Long id) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get menu by id: " + id);

        try {
            Menu menu = menuRepository.findById(id)
                    .orElseThrow(() -> processException("Menu not found: " + id));

            loggingUtil.logSuccess(Action.RETRIEVE, "Got menu by id: " + id);
            return menu;
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "Database error while getting menu");
            throw processException("Cannot access database", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unexpected error while getting menu");
            throw processException("Unexpected error", e);
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public Optional<Menu> getMenuByTypeAndName(String type, String name) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get menu by type and name: type=" + type + ", name=" + name);

        try {
            Optional<Menu> result = menuRepository.findByTypeAndName(type, name);

            if (result.isPresent()) {
                loggingUtil.logSuccess(Action.RETRIEVE, "Got menu by type and name: " + name);
            } else {
                loggingUtil.logSuccess(Action.RETRIEVE, "Menu not found for type and name: " + name);
            }

            return result;

        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "Database error while getting menu by type and name");
            throw processException("Cannot access database", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unexpected error while getting menu by type and name");
            throw processException("Unexpected error", e);
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
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
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
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
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
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

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void saveMenu(Long id, MenuRequest request) throws Exception {
        boolean isNew = (id == null);
        Action action = isNew ? Action.CREATE : Action.UPDATE;

        loggingUtil.logAttempt(action, "Try to " + (isNew ? "create" : "update") + " menu: " + request.getName());

        try {
            if (isNew) {
                // 중복 체크
                if (menuRepository.findByTypeAndName(request.getType(), request.getName()).isPresent()) {
                    throw processException("이미 동일한 이름의 메뉴가 존재합니다: " + request.getName());
                }

                Menu menu = menuMapper.toEntity(request);

                menuRepository.save(menu);
                loggingUtil.logSuccess(action, "Menu created: " + request.getName());

            } else {
                Menu menu = menuRepository.findById(Long.valueOf(id))
                        .orElseThrow(() -> processException("해당 ID의 메뉴가 존재하지 않습니다: " + id));

                // 수정 가능한 필드만 반영
                menu.setName(request.getName());
                menu.setTitle(request.getTitle());
                menu.setDisplay(Menu.Display.valueOf(String.valueOf(request.getDisplay())));
                menu.setValue(request.getValue());
                menu.setPathUrl(request.getPathUrl());
                menu.setPathId(request.getPathId());
                menu.setPosition(request.getPosition());
                menu.setLevel(request.getLevel());

                menuRepository.save(menu);
                loggingUtil.logSuccess(action, "Menu updated: " + request.getName());
            }

        } catch (Exception e) {
            loggingUtil.logFail(action, "Failed to " + (isNew ? "create" : "update") + " menu: " + e.getMessage());
            throw processException("메뉴 " + (isNew ? "등록" : "수정") + " 실패", e);
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
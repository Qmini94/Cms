package kr.co.itid.cms.service.cms.base.impl;

import kr.co.itid.cms.dto.cms.MenuResponse;
import kr.co.itid.cms.entity.cms.Menu;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.repository.cms.MenuRepository;
import kr.co.itid.cms.service.cms.base.MenuService;
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

    @Override
    public List<MenuResponse> getAllDrives() throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get all drives");

        try {
            List<Menu> menus = menuRepository.findByParentIdIsNull();
            loggingUtil.logSuccess(Action.RETRIEVE, "Got all drives");

            return menus.stream()
                    .map(menu -> new MenuResponse(
                            menu.getId(),
                            menu.getParentId(),
                            menu.getTitle(),
                            menu.getType(),
                            menu.getValue(),
                            menu.getDisplay() != null ? menu.getDisplay().name() : null,
                            menu.getPathUrl(),
                            menu.getPathId()
                    ))
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
    public List<MenuResponse> getAllChildrenByName(String name) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get children for: " + name);

        try {
            Menu rootMenu = menuRepository.findByNameOrderByPositionAsc(name)
                    .orElseThrow(() -> {
                        loggingUtil.logFail(Action.RETRIEVE, "Drive not found: " + name);
                        return processException("Drive not found", new NoSuchElementException("Drive not found"));
                    });

            loggingUtil.logSuccess(Action.RETRIEVE, "Got children for: " + name);
            return buildMenuTree(rootMenu.getId());
        } catch (NoSuchElementException e) {
            throw e;
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "Database error while getting children: " + name);
            throw processException("Cannot access database", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error while getting children: " + name);
            throw processException("Unexpected error", e);
        }
    }

    private List<MenuResponse> buildMenuTree(Long parentId) {
        List<Menu> children = menuRepository.findByParentIdOrderByPositionAsc(parentId);

        return children.stream()
                .map(menu -> {
                    MenuResponse response = new MenuResponse(
                            menu.getId(),
                            menu.getParentId(),
                            menu.getTitle(),
                            menu.getType(),
                            menu.getValue(),
                            menu.getDisplay() != null ? menu.getDisplay().name() : null,
                            menu.getPathUrl(),
                            menu.getPathId()
                    );
                    List<MenuResponse> subChildren = buildMenuTree(menu.getId());
                    response.setChildren(subChildren);
                    return response;
                })
                .toList();
    }
}
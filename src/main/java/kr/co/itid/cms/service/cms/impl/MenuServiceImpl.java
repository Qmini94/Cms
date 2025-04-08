package kr.co.itid.cms.service.cms.impl;

import kr.co.itid.cms.dto.cms.MenuResponse;
import kr.co.itid.cms.entity.cms.Menu;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.repository.cms.MenuRepository;
import kr.co.itid.cms.service.cms.MenuService;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
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
                            menu.getPathUrl()
                    ))
                    .toList();
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Error while getting all drives");
            throw processException("Error while getting all drives", e);
        }
    }

    @Override
    public List<MenuResponse> getAllChildrenByName(String name) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get children by name: " + name);

        try {
            Menu rootMenu = menuRepository.findByNameOrderByLeftAsc(name)
                    .orElseThrow(() -> {
                        loggingUtil.logFail(Action.RETRIEVE, "Drive not found: " + name);
                        return processException("Drive not found", new NoSuchElementException("Drive not found"));
                    });

            loggingUtil.logSuccess(Action.RETRIEVE, "Got children for name: " + name);
            return buildMenuTree(rootMenu.getId());
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Error while getting children: " + name);
            throw processException("Error while getting children", e);
        }
    }

    private List<MenuResponse> buildMenuTree(Long parentId) {
        List<Menu> children = menuRepository.findByParentIdOrderByLevelAscLeftAsc(parentId);

        return children.stream()
                .map(menu -> {
                    MenuResponse response = new MenuResponse(
                            menu.getId(),
                            menu.getParentId(),
                            menu.getTitle(),
                            menu.getType(),
                            menu.getValue(),
                            menu.getDisplay() != null ? menu.getDisplay().name() : null,
                            menu.getPathUrl()
                    );
                    List<MenuResponse> subChildren = buildMenuTree(menu.getId());
                    response.setChildren(subChildren);
                    return response;
                })
                .toList();
    }
}
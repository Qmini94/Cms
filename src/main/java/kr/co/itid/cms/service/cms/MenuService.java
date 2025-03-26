package kr.co.itid.cms.service.cms;

import kr.co.itid.cms.dto.cms.MenuResponse;
import kr.co.itid.cms.entity.cms.Menu;
import kr.co.itid.cms.repository.cms.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    public List<MenuResponse> getAllDrives() {
        List<Menu> menus = menuRepository.findByType("drive");

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
    }

    public List<MenuResponse> getAllMenusByDrive(String driveTitle) {
        // 1. 드라이브 루트 메뉴 조회
        Menu rootMenu = menuRepository.findByTitleAndType(driveTitle, "drive")
                .orElseThrow(() -> new RuntimeException("Drive not found: " + driveTitle));

        // 2. 하위 메뉴 트리 구성
        return buildMenuTree(rootMenu.getId());
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
                    // 재귀 호출로 하위 children 구성
                    List<MenuResponse> subChildren = buildMenuTree(menu.getId());
                    response.setChildren(subChildren);
                    return response;
                })
                .toList();
    }
}

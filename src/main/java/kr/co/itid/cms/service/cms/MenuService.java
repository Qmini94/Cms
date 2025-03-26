package kr.co.itid.cms.service.cms;

import kr.co.itid.cms.dto.cms.MenuResponse;
import kr.co.itid.cms.entity.cms.Menu;
import kr.co.itid.cms.repository.cms.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    public List<MenuResponse> getAllMenus() {
        List<Menu> menus = menuRepository.findAllByOrderByLevelAscLeftAsc();

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
                .collect(Collectors.toList());
    }

    public String getMenuTitleById(Long id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 메뉴가 존재하지 않습니다."));
        return menu.getTitle();
    }

    public List<MenuResponse> getChildrenByParentId(Long parentId) {
        List<Menu> children = menuRepository.findByParentIdOrderByLevelAscLeftAsc(parentId);

        return children.stream()
                .map(menu -> new MenuResponse(
                        menu.getId(),
                        menu.getParentId(),
                        menu.getTitle(),
                        menu.getType(),
                        menu.getValue(),
                        menu.getDisplay() != null ? menu.getDisplay().name() : null,
                        menu.getPathUrl()
                ))
                .collect(Collectors.toList());
    }
}

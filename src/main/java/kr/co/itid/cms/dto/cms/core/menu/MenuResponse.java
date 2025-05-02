package kr.co.itid.cms.dto.cms.core.menu;

import kr.co.itid.cms.entity.cms.core.Menu;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MenuResponse {
    private final Long id;
    private final Long parentId;
    private final String title;
    private final String type;
    private final String value;
    private final String display;
    private final String pathUrl;
    private final String pathId;

    public static MenuResponse from(Menu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getParentId(),
                menu.getTitle(),
                menu.getType(),
                menu.getValue(),
                menu.getDisplay() != null ? menu.getDisplay().name() : null,
                menu.getPathUrl(),
                menu.getPathId()
        );
    }
}
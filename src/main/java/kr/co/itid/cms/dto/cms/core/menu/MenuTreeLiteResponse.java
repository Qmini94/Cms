package kr.co.itid.cms.dto.cms.core.menu;

import kr.co.itid.cms.entity.cms.core.Menu.Display;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class MenuTreeLiteResponse {
    private Long id;
    private Long parentId;
    private String title;
    private String type;
    private String value;
    private Display display;
    private String pathUrl;
    private String pathId;
    @Builder.Default
    private List<MenuTreeLiteResponse> children = new ArrayList<>();
}

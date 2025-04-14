package kr.co.itid.cms.dto.cms.base;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class MenuResponse {
    private Long id;
    private Long parentId;
    private String title;
    private String type;
    private String value;
    private String display;
    private String pathUrl;
    private String pathId;
    @Setter
    private List<MenuResponse> children;

    public MenuResponse(Long id, Long parentId, String title, String type, String value,
                        String display, String pathUrl, String pathId) {
        this.id = id;
        this.parentId = parentId;
        this.title = title;
        this.type = type;
        this.value = value;
        this.display = display;
        this.pathUrl = pathUrl;
        this.pathId = pathId;
        this.children = new ArrayList<>();
    }
}


package kr.co.itid.cms.dto.cms.core.menu.response;

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
    private String name;
    private String type;
    private String value;

    private Boolean isShow;

    private String pathUrl;
    private String pathId;

    @Builder.Default
    private List<MenuTreeLiteResponse> children = new ArrayList<>();
}
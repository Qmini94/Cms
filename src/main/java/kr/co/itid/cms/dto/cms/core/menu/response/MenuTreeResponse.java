package kr.co.itid.cms.dto.cms.core.menu.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class MenuTreeResponse {
    private Long id;
    private Long parentId;
    private Integer position;
    private Integer level;

    private String title;
    private String name;
    private String type;
    private String value;

    private Boolean isShow;

    private String pathUrl;
    private String pathString;
    private String pathId;

    private Boolean isUseSearch;
    private Boolean isUseCount;

    @Builder.Default
    private List<MenuTreeResponse> children = new ArrayList<>();
}
package kr.co.itid.cms.dto.cms.core.menu.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenuResponse {
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
}
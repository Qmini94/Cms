package kr.co.itid.cms.dto.cms.core.menu;

import lombok.*;

@Getter
@Builder
public class MenuResponse {
    private Long id;
    private Long parentId;
    private String title;
    private String type;
    private String value;
    private String display;
    private String pathUrl;
    private String pathId;
}
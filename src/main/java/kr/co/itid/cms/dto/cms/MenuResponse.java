package kr.co.itid.cms.dto.cms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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
    private String url;
}

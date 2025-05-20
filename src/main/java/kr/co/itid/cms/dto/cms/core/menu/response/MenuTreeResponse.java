package kr.co.itid.cms.dto.cms.core.menu.response;

import kr.co.itid.cms.entity.cms.core.menu.Menu.Display;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
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
    private Display display;
    private Boolean optSns;
    private Boolean optShortUrl;
    private Boolean optQrcode;
    private Boolean optMobile;
    private String pathUrl;
    private String pathId;
    private String navi;
    private Integer serialNo;
    private String module;
    private String boardId;
    private String searchOpt;
    private String pageManager;
    @Builder.Default
    private List<MenuTreeResponse> children = new ArrayList<>();
}

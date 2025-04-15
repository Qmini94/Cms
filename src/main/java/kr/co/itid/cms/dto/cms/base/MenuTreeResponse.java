package kr.co.itid.cms.dto.cms.base;

import kr.co.itid.cms.entity.cms.base.Menu;
import kr.co.itid.cms.entity.cms.base.Menu.Display;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuTreeResponse {
    private Long id;
    private Long parentId;
    private Long position;
    private Long level;
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
    private List<MenuTreeResponse> children = new ArrayList<>();

    public static MenuTreeResponse fromEntity(Menu menu) {
        MenuTreeResponse dto = new MenuTreeResponse();
        dto.setId(menu.getId());
        dto.setParentId(menu.getParentId());
        dto.setPosition(menu.getPosition());
        dto.setLevel(menu.getLevel());
        dto.setTitle(menu.getTitle());
        dto.setName(menu.getName());
        dto.setType(menu.getType());
        dto.setValue(menu.getValue());
        dto.setDisplay(menu.getDisplay());
        dto.setOptSns(menu.getOptSns());
        dto.setOptShortUrl(menu.getOptShortUrl());
        dto.setOptQrcode(menu.getOptQrcode());
        dto.setOptMobile(menu.getOptMobile());
        dto.setPathUrl(menu.getPathUrl());
        dto.setPathId(menu.getPathId());
        dto.setNavi(menu.getNavi());
        dto.setSerialNo(menu.getSerialNo());
        dto.setModule(menu.getModule());
        dto.setBoardId(menu.getBoardId());
        dto.setSearchOpt(menu.getSearchOpt());
        dto.setPageManager(menu.getPageManager());
        return dto;
    }
}
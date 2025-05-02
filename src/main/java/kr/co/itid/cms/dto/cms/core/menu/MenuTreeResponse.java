package kr.co.itid.cms.dto.cms.core.menu;

import kr.co.itid.cms.entity.cms.core.Menu.Display;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @Builder.Default
    private List<MenuTreeResponse> children = new ArrayList<>();

    public static MenuTreeResponse ofLite(kr.co.itid.cms.entity.cms.core.Menu menu, List<MenuTreeResponse> children) {
        return MenuTreeResponse.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .title(menu.getTitle())
                .type(menu.getType())
                .value(menu.getValue())
                .display(menu.getDisplay())
                .pathUrl(menu.getPathUrl())
                .pathId(menu.getPathId())
                .children(children)
                .build();
    }

    public static MenuTreeResponse ofFull(kr.co.itid.cms.entity.cms.core.Menu menu, List<MenuTreeResponse> children) {
        return MenuTreeResponse.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .position(menu.getPosition())
                .level(menu.getLevel())
                .title(menu.getTitle())
                .name(menu.getName())
                .type(menu.getType())
                .value(menu.getValue())
                .display(menu.getDisplay())
                .optSns(menu.getOptSns())
                .optShortUrl(menu.getOptShortUrl())
                .optQrcode(menu.getOptQrcode())
                .optMobile(menu.getOptMobile())
                .pathUrl(menu.getPathUrl())
                .pathId(menu.getPathId())
                .navi(menu.getNavi())
                .serialNo(menu.getSerialNo())
                .module(menu.getModule())
                .boardId(menu.getBoardId())
                .searchOpt(menu.getSearchOpt())
                .pageManager(menu.getPageManager())
                .children(children)
                .build();
    }
}

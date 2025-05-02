package kr.co.itid.cms.mapper.cms.core.menu;

import kr.co.itid.cms.dto.cms.core.menu.MenuResponse;
import kr.co.itid.cms.dto.cms.core.menu.MenuTreeResponse;
import kr.co.itid.cms.entity.cms.core.Menu;

import java.util.List;

public class MenuMapper {
    public static MenuResponse toResponse(Menu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getParentId(),
                menu.getTitle(),
                menu.getType(),
                menu.getValue(),
                menu.getDisplay() != null ? menu.getDisplay().name() : null,
                menu.getPathUrl(),
                menu.getPathId()
        );
    }

    public static MenuTreeResponse toLiteResponse(Menu menu, List<MenuTreeResponse> children) {
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

    public static MenuTreeResponse toFullResponse(Menu menu, List<MenuTreeResponse> children) {
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


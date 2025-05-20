package kr.co.itid.cms.mapper.cms.core.menu;

import kr.co.itid.cms.dto.cms.core.menu.response.MenuResponse;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuTreeLiteResponse;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuTreeResponse;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuTypeValueResponse;
import kr.co.itid.cms.entity.cms.core.menu.Menu;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MenuMapper {

    @Mapping(target = "display", expression = "java(menu.getDisplay() != null ? menu.getDisplay().name() : null)")
    MenuResponse toResponse(Menu menu);

    @Mapping(target = "name", source = "name")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "value", source = "value")
    MenuTypeValueResponse toTypeValueResponse(Menu menu);

    @Named("toLiteTreeResponse")
    default MenuTreeLiteResponse toLiteTreeResponse(Menu menu, List<MenuTreeLiteResponse> children) {
        return MenuTreeLiteResponse.builder()
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

    @Named("toFullResponse")
    default MenuTreeResponse toFullResponse(Menu menu, List<MenuTreeResponse> children) {
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
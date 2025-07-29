package kr.co.itid.cms.mapper.cms.core.menu;

import kr.co.itid.cms.dto.cms.core.menu.request.MenuRequest;
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

    Menu toEntity(MenuRequest menuRequest);

    @Mapping(target = "isShow", source = "isShow")
    MenuResponse toResponse(Menu menu);

    MenuTypeValueResponse toTypeValueResponse(Menu menu);

    @Named("toLiteTreeResponse")
    default MenuTreeLiteResponse toLiteTreeResponse(Menu menu, List<MenuTreeLiteResponse> children) {
        return MenuTreeLiteResponse.builder()
                .id(menu.getId())
                .parentId(menu.getParentId())
                .title(menu.getTitle())
                .name(menu.getName())
                .type(menu.getType())
                .value(menu.getValue())
                .isShow(menu.getIsShow())
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
                .position(menu.getPosition().intValue())
                .level(menu.getLevel().intValue())
                .title(menu.getTitle())
                .name(menu.getName())
                .type(menu.getType())
                .value(menu.getValue())
                .isShow(menu.getIsShow())
                .pathUrl(menu.getPathUrl())
                .pathString(menu.getPathString())
                .pathId(menu.getPathId())
                .isUseSearch(menu.getIsUseSearch())
                .isUseCount(menu.getIsUseCount())
                .children(children)
                .build();
    }
}
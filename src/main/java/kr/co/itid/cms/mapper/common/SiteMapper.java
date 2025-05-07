package kr.co.itid.cms.mapper.common;

import kr.co.itid.cms.dto.common.site.SiteResponse;
import kr.co.itid.cms.entity.common.Site;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SiteMapper {
    SiteResponse toResponse(Site site);
}
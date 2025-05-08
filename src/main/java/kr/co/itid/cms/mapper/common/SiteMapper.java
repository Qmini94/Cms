package kr.co.itid.cms.mapper.common;

import kr.co.itid.cms.dto.cms.core.site.SiteResponse;
import kr.co.itid.cms.entity.cms.core.site.Site;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SiteMapper {
    SiteResponse toResponse(Site site);
}
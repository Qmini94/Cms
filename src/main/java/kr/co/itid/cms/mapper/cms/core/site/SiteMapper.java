package kr.co.itid.cms.mapper.cms.core.site;

import kr.co.itid.cms.dto.cms.core.site.request.SiteRequest;
import kr.co.itid.cms.dto.cms.core.site.response.SiteResponse;
import kr.co.itid.cms.entity.cms.core.site.Site;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SiteMapper {
    Site toEntity(SiteRequest request);
    SiteResponse toResponse(Site site);
}
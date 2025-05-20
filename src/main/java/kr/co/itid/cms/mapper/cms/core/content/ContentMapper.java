package kr.co.itid.cms.mapper.cms.core.content;

import kr.co.itid.cms.dto.cms.core.content.request.ContentRequest;
import kr.co.itid.cms.dto.cms.core.content.response.ContentResponse;
import kr.co.itid.cms.entity.cms.core.content.Content;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ContentMapper {

    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Mapping(target = "isUse", constant = "true")
    @Mapping(target = "createdDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedDate", ignore = true)
    Content toEntity(ContentRequest request);

    @Mapping(target = "createdDate", expression = "java(formatDateTime(entity.getCreatedDate()))")
    @Mapping(target = "updatedDate", expression = "java(formatDateTime(entity.getUpdatedDate()))")
    ContentResponse toResponse(Content entity);

    List<ContentResponse> toResponseList(List<Content> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "updatedDate", expression = "java(java.time.LocalDateTime.now())")
    void updateEntity(@MappingTarget Content entity, ContentRequest request);

    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(FORMATTER);
    }
}

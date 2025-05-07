package kr.co.itid.cms.mapper.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.BoardMasterListResponse;
import kr.co.itid.cms.dto.cms.core.board.BoardMasterRequest;
import kr.co.itid.cms.dto.cms.core.board.BoardMasterResponse;
import kr.co.itid.cms.entity.cms.core.BoardMaster;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BoardMapper {

    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Mapping(target = "id", source = "id")
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    BoardMaster toEntity(BoardMasterRequest request, Long id);

    @Mapping(target = "createdDate", expression = "java(formatDateTime(entity.getCreatedDate()))")
    @Mapping(target = "updatedDate", expression = "java(formatDateTime(entity.getUpdatedDate()))")
    BoardMasterResponse toResponse(BoardMaster entity);

    List<BoardMasterListResponse> toResponseList(List<BoardMaster> entities);

    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(FORMATTER);
    }

    @AfterMapping
    default void setDefaultValues(BoardMasterRequest request, @MappingTarget BoardMaster entity) {
        // 날짜 필드는 JPA가 자동으로 처리하도록 무시합니다
    }
}
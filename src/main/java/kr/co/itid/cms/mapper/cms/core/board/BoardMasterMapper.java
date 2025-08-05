package kr.co.itid.cms.mapper.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterListResponse;
import kr.co.itid.cms.dto.cms.core.board.request.BoardMasterRequest;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterResponse;
import kr.co.itid.cms.entity.cms.core.board.BoardMaster;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BoardMasterMapper {

    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Mapping(target = "idx", source = "idx")
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    BoardMaster toEntity(BoardMasterRequest request, Long idx);

    @Mapping(target = "createdDate", expression = "java(formatDateTime(entity.getCreatedDate()))")
    @Mapping(target = "updatedDate", expression = "java(formatDateTime(entity.getUpdatedDate()))")
    BoardMasterResponse toResponse(BoardMaster entity);

    BoardMasterListResponse toListResponse(BoardMaster entity);

    @Named("formatDateTime")
    default String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(FORMATTER);
    }
}
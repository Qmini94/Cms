package kr.co.itid.cms.mapper.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.BoardMasterRequest;
import kr.co.itid.cms.dto.cms.core.board.BoardMasterResponse;
import kr.co.itid.cms.entity.cms.core.BoardMaster;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class BoardMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static BoardMaster toEntity(BoardMasterRequest dto, Long id) {
        BoardMaster entity = new BoardMaster();
        entity.setId(id);
        entity.setBoardId(dto.getBoardId());
        entity.setBoardName(dto.getBoardName());
        entity.setDescription(dto.getDescription());
        entity.setIsUse(dto.getIsUse());
        entity.setBoardType(dto.getBoardType());
        entity.setExtendsOption(dto.getExtendsOption());
        return entity;
    }

    public static BoardMasterResponse toResponse(BoardMaster entity) {
        return BoardMasterResponse.builder()
                .id(entity.getId())
                .boardId(entity.getBoardId())
                .boardName(entity.getBoardName())
                .isUse(entity.getIsUse())
                .createdDate(formatDateTime(entity.getCreatedDate()))
                .updatedDate(formatDateTime(entity.getUpdatedDate()))
                .build();
    }

    public static List<BoardMasterResponse> toResponseList(List<BoardMaster> entities) {
        return entities.stream()
                .map(BoardMapper::toResponse)
                .collect(Collectors.toList());
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(FORMATTER);
    }
}
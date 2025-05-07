package kr.co.itid.cms.mapper.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.BoardMasterRequest;
import kr.co.itid.cms.dto.cms.core.board.BoardMasterResponse;
import kr.co.itid.cms.entity.cms.core.BoardMaster;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class BoardMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static BoardMaster toEntity(BoardMasterRequest dto) {
        BoardMaster entity = new BoardMaster();
        entity.setBoardId(dto.getBoardId());
        entity.setBoardName(dto.getBoardName());
        entity.setDescription(dto.getDescription());
        entity.setUseYn(dto.getUseYn());
        entity.setBoardType(dto.getBoardType());
        entity.setExtendsOption(dto.getExtendsOption());
        return entity;
    }

    public static BoardMasterResponse toResponse(BoardMaster entity) {
        return BoardMasterResponse.builder()
                .id(entity.getId())
                .boardId(entity.getBoardId())
                .boardName(entity.getBoardName())
                .useYn(entity.getUseYn())
                .regDate(formatTimestamp(entity.getRegDate()))
                .updatedDate(formatTimestamp(entity.getUpdatedDate()))
                .build();
    }

    public static List<BoardMasterResponse> toResponseList(List<BoardMaster> entities) {
        return entities.stream()
                .map(BoardMapper::toResponse)
                .collect(Collectors.toList());
    }

    private static String formatTimestamp(Integer timestamp) {
        if (timestamp == null || timestamp == 0) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC)
                .format(FORMATTER);
    }
}
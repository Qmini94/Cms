package kr.co.itid.cms.mapper.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.request.BoardRequest;
import kr.co.itid.cms.dto.cms.core.board.response.BoardResponse;
import kr.co.itid.cms.dto.cms.core.board.response.BoardViewResponse;
import kr.co.itid.cms.entity.cms.core.board.Board;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BoardMapper {
    Board toEntity(BoardRequest request);
    BoardResponse toResponse(Board board);
    BoardViewResponse toResponseView(Board board);
}

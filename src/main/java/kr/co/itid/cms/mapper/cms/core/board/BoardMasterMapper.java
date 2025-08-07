package kr.co.itid.cms.mapper.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterListResponse;
import kr.co.itid.cms.entity.cms.core.board.BoardMaster;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BoardMasterMapper {
    BoardMasterListResponse toListResponse(BoardMaster entity);
}
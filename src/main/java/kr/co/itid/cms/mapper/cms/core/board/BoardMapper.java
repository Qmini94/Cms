package kr.co.itid.cms.mapper.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.BoardMasterRequest;
import kr.co.itid.cms.entity.cms.core.BoardMaster;

public class BoardMapper {
    public static BoardMaster toEntity(BoardMasterRequest dto) {
        BoardMaster entity = new BoardMaster();
        entity.setBoardId(dto.getBoardId());
        entity.setBoardName(dto.getBoardName());
        entity.setDescription(dto.getDescription());
        entity.setUseYn(dto.getUseYn());
        entity.setSkinType(dto.getSkinType());
        entity.setExtendsOption(dto.getExtendsOption());
        return entity;
    }
}
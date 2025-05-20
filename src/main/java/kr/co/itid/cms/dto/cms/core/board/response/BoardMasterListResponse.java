package kr.co.itid.cms.dto.cms.core.board.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardMasterListResponse {
    private Long idx;
    private String boardId;
    private String boardName;
    private String description;
    private Boolean isUse;
    private String boardType;
    private String createdDate;
    private String updatedDate;
}
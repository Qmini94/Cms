package kr.co.itid.cms.dto.cms.core.board;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardMasterResponse {
    private Long id;
    private String boardId;
    private String boardName;
    private Boolean isUse;
    private String createdDate;
    private String updatedDate;
}

package kr.co.itid.cms.dto.cms.core.board.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardResponse {
    private Long idx;
    private String boardId;
    private String title;
    private String regName;
    private String openStatus;
    private Boolean isApproved;
    private LocalDateTime createdDate;
}

package kr.co.itid.cms.dto.cms.core.board.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardViewResponse {
    private Long idx;
    private String title;
    private String content;
    private String regName;
    private int viewCount;
    private LocalDateTime createdDate;
}

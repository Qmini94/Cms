package kr.co.itid.cms.dto.cms.core.content;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentResponse {
    private Integer idx;
    private Integer menuIdx;
    private Boolean isUse;
    private String subject;
    private String content;
    private String createdDate;
    private String createdBy;
    private String updatedDate;
    private String updatedBy;
}

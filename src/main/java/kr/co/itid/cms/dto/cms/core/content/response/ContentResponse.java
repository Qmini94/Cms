package kr.co.itid.cms.dto.cms.core.content.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentResponse {
    private Integer idx;
    private Integer parentId;
    private Integer sort;
    private Boolean isUse;
    private String title;
    private String content;
    private String hostname;
    private String createdDate;
    private String createdBy;
    private String updatedDate;
    private String updatedBy;
}
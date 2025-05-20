package kr.co.itid.cms.dto.cms.core.board.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardMasterResponse {
    private Long idx;
    private String boardId;
    private String boardName;
    private String description;
    private Boolean isUse;
    private Integer boardType;
    private Boolean isAdminApproval;
    private Boolean isPrivacyOption;
    private Integer maxFileUpload;
    private Integer maxTotalFileSize;
    private String restrictedFiles;
    private Integer maxFileSize;
    private String allowedImages;
    private Integer maxImageSize;
    private Boolean isSmsAlert;
    private Boolean isRequiredFields;
    private Boolean isComment;
    private Boolean isUsePeriod;
    private Boolean isAuthorPostsView;
    private Boolean isAdminDeletedView;
    private Integer listCount;
    private Boolean isShowAuthor;
    private Boolean isShowDate;
    private Boolean isSearchFieldControl;
    private Boolean isTopPost;
    private String extendsOption;
    private String createdDate;
    private String updatedDate;
}
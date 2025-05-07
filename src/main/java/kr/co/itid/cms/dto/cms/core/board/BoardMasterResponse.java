package kr.co.itid.cms.dto.cms.core.board;

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
    private String boardType;
    private Boolean isAdminApproval;
    private Boolean isPrivacyOption;
    private Boolean isPrivacyDefault;
    private Integer maxFileUpload;
    private Integer maxTotalFileSize;
    private String restrictedFiles;
    private Integer maxFileSize;
    private String allowedImages;
    private Integer maxImageSize;
    private Boolean isSmsAlert;
    private Boolean isRequiredFields;
    private Boolean isFileRequired;
    private Boolean isAdminReply;
    private Boolean isReplySmsAlert;
    private Boolean isShowFileImage;
    private Boolean isComment;
    private Boolean isAdminExcelExport;
    private Boolean isListAttachment;
    private Boolean isUsePeriod;
    private Boolean isAuthorPostsView;
    private Boolean isAdminDeletedView;
    private Integer listCount;
    private Boolean isListCountSearch;
    private Integer pageListCount;
    private Boolean isContentCut;
    private Integer postDisplayPeriod;
    private Boolean isHideName;
    private Boolean isShowPostName;
    private Boolean isShowPostTitle;
    private Boolean isShowAuthor;
    private Boolean isShowDate;
    private Boolean isSearchFieldControl;
    private Boolean isUseStatus;
    private Boolean isStatusManagement;
    private Boolean isTopPost;
    private Boolean isTopLimit;
    private Integer topLimitCount;
    private Boolean isReceivePosts;
    private Boolean isSendPosts;
    private String extendsOption;
    private String createdDate;
    private String updatedDate;
}
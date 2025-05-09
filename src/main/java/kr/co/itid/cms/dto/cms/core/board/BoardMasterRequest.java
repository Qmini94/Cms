package kr.co.itid.cms.dto.cms.core.board;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class BoardMasterRequest {

    private Long idx;

    @NotBlank(message = "boardId is required")
    @Size(max = 50, message = "boardId must be less than 50 characters")
    private String boardId;

    @NotBlank(message = "boardName is required")
    @Size(max = 100, message = "boardName must be less than 100 characters")
    private String boardName;

    @Size(max = 2000, message = "Description too long")
    private String description;

    private Boolean isUse = true;

    @Size(max = 50, message = "BoardType max length is 50")
    private String boardType;

    private Boolean isAdminApproval = false;
    private Boolean isPrivacyDefault = false;

    @Max(value = 100, message = "Maximum file upload count should not exceed 100")
    private Integer maxFileUpload = 0;

    @Max(value = 1000000, message = "Total file size should not exceed 1000000KB")
    private Integer maxTotalFileSize = 0;

    @Size(max = 255, message = "restrictedFiles must be less than 255 characters")
    private String restrictedFiles;

    @Max(value = 500000, message = "File size should not exceed 500000KB")
    private Integer maxFileSize = 0;

    @Size(max = 255, message = "allowedImages must be less than 255 characters")
    private String allowedImages;

    @Max(value = 100000, message = "Image size should not exceed 100000KB")
    private Integer maxImageSize = 0;

    private Boolean isSmsAlert = false;
    private Boolean isRequiredFields = false;
    private Boolean isComment = false;
    private Boolean isUsePeriod = false;
    private Boolean isAuthorPostsView = false;
    private Boolean isAdminDeletedView = false;

    @Min(value = 1, message = "List count should be at least 1")
    @Max(value = 100, message = "List count should not exceed 100")
    private Integer listCount = 10;

    private Boolean isShowAuthor = true;
    private Boolean isShowDate = true;
    private Boolean isSearchFieldControl = false;
    private Boolean isTopPost = false;

    @Size(max = 10000, message = "Extends option is too long")
    private String extendsOption;
}
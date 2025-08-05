package kr.co.itid.cms.dto.cms.core.board.request;

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

    @NotBlank(message = "게시판 ID는 필수입니다.")
    @Size(max = 50, message = "게시판 ID는 50자 이내여야 합니다.")
    private String boardId;

    @NotBlank(message = "게시판 이름은 필수입니다.")
    @Size(max = 100, message = "게시판 이름은 100자 이내여야 합니다.")
    private String boardName;

    @Size(max = 2000, message = "게시판 설명은 2000자 이내여야 합니다.")
    private String description;

    private Boolean isUse = true;

    @NotBlank(message = "게시판 유형은 필수입니다.")
    @Size(max = 100, message = "게시판 유형은 100자 이내여야 합니다.")
    private String boardType;

    private Boolean isAdminApproval = false;
    private Boolean isPrivacyOption = false;

    @Max(value = 100, message = "첨부파일 개수는 최대 100개까지 가능합니다.")
    private Integer maxFileUpload = 0;

    @Max(value = 1000000, message = "전체 파일 크기는 최대 1,000,000KB를 초과할 수 없습니다.")
    private Integer maxTotalFileSize = 0;

    @Size(max = 255, message = "제한 파일 확장자는 255자 이내여야 합니다.")
    private String restrictedFiles;

    @Max(value = 500000, message = "파일 하나의 최대 크기는 500,000KB를 초과할 수 없습니다.")
    private Integer maxFileSize = 0;

    @Size(max = 255, message = "허용 이미지 확장자는 255자 이내여야 합니다.")
    private String allowedImages;

    @Max(value = 100000, message = "이미지 크기는 최대 100,000KB를 초과할 수 없습니다.")
    private Integer maxImageSize = 0;

    private Boolean isSmsAlert = false;
    private Boolean isRequiredFields = false;
    private Boolean isComment = false;
    private Boolean isUsePeriod = false;
    private Boolean isAuthorPostsView = false;
    private Boolean isAdminDeletedView = false;

    @Min(value = 1, message = "목록 개수는 최소 1 이상이어야 합니다.")
    @Max(value = 100, message = "목록 개수는 최대 100까지 가능합니다.")
    private Integer listCount = 10;

    private Boolean isShowAuthor = true;
    private Boolean isShowDate = true;
    private Boolean isSearchFieldControl = false;
    private Boolean isTopPost = false;

    @Size(max = 10000, message = "확장 설정은 10,000자 이내여야 합니다.")
    private String extendsOption;
}
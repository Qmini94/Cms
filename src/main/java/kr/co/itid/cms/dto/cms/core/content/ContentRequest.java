package kr.co.itid.cms.dto.cms.core.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

/**
 * 콘텐츠 등록/수정 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentRequest {

    private Integer parentId;

    private Integer sort = 0;

    private Boolean isUse = true;

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 최대 200자까지 가능합니다")
    private String title;

    @NotBlank(message = "본문은 필수입니다")
    private String content;

    @NotBlank(message = "hostname은 필수입니다")
    @Size(max = 30, message = "hostname은 최대 30자까지 가능합니다")
    private String hostname;

    @Size(max = 30, message = "작성자 ID는 최대 30자까지 가능합니다")
    private String createdBy;

    @Size(max = 30, message = "수정자 ID는 최대 30자까지 가능합니다")
    private String updatedBy;
}
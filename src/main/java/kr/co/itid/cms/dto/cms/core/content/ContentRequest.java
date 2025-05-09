package kr.co.itid.cms.dto.cms.core.content;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 콘텐츠 등록/수정 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentRequest {

    @NotNull(message = "menuIdx는 필수입니다")
    private Integer menuIdx;

    private Boolean isUse = true;

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 최대 200자까지 가능합니다")
    private String subject;

    @NotBlank(message = "본문은 필수입니다")
    private String content;

    @Size(max = 30, message = "작성자 ID는 최대 30자까지 가능합니다")
    private String createdBy;

    @Size(max = 30, message = "수정자 ID는 최대 30자까지 가능합니다")
    private String updatedBy;
}

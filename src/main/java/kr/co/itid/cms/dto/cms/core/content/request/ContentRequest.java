package kr.co.itid.cms.dto.cms.core.content.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

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
}
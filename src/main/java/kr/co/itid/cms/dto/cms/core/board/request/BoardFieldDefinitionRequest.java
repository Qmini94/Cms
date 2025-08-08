package kr.co.itid.cms.dto.cms.core.board.request;

import lombok.*;

import javax.validation.constraints.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardFieldDefinitionRequest {

    // board_master_idx는 보통 상위 래퍼에서 전달(필요 시 열어둠)
    private Long boardMasterIdx;

    @NotBlank
    @Size(max = 100)
    // DB 컬럼명 규칙(영문, 숫자, 언더스코어만). 필요 시 변경
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "field_name은 영문으로 시작하고 영문/숫자/언더스코어만 가능합니다.")
    private String fieldName;

    @NotBlank
    @Size(max = 100)
    private String displayName;

    @NotBlank
    @Size(max = 50)
    // 예: "VARCHAR(255)", "TEXT", "INT", "DATE", "DATETIME", "ENUM('y','n')" 등
    private String fieldType;

    @Builder.Default
    private Boolean isRequired = false;

    @Builder.Default
    private Boolean isSearchable = false;

    @Builder.Default
    private Integer fieldOrder = 0;

    @Size(max = 255)
    private String defaultValue;

    @Size(max = 255)
    private String placeholder;
}

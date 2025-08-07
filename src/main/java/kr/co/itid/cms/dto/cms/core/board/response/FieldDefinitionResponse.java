package kr.co.itid.cms.dto.cms.core.board.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
public class FieldDefinitionResponse {
    private Long id;
    private Long boardMasterIdx;
    private String fieldName;
    private String displayName;
    private String fieldType;
    private boolean required;
    private boolean searchable;
    private int fieldOrder;
    private String defaultValue;
    private String placeholder;
}
package kr.co.itid.cms.dto.cms.core.board.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
package kr.co.itid.cms.dto.cms.core.board.response;

import lombok.*;

@Getter
@Builder
public class BoardFieldDefinitionResponse {
    private Long id;
    private Long boardMasterIdx;
    private String fieldName;
    private String displayName;
    private String fieldType;
    private Boolean isRequired;
    private Boolean isSearchable;
    private Integer fieldOrder;
    private String defaultValue;
    private String placeholder;

    // BoardMasterResponse가 String 날짜를 쓰고 있으니 맞춰서 String 권장
    private String createdDate;
    private String updatedDate;
}

package kr.co.itid.cms.dto.cms.core.board.request;

import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor @AllArgsConstructor @Builder
public class BoardUpdateRequest {
    @NotNull
    private Long idx;

    @Valid
    @NotNull
    private BoardMasterRequest master;

    @Valid
    @NotNull
    private List<BoardFieldDefinitionRequest> fields;
}

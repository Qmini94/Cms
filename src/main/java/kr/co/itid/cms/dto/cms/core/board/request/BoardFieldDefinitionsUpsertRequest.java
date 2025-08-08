package kr.co.itid.cms.dto.cms.core.board.request;

import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardFieldDefinitionsUpsertRequest {

    @NotNull
    private Long boardMasterIdx;

    @Valid
    @NotNull
    @Size(min = 0)
    private List<BoardFieldDefinitionRequest> fields;
}

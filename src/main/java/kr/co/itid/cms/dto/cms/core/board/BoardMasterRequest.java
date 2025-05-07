package kr.co.itid.cms.dto.cms.core.board;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class BoardMasterRequest {
    @NotBlank(message = "boardId is required")
    private String boardId;

    @NotBlank(message = "boardName is required")
    private String boardName;

    @Size(max = 2000, message = "Description too long")
    private String description;

    private Boolean isUse = true;

    @Size(max = 50, message = "BoardType max length is 50")
    private String BoardType;

    private String extendsOption;
}


package kr.co.itid.cms.dto.cms.core.board;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class BoardMasterRequest {

    @NotBlank(message = "boardId is required")
    private String boardId;

    @NotBlank(message = "boardName is required")
    private String boardName;

    @Size(max = 2000, message = "Description too long")
    private String description;

    private Boolean useYn = true;

    @Size(max = 50, message = "skinType max length is 50")
    private String skinType;

    private String extendsOption;
}


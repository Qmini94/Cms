package kr.co.itid.cms.dto.cms.core.menu;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MenuTypeValueResponse {
    private String type;
    private String value;
}


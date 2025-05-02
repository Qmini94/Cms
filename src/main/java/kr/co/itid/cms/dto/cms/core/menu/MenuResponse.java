package kr.co.itid.cms.dto.cms.core.menu;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MenuResponse {
    private final Long id;
    private final Long parentId;
    private final String title;
    private final String type;
    private final String value;
    private final String display;
    private final String pathUrl;
    private final String pathId;
}
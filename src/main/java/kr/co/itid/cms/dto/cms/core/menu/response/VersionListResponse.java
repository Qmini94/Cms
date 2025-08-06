package kr.co.itid.cms.dto.cms.core.menu.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class VersionListResponse {
    private List<String> fileNames;
    private String activeFileName;

    public VersionListResponse(List<String> fileNames, String activeFileName) {
        this.fileNames = fileNames;
        this.activeFileName = activeFileName;
    }
}

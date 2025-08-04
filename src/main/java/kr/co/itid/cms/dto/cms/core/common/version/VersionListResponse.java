package kr.co.itid.cms.dto.cms.core.common.version;

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

    public List<String> getFileNames() {
        return fileNames;
    }

    public String getActiveFileName() {
        return activeFileName;
    }

    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }

    public void setActiveFileName(String activeFileName) {
        this.activeFileName = activeFileName;
    }
}

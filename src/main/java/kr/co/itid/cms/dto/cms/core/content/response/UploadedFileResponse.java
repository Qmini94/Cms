package kr.co.itid.cms.dto.cms.core.content.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UploadedFileResponse {
    private String filename;   // 파일명
    private Long size;         // 바이트 단위 용량
    private String uploadedAt; // 업로드일(문자열 포맷)
    private String url;        // 접근 경로: /contents/cms/{parentId}/{filename}
}
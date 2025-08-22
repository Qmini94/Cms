package kr.co.itid.cms.dto.cms.core.template.response;

import kr.co.itid.cms.enums.LayoutKind;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class LayoutResponse {
    private Long layoutId;
    private Long siteIdx;
    private LayoutKind kind;
    private Integer version;
    private Boolean published;
    private String html;
    private List<String> cssUrls;
    private List<String> jsUrls;
    private OffsetDateTime updatedAt; // 엔티티에 수정일이 없다면 null 가능
}

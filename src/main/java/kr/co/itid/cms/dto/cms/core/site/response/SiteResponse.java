package kr.co.itid.cms.dto.cms.core.site.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SiteResponse {
    private Integer idx;
    private String siteName;
    private String siteHostName;
    private String siteDomain;
    private Boolean isDeleted;
    private String siteOption;
    private String allowIp;
    private String denyIp;
}
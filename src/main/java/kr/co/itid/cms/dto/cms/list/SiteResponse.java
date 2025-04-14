package kr.co.itid.cms.dto.cms.list;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SiteResponse {
    private Integer idx;
    private String siteName;
    private String siteDomain;
    private String sitePort;
    private String siteOption;
    private String language;
    private String admin;
    private Integer sessionTimeout;
    private String sessionLayer;
    private String company;
    private String ddd;
    private String tel;
    private String sessionDupLogin;
    private String sslMode;
    private String siren24Id;
    private String siren24No;
    private String umsId;
    private String umsKey;
    private Integer treeId;
    private String privacyCheck;
    private String badText;
    private String badTextOption;
    private String naverApiKey;
    private String naverMapKey;
    private String googleMapKey;
    private String csApiUrl;
    private String csApiKey;
    private String digitomiDomain;
    private String digitomiApi;
    private String digitomiKey;
    private String digitomiReturnUrl;
}

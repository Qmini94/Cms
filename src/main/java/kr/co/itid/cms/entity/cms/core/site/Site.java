package kr.co.itid.cms.entity.cms.core.site;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "site")
@Getter
@Setter
@NoArgsConstructor
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "site_name")
    private String siteName;

    @Column(name = "site_domain")
    private String siteDomain;

    @Column(name = "site_port", nullable = false)
    private String sitePort;

    @Column(name = "site_option", nullable = false)
    private String siteOption;

    @Column(name = "language", nullable = false)
    private String language;

    @Column(name = "siren24_id")
    private String siren24Id;

    @Column(name = "siren24_no")
    private String siren24No;

    @Column(name = "ums_id")
    private String umsId;

    @Column(name = "ums_key")
    private String umsKey;

    @Column(name = "privacy_check")
    private String privacyCheck;

    @Column(name = "badtext", columnDefinition = "TEXT")
    private String badText;

    @Column(name = "badtext_option", nullable = false)
    private String badTextOption;

    @Column(name = "naver_api_key")
    private String naverApiKey;

    @Column(name = "naver_map_key")
    private String naverMapKey;

    @Column(name = "google_map_key")
    private String googleMapKey;

    @Column(name = "cs_api_url")
    private String csApiUrl;

    @Column(name = "cs_api_key")
    private String csApiKey;

    @Column(name = "digitomi_domain")
    private String digitomiDomain;

    @Column(name = "digitomi_api")
    private String digitomiApi;

    @Column(name = "digitomi_key")
    private String digitomiKey;

    @Column(name = "digitomi_return_url")
    private String digitomiReturnUrl;
}

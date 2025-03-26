package kr.co.itid.cms.entity.cms;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "_site")
@Getter
@Setter
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

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

    @Column(name = "admin")
    private String admin;

    @Column(name = "session_timeout", nullable = false)
    private Integer sessionTimeout;

    @Column(name = "session_layer", nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionOption sessionLayer;

    @Column(name = "company")
    private String company;

    @Column(name = "ddd")
    private String ddd;

    @Column(name = "tel")
    private String tel;

    @Column(name = "session_dup_login", nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionOption sessionDupLogin;

    @Column(name = "ssl_mode", nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionOption sslMode;

    @Column(name = "siren24_id")
    private String siren24Id;

    @Column(name = "siren24_no")
    private String siren24No;

    @Column(name = "ums_id")
    private String umsId;

    @Column(name = "ums_key")
    private String umsKey;

    @Column(name = "tree_id")
    private Integer treeId;

    @Column(name = "privacy_check")
    private String privacyCheck;

    @Column(name = "badtext", columnDefinition = "TEXT")
    private String badText;

    @Column(name = "badtext_option", nullable = false)
    @Enumerated(EnumType.STRING)
    private BadTextOption badTextOption;

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

    // enum 정의
    public enum SessionOption {
        off, on
    }

    public enum BadTextOption {
        TRUE("true"),
        FALSE("false"),
        REPLACE("replace");

        private final String value;

        BadTextOption(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}

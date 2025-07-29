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

    @Column(name = "site_hostname")
    private String siteHostName;

    @Column(name = "site_domain")
    private String siteDomain;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "is_open", nullable = false)
    private Boolean isOpen = false;

    @Column(name = "allow_ip", columnDefinition = "TEXT")
    private String allowIp;

    @Column(name = "deny_ip", columnDefinition = "TEXT")
    private String denyIp;
}
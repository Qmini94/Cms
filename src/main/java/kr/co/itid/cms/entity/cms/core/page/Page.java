package kr.co.itid.cms.entity.cms.core.page;

import kr.co.itid.cms.entity.cms.core.site.Site;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "cms_page")
@Getter
@Setter
@NoArgsConstructor
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_idx", nullable = false)
    private Site site;

    @Column(name = "path", length = 512, nullable = false)
    private String path; // 예: "/www", "/www/notice"

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_version_idx")
    private PageVersion currentVersion;
}

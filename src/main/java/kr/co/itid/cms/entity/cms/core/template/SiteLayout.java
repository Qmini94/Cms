package kr.co.itid.cms.entity.cms.core.template;

import kr.co.itid.cms.entity.cms.core.site.Site;
import kr.co.itid.cms.enums.LayoutKind;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cms_site_layout")
@Getter
@Setter
@NoArgsConstructor
public class SiteLayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_idx", nullable = false)
    private Site site;

    @Enumerated(EnumType.STRING)  // ← Enum을 문자열로 저장
    @Column(name = "kind", length = 20, nullable = false)
    private LayoutKind kind;

    @Column(name = "html", columnDefinition = "LONGTEXT", nullable = false)
    private String html;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Column(name = "is_published", nullable = false)
    private Boolean isPublished = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name="extra_css_urls", columnDefinition="LONGTEXT")
    private String extraCssUrlsJson;   // JSON 배열 문자열

    @Column(name="extra_js_urls", columnDefinition="LONGTEXT")
    private String extraJsUrlsJson;    // JSON 배열 문자열
}
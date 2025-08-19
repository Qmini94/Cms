package kr.co.itid.cms.entity.cms.core.page;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cms_page_version")
@Getter
@Setter
@NoArgsConstructor
public class PageVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_idx", nullable = false)
    private Page page;

    @Column(name = "content_html", columnDefinition = "LONGTEXT", nullable = false)
    private String contentHtml;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "created_by", length = 100)
    private String createdBy;
}

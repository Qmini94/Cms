package kr.co.itid.cms.entity.cms.core.content;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "contents")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Content {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    @Column(name = "parent_id")
    private Integer parentId;

    @Column(name = "sort", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer sort = 0;

    @Column(name = "is_use", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isUse = false;

    @Column(length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(name = "hostname", nullable = false, length = 30)
    private String hostname;

    @Column(name = "updated_by", length = 30)
    private String updatedBy;

    @Column(name = "created_by", length = 30)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @CreatedDate
    @Column(name = "created_date")
    private LocalDateTime createdDate;
}
package kr.co.itid.cms.entity.cms.core.board;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "board")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "board_id", length = 50)
    private String boardId;

    @Column(name = "depart_name", length = 250)
    private String departName;

    @Column(name = "reg_pin", length = 250)
    private String regPin;

    @Column(name = "reg_id", length = 250)
    private String regId;

    @Column(name = "reg_name", length = 30)
    private String regName;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    @Column(name = "reg_ip", length = 15)
    private String regIp;

    @Column(name = "is_deleted", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDeleted = false;

    @Column(name = "search_tag", length = 100)
    private String searchTag;

    @Column(name = "is_top_fixed", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isTopFixed = false;

    @Column(name = "top_start")
    private LocalDateTime topStart;

    @Column(name = "top_end")
    private LocalDateTime topEnd;

    @Column(name = "pidx")
    private Integer pidx;

    @Column(name = "level")
    private Integer level;

    @Column(name = "seq")
    private Integer seq;

    @Column(name = "sort", precision = 10, scale = 2)
    private Double sort;

    @Column(name = "admin_comment", columnDefinition = "LONGTEXT")
    private String adminComment;

    @Enumerated(EnumType.STRING)
    @Column(name = "admin_comment_to", columnDefinition = "ENUM('all','writer') DEFAULT 'writer'")
    private AdminCommentTo adminCommentTo = AdminCommentTo.writer;

    @Enumerated(EnumType.STRING)
    @Column(name = "open_status", columnDefinition = "ENUM('y','a','n') DEFAULT 'y'")
    private OpenStatus openStatus = OpenStatus.y;

    @Column(name = "is_approved", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isApproved = true;

    @Column(name = "category_1", length = 50)
    private String category1;

    @Column(name = "category_2", length = 50)
    private String category2;

    @Column(name = "process_1", length = 50)
    private String process1;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "contents", columnDefinition = "LONGTEXT")
    private String contents;

    @Column(name = "mainimage_idx")
    private Long mainimageIdx;

    @Column(name = "period_start", length = 10)
    private String periodStart;

    @Column(name = "period_end", length = 10)
    private String periodEnd;

    @Column(name = "contents_original", columnDefinition = "LONGTEXT")
    private String contentsOriginal;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    public void increaseViewCount() {
        if (this.viewCount == null) {
            this.viewCount = 1;
        } else {
            this.viewCount += 1;
        }
    }

    public enum AdminCommentTo {
        all, writer
    }

    public enum OpenStatus {
        y, a, n
    }
}
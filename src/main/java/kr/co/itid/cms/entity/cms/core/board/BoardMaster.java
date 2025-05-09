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
@Table(name = "board_master")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class BoardMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "board_id", nullable = false, unique = true, length = 50)
    private String boardId;

    @Column(name = "board_name", nullable = false, length = 100)
    private String boardName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_use", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isUse = true;

    @Column(name = "board_type", length = 50)
    private String boardType;

    @Column(name = "is_admin_approval", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isAdminApproval = false;

    @Column(name = "is_privacy_default", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isPrivacyDefault = false;

    @Column(name = "max_file_upload")
    private Integer maxFileUpload = 0;

    @Column(name = "max_total_file_size")
    private Integer maxTotalFileSize = 0;

    @Column(name = "restricted_files", length = 255)
    private String restrictedFiles;

    @Column(name = "max_file_size")
    private Integer maxFileSize = 0;

    @Column(name = "allowed_images", length = 255)
    private String allowedImages;

    @Column(name = "max_image_size")
    private Integer maxImageSize = 0;

    @Column(name = "is_sms_alert", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isSmsAlert = false;

    @Column(name = "is_required_fields", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isRequiredFields = false;

    @Column(name = "is_comment", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isComment = false;

    @Column(name = "is_use_period", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isUsePeriod = false;

    @Column(name = "is_author_posts_view", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isAuthorPostsView = false;

    @Column(name = "is_admin_deleted_view", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isAdminDeletedView = false;

    @Column(name = "list_count")
    private Integer listCount = 10;

    @Column(name = "is_show_author", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isShowAuthor = true;

    @Column(name = "is_show_date", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isShowDate = true;

    @Column(name = "is_search_field_control", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isSearchFieldControl = false;

    @Column(name = "is_top_post", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isTopPost = false;

    @Column(name = "extends_option", columnDefinition = "TEXT")
    private String extendsOption;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;
}

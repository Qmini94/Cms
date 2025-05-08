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

    @Column(name = "is_use")
    private Boolean isUse = true;

    @Column(name = "board_type", length = 50)
    private String boardType;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    @Column(name = "is_admin_approval")
    private Boolean isAdminApproval = false;

    @Column(name = "is_privacy_option")
    private Boolean isPrivacyOption = false;

    @Column(name = "is_privacy_default")
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

    @Column(name = "is_sms_alert")
    private Boolean isSmsAlert = false;

    @Column(name = "is_required_fields")
    private Boolean isRequiredFields = false;

    @Column(name = "is_file_required")
    private Boolean isFileRequired = false;

    @Column(name = "is_admin_reply")
    private Boolean isAdminReply = false;

    @Column(name = "is_reply_sms_alert")
    private Boolean isReplySmsAlert = false;

    @Column(name = "is_show_file_image")
    private Boolean isShowFileImage = false;

    @Column(name = "is_comment")
    private Boolean isComment = false;

    @Column(name = "is_admin_excel_export")
    private Boolean isAdminExcelExport = false;

    @Column(name = "is_list_attachment")
    private Boolean isListAttachment = false;

    @Column(name = "is_use_period")
    private Boolean isUsePeriod = false;

    @Column(name = "is_author_posts_view")
    private Boolean isAuthorPostsView = false;

    @Column(name = "is_admin_deleted_view")
    private Boolean isAdminDeletedView = false;

    @Column(name = "list_count")
    private Integer listCount = 10;

    @Column(name = "is_list_count_search")
    private Boolean isListCountSearch = false;

    @Column(name = "page_list_count")
    private Integer pageListCount = 10;

    @Column(name = "is_content_cut")
    private Boolean isContentCut = false;

    @Column(name = "post_display_period")
    private Integer postDisplayPeriod = 0;

    @Column(name = "is_hide_name")
    private Boolean isHideName = false;

    @Column(name = "is_show_post_name")
    private Boolean isShowPostName = true;

    @Column(name = "is_show_post_title")
    private Boolean isShowPostTitle = true;

    @Column(name = "is_show_author")
    private Boolean isShowAuthor = true;

    @Column(name = "is_show_date")
    private Boolean isShowDate = true;

    @Column(name = "is_search_field_control")
    private Boolean isSearchFieldControl = false;

    @Column(name = "is_use_status")
    private Boolean isUseStatus = false;

    @Column(name = "is_status_management")
    private Boolean isStatusManagement = false;

    @Column(name = "is_top_post")
    private Boolean isTopPost = false;

    @Column(name = "is_top_limit")
    private Boolean isTopLimit = false;

    @Column(name = "top_limit_count")
    private Integer topLimitCount = 0;

    @Column(name = "is_receive_posts")
    private Boolean isReceivePosts = false;

    @Column(name = "is_send_posts")
    private Boolean isSendPosts = false;

    @Column(name = "extends_option", columnDefinition = "TEXT")
    private String extendsOption;
}
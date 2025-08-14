package kr.co.itid.cms.entity.cms.core.permission;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Table(name = "cms_permission")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    // ENUM('id','level')에 맞춰 저장 직전에 소문자 정규화/검증
    @Column(nullable = false, columnDefinition = "enum('id','level')")
    private String type;

    @Column(length = 30)
    private String value;

    @Column(columnDefinition = "enum('y','n') default 'n'")
    private String manage;

    @Column(columnDefinition = "enum('y','n') default 'n'")
    private String admin;

    @Column(columnDefinition = "enum('y','n') default 'n'")
    private String access;

    // 예약어 가능성 있는 컬럼은 백틱 인용
    @Column(name = "`view`", columnDefinition = "enum('y','n') default 'n'")
    private String view;

    @Column(name = "`write`", columnDefinition = "enum('y','n') default 'n'")
    private String write;

    @Column(name = "`modify`", columnDefinition = "enum('y','n') default 'n'")
    private String modify;

    @Column(columnDefinition = "enum('y','n') default 'n'")
    private String reply;

    @Column(columnDefinition = "enum('y','n') default 'n'")
    private String remove;

    private Integer sort;

    @Column(name = "reg_user", length = 30)
    private String regUser;

    @Column(name = "reg_date")
    private LocalDateTime regDate;

    @Column(name = "mod_user", length = 30)
    private String modUser;

    @Column(name = "mod_date")
    private LocalDateTime modDate;

    @Column(columnDefinition = "enum('y','n') default 'n'")
    private String del;

    @Column(name = "del_user", length = 30)
    private String delUser;

    @Column(name = "del_date")
    private LocalDateTime delDate;

    /* ====== 정규화/검증 로직 ====== */

    public void setType(String type) {
        this.type = normalizeType(type);
    }

    private static String normalizeType(String in) {
        if (in == null) throw new IllegalArgumentException("type cannot be null");
        String v = in.trim().toLowerCase(Locale.ROOT);
        if (!"id".equals(v) && !"level".equals(v)) {
            throw new IllegalArgumentException("Invalid type: " + in + " (allowed: 'id','level')");
        }
        return v;
    }
}
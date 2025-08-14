package kr.co.itid.cms.entity.cms.core.permission;

import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;

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

    @Column(columnDefinition = "enum('y','n') default 'n'")
    private String view;

    @Column(columnDefinition = "enum('y','n') default 'n'")
    private String write;

    @Column(columnDefinition = "enum('y','n') default 'n'")
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
}
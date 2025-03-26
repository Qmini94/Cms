package kr.co.itid.cms.entity.cms;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "_cms_menu")
@Getter
@Setter
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id", nullable = false)
    private Long parentId;

    @Column(name = "position", nullable = false)
    private Long position;

    @Column(name = "`left`", nullable = false) // reserved word
    private Long left;

    @Column(name = "`right`", nullable = false) // reserved word
    private Long right;

    @Column(name = "level", nullable = false)
    private Long level;

    @Column(name = "title")
    private String title;

    @Column(name = "name")
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "value")
    private String value;

    @Column(name = "display")
    @Enumerated(EnumType.STRING)
    private Display display;

    @Column(name = "_path_url")
    private String pathUrl;

    @Column(name = "_serial_no")
    private Integer serialNo;

    @Column(name = "_module")
    private String module;

    @Column(name = "_board_id")
    private String boardId;

    public enum Display {
        show, hide
    }
}

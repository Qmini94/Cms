package kr.co.itid.cms.entity.cms.core.menu;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "cms_menu")
@Getter
@Setter
@NoArgsConstructor
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 고유번호 (PK)

    @Column(name = "parent_id")
    private Long parentId;  // 상속메뉴

    @Column(name = "position", nullable = false)
    private int position;  // 트리 포지션

    @Column(name = "level", nullable = false)
    private Long level;  // 트리 레벨

    @Column(name = "title", length = 255)
    private String title;  // 메뉴명

    @Column(name = "name", length = 255)
    private String name;  // 메뉴 이름

    @Column(name = "type", length = 255)
    private String type;  // 메뉴 타입

    @Column(name = "value", length = 255)
    private String value;  // 구분값

    @Column(name = "is_show", nullable = false)
    private Boolean isShow;  // 디스플레이 여부

    @Column(name = "path_url", length = 255)
    private String pathUrl;  // 경로 URL

    @Column(name = "path_string", length = 255)
    private String pathString;  // 경로 경로명

    @Column(name = "path_id", length = 255)
    private String pathId;  // 경로 아이디
}
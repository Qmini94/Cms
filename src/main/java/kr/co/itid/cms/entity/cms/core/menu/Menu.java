package kr.co.itid.cms.entity.cms.core.menu;

import kr.co.itid.cms.util.BooleanToEnumStringConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "_cms_menu")
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
    private Integer position;  // 트리 포지션

    @Column(name = "level", nullable = false)
    private Integer level;  // 트리 레벨

    @Column(name = "title", length = 255)
    private String title;  // 메뉴명

    @Column(name = "name", length = 255)
    private String name;  // 메뉴 이름

    @Column(name = "type", length = 255)
    private String type;  // 메뉴 타입

    @Column(name = "value", length = 255)
    private String value;  // 구분값

    @Enumerated(EnumType.STRING)
    @Column(name = "display")
    private Display display;  // 표시 여부 (show, hide)

    @Column(name = "_opt_sns")
    @Convert(converter = BooleanToEnumStringConverter.class)
    private Boolean optSns;  // SNS 여부

    @Column(name = "_opt_shot_url")
    @Convert(converter = BooleanToEnumStringConverter.class)
    private Boolean optShortUrl;  // short_url 여부

    @Column(name = "_opt_qrcode")
    @Convert(converter = BooleanToEnumStringConverter.class)
    private Boolean optQrcode;  // QR코드 여부

    @Column(name = "_opt_mobile")
    @Convert(converter = BooleanToEnumStringConverter.class)
    private Boolean optMobile;  // 모바일 여부

    @Column(name = "_path_url", length = 255)
    private String pathUrl;  // 경로 URL

    @Column(name = "_path_id", length = 255)
    private String pathId;  // 경로 아이디

    @Column(name = "_navi", length = 400)
    private String navi;  // 네비게이션

    @Column(name = "_serial_no")
    private Integer serialNo;  // 구분값

    @Column(name = "_module", length = 200)
    private String module;  // 모듈명

    @Column(name = "_board_id", length = 200)
    private String boardId;  // 모듈 아이디

    @Column(name = "_search_opt")
    private String searchOpt;  // 검색 여부 (y/n)

    @Column(name = "_page_manager", length = 255)
    private String pageManager;  // 페이지 담당자

    public enum Display {
        show, hide
    }
}
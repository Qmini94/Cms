package kr.co.itid.cms.dto.cms.core.menu.request;

import kr.co.itid.cms.entity.cms.core.menu.Menu;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class MenuRequest {

    private Long id; // 수정 시 사용

    private Long parentId;

    @NotNull(message = "메뉴 위치는 필수입니다.")
    private Integer position;

    @NotNull(message = "메뉴 레벨은 필수입니다.")
    private Integer level;

    @NotBlank(message = "메뉴명은 필수입니다.")
    @Size(max = 255, message = "메뉴명은 255자 이내여야 합니다.")
    private String title;

    @NotBlank(message = "메뉴 이름은 필수입니다.")
    @Size(max = 255, message = "메뉴 이름은 255자 이내여야 합니다.")
    private String name;

    @NotBlank(message = "메뉴 타입은 필수입니다.")
    @Size(max = 255, message = "메뉴 타입은 255자 이내여야 합니다.")
    private String type;

    @Size(max = 255, message = "구분값은 255자 이내여야 합니다.")
    private String value;

    @NotNull(message = "표시 여부는 필수입니다.")
    private Menu.Display display;

    private Boolean optSns;
    private Boolean optShortUrl;
    private Boolean optQrcode;
    private Boolean optMobile;

    @Size(max = 255, message = "경로 URL은 255자 이내여야 합니다.")
    private String pathUrl;

    @Size(max = 255, message = "경로 ID는 255자 이내여야 합니다.")
    private String pathId;

    @Size(max = 400, message = "네비게이션은 400자 이내여야 합니다.")
    private String navi;

    private Integer serialNo;

    @Size(max = 200, message = "모듈명은 200자 이내여야 합니다.")
    private String module;

    @Size(max = 200, message = "게시판 ID는 200자 이내여야 합니다.")
    private String boardId;

    private String searchOpt;

    @Size(max = 255, message = "페이지 담당자는 255자 이내여야 합니다.")
    private String pageManager;
}

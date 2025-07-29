package kr.co.itid.cms.dto.cms.core.menu.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class MenuRequest {

    private Long id; // 수정 시 사용

    private Long parentId;

    @NotNull(message = "메뉴 위치는 필수입니다.")
    private int position;

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
    private Boolean isShow;

    @Size(max = 255, message = "경로 URL은 255자 이내여야 합니다.")
    private String pathUrl;

    @Size(max = 255, message = "경로명은 255자 이내여야 합니다.")
    private String pathString;

    @Size(max = 255, message = "경로 ID는 255자 이내여야 합니다.")
    private String pathId;

    @NotNull(message = "검색 사용 여부는 필수입니다.")
    private Boolean isUseSearch;

    @NotNull(message = "조회수 사용 여부는 필수입니다.")
    private Boolean isUseCount;

    private List<MenuRequest> children;
}
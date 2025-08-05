package kr.co.itid.cms.controller.cms.core.menu;

import kr.co.itid.cms.dto.cms.core.menu.request.MenuRequest;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuResponse;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuTreeLiteResponse;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuTreeResponse;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.cms.core.menu.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 메뉴 관련 API를 제공하는 컨트롤러입니다.
 * 드라이브 메뉴 전체 조회 및 특정 메뉴의 하위 메뉴를 조회할 수 있습니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/back-api/menu")
@Validated
public class MenuController {

    private final MenuService menuService;

    /**
     * 모든 드라이브 메뉴를 조회합니다.
     *
     * @return ApiResponse&lt;List&lt;MenuResponse&gt;&gt; 드라이브 메뉴 목록을 포함한 응답
     * @throws Exception 메뉴 조회 중 오류 발생 시
     */
    @GetMapping("/drive")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getRootMenus() throws Exception {
        List<MenuResponse> menus = menuService.getRootMenus();
        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    /**
     * 특정 이름을 가진 드라이브의 하위 메뉴를 가볍게 조회합니다.
     *
     * @param name 드라이브 또는 메뉴 이름 (필수, 공백 불가)
     * @return ApiResponse&lt;List&lt;MenuResponse&gt;&gt; 트리 형태의 하위 메뉴 가벼운 응답
     * @throws Exception 하위 메뉴 조회 중 오류 발생 시
     */
    @GetMapping("/{name}/lite")
    public ResponseEntity<ApiResponse<List<MenuTreeLiteResponse>>> getChildrenByName(
            @PathVariable @Pattern(regexp = "^[a-zA-Z0-9_-]{3,30}$") String name) throws Exception {

        List<MenuTreeLiteResponse> children = menuService.getMenuTreeLiteByName(name);
        return ResponseEntity.ok(ApiResponse.success(children));
    }

    /**
     * 특정 이름을 가진 드라이브의 드라이브 포함 하위 메뉴를 TREE 구성을 위해 전체 데이터를 조회합니다.
     *
     * @param name 드라이브 또는 메뉴 이름 (필수, 공백 불가)
     * @return ApiResponse&lt;List&lt;MenuTreeResponse&gt;&gt; 트리 형태의 하위 메뉴 응답
     * @throws Exception 트리 조회 중 오류 발생 시
     */
    @GetMapping("/{name}/tree")
    public ResponseEntity<ApiResponse<List<MenuTreeResponse>>> getChildrenTreeByName(
            @PathVariable @Pattern(regexp = "^[a-zA-Z0-9_-]{3,30}$") String name) throws Exception {

        List<MenuTreeResponse> children = menuService.getMenuTreeByName(name);
        return ResponseEntity.ok(ApiResponse.success(children));
    }
}
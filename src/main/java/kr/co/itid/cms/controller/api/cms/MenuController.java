package kr.co.itid.cms.controller.api.cms;

import kr.co.itid.cms.dto.cms.MenuResponse;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.cms.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 메뉴 관련 API를 제공하는 컨트롤러입니다.
 * 드라이브 메뉴 전체 조회 및 특정 메뉴의 하위 메뉴를 조회할 수 있습니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    /**
     * 모든 드라이브 메뉴를 조회합니다.
     *
     * @return ApiResponse&lt;List&lt;MenuResponse&gt;&gt; 드라이브 메뉴 목록을 포함한 응답
     * @throws Exception 메뉴 조회 중 오류 발생 시
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getAllDrives() throws Exception {
        List<MenuResponse> menus = menuService.getAllDrives();
        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    /**
     * 특정 이름을 가진 드라이브의 하위 메뉴를 조회합니다.
     *
     * @param name 드라이브 또는 메뉴 이름
     * @return ApiResponse&lt;List&lt;MenuResponse&gt;&gt; 하위 메뉴 목록을 포함한 응답
     * @throws Exception 하위 메뉴 조회 중 오류 발생 시
     */
    @GetMapping("/{name}")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getChildrenByName(@PathVariable String name) throws Exception {
        List<MenuResponse> children = menuService.getAllChildrenByName(name);
        return ResponseEntity.ok(ApiResponse.success(children));
    }
}

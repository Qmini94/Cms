package kr.co.itid.cms.controller.api.cms;

import kr.co.itid.cms.dto.cms.MenuResponse;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.cms.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getAllMenus() {
        List<MenuResponse> menus = menuService.getAllMenus();
        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> getMenuTitle(@PathVariable Long id) {
        String title = menuService.getMenuTitleById(id);
        return ResponseEntity.ok(ApiResponse.success(title));
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getChildren(@PathVariable Long id) {
        List<MenuResponse> children = menuService.getChildrenByParentId(id);
        return ResponseEntity.ok(ApiResponse.success(children));
    }
}

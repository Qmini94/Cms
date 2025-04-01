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

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getAllDrives() {
        List<MenuResponse> menus = menuService.getAllDrives();
        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    @GetMapping("/{name}")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getAllChildById(@PathVariable String name) {
        List<MenuResponse> children = menuService.getAllChildByName(name);
        return ResponseEntity.ok(ApiResponse.success(children));
    }
}

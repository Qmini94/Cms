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
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getAllDrives() {
        List<MenuResponse> menus = menuService.getAllDrives();
        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    @GetMapping("/{drive}")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getAllMenusByDrive(@PathVariable String drive) {
        List<MenuResponse> children = menuService.getAllMenusByDrive(drive);
        return ResponseEntity.ok(ApiResponse.success(children));
    }
}

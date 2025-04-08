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
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getAllDrives() throws Exception {
        List<MenuResponse> menus = menuService.getAllDrives();
        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    @GetMapping("/{name}")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getChildrenByName(@PathVariable String name) throws Exception {
        List<MenuResponse> children = menuService.getAllChildrenByName(name);
        return ResponseEntity.ok(ApiResponse.success(children));
    }
}

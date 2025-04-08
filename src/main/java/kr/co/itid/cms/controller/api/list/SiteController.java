package kr.co.itid.cms.controller.api.list;

import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.dto.list.SiteResponse;
import kr.co.itid.cms.service.list.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/list/site")
public class SiteController {

    private final SiteService siteService;

//    @PreAuthorize("@permService.hasAccess(authentication, #menuId, 'READ')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<SiteResponse>>> getSiteAllData(@RequestParam int menuId) throws Exception {
        List<SiteResponse> sites = siteService.getSiteAllData();
        return ResponseEntity.ok(ApiResponse.success(sites));
    }
}

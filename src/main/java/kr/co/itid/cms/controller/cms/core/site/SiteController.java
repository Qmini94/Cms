package kr.co.itid.cms.controller.cms.core.site;

import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.dto.cms.core.site.SiteResponse;
import kr.co.itid.cms.service.cms.core.site.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 사이트 정보 관련 API를 제공하는 컨트롤러입니다.
 * 모든 사이트 데이터를 조회할 수 있습니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/list/site")
@Validated
public class SiteController {

    private final SiteService siteService;

    /**
     * 모든 사이트 정보를 조회합니다.
     *
     * @return ApiResponse&lt;List&lt;SiteResponse&gt;&gt; 사이트 목록을 포함한 응답
     * @throws Exception 데이터 조회 중 오류 발생 시
     */
    @PreAuthorize("@permService.hasAccess('ACCESS')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SiteResponse>>> getSiteAllData() throws Exception {

        List<SiteResponse> sites = siteService.getSiteAllData();
        return ResponseEntity.ok(ApiResponse.success(sites));
    }
}

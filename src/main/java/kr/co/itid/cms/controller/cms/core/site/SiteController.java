package kr.co.itid.cms.controller.cms.core.site;

import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.dto.cms.core.site.SiteResponse;
import kr.co.itid.cms.service.cms.core.site.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사이트 정보 관련 API를 제공하는 컨트롤러입니다.
 * 모든 사이트 데이터를 조회할 수 있습니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/site")
@Validated
public class SiteController {

    private final SiteService siteService;

    /**
     * 모든 사이트 정보를 조회합니다.
     *
     * @return ApiResponse&lt;List&lt;SiteResponse&gt;&gt; 사이트 목록을 포함한 응답
     * @throws Exception 데이터 조회 중 오류 발생 시
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<SiteResponse>>> getSiteAllData() throws Exception {

        List<SiteResponse> sites = siteService.getSiteAllData();
        return ResponseEntity.ok(ApiResponse.success(sites));
    }

    /**
     * 특정 호스트명을 가진 사이트 정보를 수정합니다.
     *
     * @param siteHostName 수정할 사이트의 호스트명
     * @param request 수정할 사이트 정보
     * @return 수정된 사이트 응답
     */
    @PreAuthorize("@permService.hasAccess('MODIFY')")
    @PutMapping("/{siteHostName}")
    public ResponseEntity<ApiResponse<SiteResponse>> updateSiteByHostName(
            @PathVariable String siteHostName,
            @RequestBody @Validated SiteResponse request) throws Exception {

        SiteResponse updated = siteService.updateSiteByHostName(siteHostName, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }
}

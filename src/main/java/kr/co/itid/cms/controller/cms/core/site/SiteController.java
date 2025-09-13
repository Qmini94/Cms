package kr.co.itid.cms.controller.cms.core.site;

import kr.co.itid.cms.aop.ExecutionTime;
import kr.co.itid.cms.dto.cms.core.site.request.SiteRequest;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.dto.cms.core.site.response.SiteResponse;
import kr.co.itid.cms.service.cms.core.site.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 사이트 정보 관련 API를 제공하는 컨트롤러입니다.
 * 모든 사이트 데이터를 조회할 수 있습니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/back-api/site")
@Validated
public class SiteController {

    private final SiteService siteService;

    /**
     * 삭제되지 않은 사이트 목록 조회
     *
     * @return ApiResponse&lt;List&lt;SiteResponse&gt;&gt; 사이트 목록을 포함한 응답
     * @throws Exception 데이터 조회 중 오류 발생 시
     */
    @ExecutionTime(description = "활성 사이트 목록 조회", unit = ExecutionTime.TimeUnit.MILLISECONDS)
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<SiteResponse>>> getSiteyIsDeletedFalse() throws Exception {

        List<SiteResponse> sites = siteService.getSitesIsDeletedFalse();
        return ResponseEntity.ok(ApiResponse.success(sites));
    }

    /**
     * 전체 사이트 목록 조회 (삭제 포함)
     *
     * @return ApiResponse&lt;List&lt;SiteResponse&gt;&gt; 사이트 목록을 포함한 응답
     * @throws Exception 데이터 조회 중 오류 발생 시
     */
    @ExecutionTime(description = "전체 사이트 목록 조회", unit = ExecutionTime.TimeUnit.MILLISECONDS)
    @GetMapping("/list/all")
    public ResponseEntity<ApiResponse<List<SiteResponse>>> getSiteAllData() throws Exception {

        List<SiteResponse> sites = siteService.getSiteAllData();
        return ResponseEntity.ok(ApiResponse.success(sites));
    }

    /**
     * 사이트를 생성합니다.
     *
     * @param request 생성할 사이트 정보
     * @return 성공 여부 (true)
     */
    @ExecutionTime(description = "사이트 생성", unit = ExecutionTime.TimeUnit.MILLISECONDS, level = ExecutionTime.LogLevel.INFO)
    @PreAuthorize("@permService.hasAccess('MODIFY')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createSite(
            @RequestBody @Validated SiteRequest request) throws Exception {

        siteService.saveSite(null, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 사이트 정보를 수정합니다.
     *
     * @param siteHostName 수정할 사이트의 호스트명
     * @param request      수정할 사이트 정보
     * @return 성공 여부 (true)
     */
    @ExecutionTime(description = "사이트 수정", unit = ExecutionTime.TimeUnit.MILLISECONDS)
    @PreAuthorize("@permService.hasAccess('MODIFY')")
    @PutMapping("/{siteHostName}")
    public ResponseEntity<ApiResponse<Void>> updateSiteByHostName(
            @PathVariable @Pattern(regexp = "^[a-zA-Z0-9_-]{3,30}$") String siteHostName,
            @RequestBody @Validated SiteRequest request) throws Exception {

        siteService.saveSite(siteHostName, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 사이트를 소프트 복구합니다. (is_deleted = false)
     *
     * @param siteHostName 복구할 사이트의 호스트명
     * @return 성공 여부
     */
    @PreAuthorize("@permService.hasAccess('MODIFY')")
    @PutMapping("/{siteHostName}/restore")
    public ResponseEntity<ApiResponse<Void>> restoreSite(
            @PathVariable @Pattern(regexp = "^[a-zA-Z0-9_-]{3,30}$") String siteHostName) throws Exception {

        siteService.restoreSite(siteHostName);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 사이트를 소프트 삭제합니다. (is_deleted = true)
     *
     * @param siteHostName 삭제할 사이트의 호스트명
     * @return 성공 여부
     */
    @PreAuthorize("@permService.hasAccess('REMOVE')")
    @DeleteMapping("/{siteHostName}")
    public ResponseEntity<ApiResponse<Void>> softDeleteSite(
            @PathVariable @Pattern(regexp = "^[a-zA-Z0-9_-]{3,30}$") String siteHostName) throws Exception {

        siteService.softDeleteSite(siteHostName);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 사이트를 완전 삭제합니다. (DB에서 제거)
     *
     * @param siteHostName 삭제할 사이트의 호스트명
     * @return 성공 여부
     */
    @PreAuthorize("@permService.hasAccess('REMOVE')")
    @DeleteMapping("/{siteHostName}/hard")
    public ResponseEntity<ApiResponse<Void>> hardDeleteSite(
            @PathVariable @Pattern(regexp = "^[a-zA-Z0-9_-]{3,30}$") String siteHostName) throws Exception {

        siteService.hardDeleteSite(siteHostName);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

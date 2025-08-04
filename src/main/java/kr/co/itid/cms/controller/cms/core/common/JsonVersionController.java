package kr.co.itid.cms.controller.cms.core.common;

import kr.co.itid.cms.dto.cms.core.common.version.VersionListResponse;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.cms.core.common.JsonVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Pattern;

/**
 * JSON 버전 관리 API
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/back-api/json-version")
@Validated
public class JsonVersionController {

    private final JsonVersionService jsonVersionService;

    /**
     * 도메인 내 버전 목록 조회
     */
    @GetMapping("/{domain}/list")
    public ResponseEntity<ApiResponse<VersionListResponse>> getVersionFiles(
            @PathVariable
            @Pattern(regexp = "^[a-zA-Z0-9_-]{2,30}$", message = "유효하지 않은 도메인입니다.")
            String domain) throws Exception {
        VersionListResponse data = jsonVersionService.getVersionFiles(domain);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 버전 파일 내용 조회
     */
    @GetMapping("/{domain}/read")
    public ResponseEntity<ApiResponse<String>> readVersionFile(
            @PathVariable
            @Pattern(regexp = "^[a-zA-Z0-9_-]{2,30}$", message = "유효하지 않은 도메인입니다.")
            String domain,
            @RequestParam
            @Pattern(regexp = "^[a-zA-Z0-9._-]+\\.json$", message = "올바른 .json 파일명을 입력해주세요.")
            String fileName
    ) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(jsonVersionService.readJsonContent(domain, fileName)));
    }

    /**
     * 버전을 활성화(active.json 갱신)
     */
    @PreAuthorize("@permService.hasAccess('MODIFY')")
    @PostMapping("/{domain}/activate")
    public ResponseEntity<ApiResponse<Void>> activateVersion(
            @PathVariable
            @Pattern(regexp = "^[a-zA-Z0-9_-]{2,30}$", message = "유효하지 않은 도메인입니다.")
            String domain,
            @RequestParam
            @Pattern(regexp = "^[a-zA-Z0-9._-]+\\.json$", message = "올바른 .json 파일명을 입력해주세요.")
            String fileName
    ) throws Exception {
        jsonVersionService.activateVersion(domain, fileName);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 특정 버전 파일을 삭제합니다.
     */
    @PreAuthorize("@permService.hasAccess('REMOVE')")
    @DeleteMapping("/{domain}/delete")
    public ResponseEntity<ApiResponse<Void>> deleteVersionFile(
            @PathVariable
            @Pattern(regexp = "^[a-zA-Z0-9_-]{2,30}$", message = "유효하지 않은 도메인입니다.")
            String domain,
            @RequestParam
            @Pattern(regexp = "^[a-zA-Z0-9._-]+\\.json$", message = "올바른 .json 파일명을 입력해주세요.")
            String fileName
    ) throws Exception {
        jsonVersionService.deleteVersion(domain, fileName);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

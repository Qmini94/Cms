package kr.co.itid.cms.controller.cms.core.common;

import kr.co.itid.cms.dto.cms.core.common.version.VersionListResponse;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.cms.core.common.JsonVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * JSON 버전 관리 API
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/json-version")
public class JsonVersionController {

    private final JsonVersionService jsonVersionService;

    /**
     * 도메인 내 버전 목록 조회
     */
    @GetMapping("/{domain}/list")
    public ResponseEntity<ApiResponse<VersionListResponse>> getVersionFiles(@PathVariable String domain) throws Exception {
        VersionListResponse data = jsonVersionService.getVersionFiles(domain);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 현재 활성화된 버전 조회
     */
    @GetMapping("/{domain}/active")
    public ResponseEntity<ApiResponse<String>> getActiveVersion(@PathVariable String domain) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(jsonVersionService.getActiveFile(domain)));
    }

    /**
     * 버전 파일 내용 조회
     */
    @GetMapping("/{domain}/read")
    public ResponseEntity<ApiResponse<String>> readVersionFile(
            @PathVariable String domain,
            @RequestParam String fileName
    ) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(jsonVersionService.readJsonContent(domain, fileName)));
    }

    /**
     * 버전을 활성화(active.json 갱신)
     */
    @PreAuthorize("@permService.hasAccess('MODIFY')")
    @PostMapping("/{domain}/activate")
    public ResponseEntity<ApiResponse<Void>> activateVersion(
            @PathVariable String domain,
            @RequestParam String fileName
    ) throws Exception {
        jsonVersionService.activateVersion(domain, fileName);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 특정 버전 파일을 수정(교체)합니다.
     */
    @PreAuthorize("@permService.hasAccess('MODIFY')")
    @PutMapping("/{domain}/update")
    public ResponseEntity<ApiResponse<Void>> updateVersionFile(
            @PathVariable String domain,
            @RequestParam String fileName,
            @RequestBody String jsonData
    ) throws Exception {
        jsonVersionService.updateVersion(domain, fileName, jsonData);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 특정 버전 파일을 삭제합니다.
     */
    @PreAuthorize("@permService.hasAccess('REMOVE')")
    @DeleteMapping("/{domain}/delete")
    public ResponseEntity<ApiResponse<Void>> deleteVersionFile(
            @PathVariable String domain,
            @RequestParam String fileName
    ) throws Exception {
        jsonVersionService.deleteVersion(domain, fileName);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

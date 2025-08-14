package kr.co.itid.cms.controller.auth;

import kr.co.itid.cms.dto.auth.permission.request.PermissionSaveRequest;
import kr.co.itid.cms.dto.auth.permission.response.PermissionChainResponse;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.auth.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@RestController
@RequiredArgsConstructor
@RequestMapping("/back-api/permission")
@Validated
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 권한 체인(현재 + 상속) 조회
     * 예: GET /back-api/permission/chain?menuId=123&pathId=1.3.8
     */
    @PreAuthorize("@permService.hasAccess('MANAGE')")
    @GetMapping("/chain")
    public ResponseEntity<ApiResponse<PermissionChainResponse>> getPermissionChain(
            @RequestParam("menuId")
            @Positive(message = "menuId는 1 이상의 값이어야 합니다.") Long menuId,

            @RequestParam(value = "pathId", required = false)
            @Size(max = 255, message = "pathId 길이가 너무 깁니다.")
            @Pattern(regexp = "^[0-9]+(\\.[0-9]+)*$", message = "pathId 형식이 올바르지 않습니다.")
            String pathId
    ) {
        try {
            PermissionChainResponse data = permissionService.getPermissionChain(menuId, pathId);
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "권한 체인 조회 중 오류: " + e.getMessage()));
        }
    }

    /**
     * 현재 메뉴 권한 일괄 업서트(정렬 포함)
     * 예: PUT /back-api/permission/{menuId}/entries
     */
    @PreAuthorize("@permService.hasAccess('MANAGE')")
    @PutMapping("/{menuId}/entries")
    public ResponseEntity<ApiResponse<Void>> upsertEntries(
            @PathVariable("menuId")
            @Positive(message = "menuId는 1 이상의 값이어야 합니다.") Long menuId,

            @Valid @RequestBody PermissionSaveRequest request
    ) {
        try {
            if (request.getMenuId() != null && !request.getMenuId().equals(menuId)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(400, "menuId가 경로와 요청 본문에서 일치하지 않습니다."));
            }
            Long effectiveMenuId = request.getMenuId() != null ? request.getMenuId() : menuId;

            permissionService.upsertPermissions(effectiveMenuId, request.getEntries());
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "권한 저장 중 오류: " + e.getMessage()));
        }
    }
}
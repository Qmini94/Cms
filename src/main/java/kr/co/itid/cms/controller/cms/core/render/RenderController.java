package kr.co.itid.cms.controller.cms.core.render;

import kr.co.itid.cms.dto.cms.core.render.response.RenderResponse;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.cms.core.render.RenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 렌더링 컨트롤러
 *
 * 설계 원칙
 * - 컨트롤러는 파라미터 바인딩/검증 + 서비스 위임만 수행(얇게 유지)
 * - 로깅/예외 래핑/캐시 결정/보안 헤더(CSP, nonce)는 서비스/필터에서 처리
 * - JSON 응답은 ApiResponse 래핑, HTML 응답은 text/html로 직접 반환(프론트 parseAs:text 대응)
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/back-api/render")
@Validated
public class RenderController {

    private final RenderService renderService;

    /**
     * 현재 사용자 컨텍스트(JWT 내 menuId 등)에 기반한 렌더 데이터(JSON)를 반환합니다.
     * 권한 체크는 ACCESS 로 제한합니다.
     */
    @PreAuthorize("@permService.hasAccess('ACCESS')")
    @GetMapping
    public ResponseEntity<ApiResponse<RenderResponse>> render() throws Exception {
        RenderResponse response = renderService.getRenderData();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
package kr.co.itid.cms.controller.cms.core.render;

import kr.co.itid.cms.dto.cms.core.render.request.LayoutPreviewRequest;
import kr.co.itid.cms.dto.cms.core.render.request.LayoutSaveRequest;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.cms.core.page.LayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 레이아웃 관리 컨트롤러
 *
 * 설계 원칙
 * - 컨트롤러는 파라미터 바인딩/검증 + 서비스 위임만 수행(얇게 유지)
 * - 보안 헤더(CSP/nonce), 캐시 정책은 필터에서 처리 (ex. SecurityHeaderFilter/PreviewFilter)
 * - 미리보기는 text/html, 저장은 ApiResponse(JSON) 규약 준수
 */
@RestController
@RequestMapping("/back-api/layouts")
@RequiredArgsConstructor
@Validated
public class LayoutController {

    private final LayoutService layoutService;

    /**
     * 레이아웃 미리보기
     * - DB에 반영하지 않고, 요청 본문의 HTML/CSS/JS와 위젯 치환/자산 주입/Sanitizer를 거친
     *   완성 HTML을 반환한다.
     * - 보안 헤더/CSP/nonce/프레임 금지는 필터에서 적용한다.
     */
    @PreAuthorize("@permService.hasAccess('MODIFY')")
    @PostMapping(
            value = "/preview",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MimeTypeUtils.TEXT_HTML_VALUE
    )
    public ResponseEntity<String> preview(@RequestBody @Valid LayoutPreviewRequest req) throws Exception {
        String html = layoutService.renderPreview(req);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    /**
     * 레이아웃 저장/퍼블리시
     * - 신규/수정/퍼블리시 동작은 서비스에서 처리(검증/버전관리/권한/로그/예외 래핑)
     */
    @PreAuthorize("@permService.hasAccess('MODIFY')")
    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<Void>> save(@RequestBody @Valid LayoutSaveRequest req) throws Exception {
        layoutService.save(req);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
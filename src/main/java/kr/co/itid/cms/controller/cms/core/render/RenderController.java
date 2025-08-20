package kr.co.itid.cms.controller.cms.core.render;

import kr.co.itid.cms.dto.cms.core.render.response.RenderResponse;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.cms.core.render.RenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.Optional;

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

    /* =========================
       JSON: 권한 기반 렌더 데이터
       ========================= */

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

    /* =========================
       HTML: composed / shell
       ========================= */

    /**
     * 퍼블리시/버전/위젯치환/자산주입/Sanitizer 적용된 최종 HTML.
     *
     * 주의
     * - 공개 페이지 캐시 적중률을 위해 인증 영향이 없도록 permitAll 로 개방(개인화는 위젯/API로 분리)
     * - 보안 헤더(CSP/nonce)와 캐시정책은 SecurityHeaderFilter 등에서 처리
     * - 프론트는 safeFetch(parseAs:text)로 그대로 렌더
     *
     * @param site          사이트 식별자(호스트/코드 등)
     * @param path          페이지 경로(기본값 "/")
     * @param layoutVersion 특정 레이아웃 버전 지정(없으면 mode에 따라 선택)
     * @param mode          published | draft (기본: published)
     */
    @PreAuthorize("@permService.hasAccess('ACCESS')")
    @GetMapping(value = "/composed", produces = MimeTypeUtils.TEXT_HTML_VALUE)
    public ResponseEntity<String> composed(
            @RequestParam @NotBlank String site,
            @RequestParam(defaultValue = "/") String path,
            @RequestParam(required = false) Optional<Long> layoutVersion,
            @RequestParam(required = false, defaultValue = "published") String mode
    ) throws Exception {
        String html = renderService.composePage(site, path, layoutVersion.orElse(null), mode);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    /**
     * Vue Teleport용 최소 Shell HTML.
     *
     * 주의
     * - 거의 정적이므로 CDN/프록시에서 장기 캐시 권장(필터에서 헤더 적용)
     * - 개인화/동적 블록은 클라이언트 위젯(API)로 주입
     *
     * @param site 사이트 식별자
     * @param path 페이지 경로
     * @param mode published | draft (기본: published)
     */
    @PreAuthorize("@permService.hasAccess('ACCESS')")
    @GetMapping(value = "/shell", produces = MimeTypeUtils.TEXT_HTML_VALUE)
    public ResponseEntity<String> shell(
            @RequestParam @NotBlank String site,
            @RequestParam(defaultValue = "/") String path,
            @RequestParam(required = false, defaultValue = "published") String mode
    ) throws Exception {
        String html = renderService.buildShell(site, path, mode);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}
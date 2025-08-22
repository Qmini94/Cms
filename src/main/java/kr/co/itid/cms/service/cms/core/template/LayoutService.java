package kr.co.itid.cms.service.cms.core.template;

import kr.co.itid.cms.dto.cms.core.template.LayoutResolveResult;
import kr.co.itid.cms.dto.cms.core.template.request.LayoutPreviewRequest;
import kr.co.itid.cms.dto.cms.core.template.request.LayoutSaveRequest;
import kr.co.itid.cms.dto.cms.core.template.response.LayoutResponse;
import kr.co.itid.cms.enums.LayoutKind;

/**
 * 레이아웃 해석/미리보기/저장 서비스
 *
 * 책임 분리
 * - 컨트롤러: 파라미터 바인딩/검증 + 본 인터페이스 호출만 수행(얇게)
 * - 서비스 구현체: 레이아웃 선택, 템플릿 병합, 위젯 치환, 자산 주입, Sanitizer, 예외 래핑/로그, 캐시 적용 지점 관리
 * - 보안 헤더(CSP/nonce), Cache-Control 등은 공통 필터에서 처리
 */
public interface LayoutService {

    /**
     * (관리자 편집용) 레이아웃 조회
     * - 사이트 식별자(idx)와 레이아웃 종류(kind)로 순수 템플릿/메타 데이터를 반환한다.
     * - 컨트롤러 GET(/back-api/layouts)에서 사용.
     *
     * @param siteIdx 사이트 식별자(idx)
     * @param kind    레이아웃 종류(MAIN/SUB 등)
     * @return LayoutResponse
     * @throws Exception 조회 실패 시
     */
    LayoutResponse getTemplateBySiteIdxAndKind(Long siteIdx, LayoutKind kind) throws Exception;

    /**
     * 렌더링에 사용할 레이아웃을 결정하고, 템플릿/자산 정보로 구성된 결과를 반환한다.
     *
     * 파이프라인 선행 단계:
     *  - site/path 기반 레이아웃 kind(MAIN/SUB) 결정
     *  - layoutVersion 명시 시 해당 버전 우선, 없으면 mode(published|draft)에 따라 선택
     *
     * @param site          사이트 식별자(호스트/코드 등)
     * @param path          페이지 경로 (예: "/")
     * @param layoutVersion 특정 레이아웃 버전 강제 조회 (null이면 mode에 따라 선택)
     * @param mode          "published" | "draft"
     * @return LayoutResolveResult
     *         - htmlTemplate : 레이아웃 템플릿 HTML
     *         - cssUrls/jsUrls : head에 주입할 자산 URL 목록(없으면 빈 리스트)
     *         - kind/layoutId/version : 메타 정보
     * @throws Exception 조회/검증 실패 시
     */
    LayoutResolveResult resolveForRender(String site, String path, Long layoutVersion, String mode) throws Exception;

    /**
     * 레이아웃 미리보기 HTML을 생성한다.
     * <p>
     * DB 반영 없이 요청 본문(HTML/CSS/JS) + 위젯 치환 + 자산 주입 + Sanitizer를 적용한
     * 최종 HTML을 반환한다.
     * </p>
     * 주의:
     * - 응답 헤더(CSP/nonce/프레임 금지)는 미리보기 전용 필터에서 처리
     *
     * @param req LayoutPreviewRequest (요청 내 HTML/CSS/JS, 레이아웃 kind 등)
     * @return 최종 합성된 HTML(text/html)
     * @throws Exception 처리 중 오류 발생 시
     */
    String renderPreview(LayoutPreviewRequest req) throws Exception;

    /**
     * 레이아웃 저장/퍼블리시를 수행한다.
     * <p>
     * 유효성 검증, 버전 관리(working/published), 퍼블리시/언퍼블리시, 감사 로그를
     * 구현체에서 처리한다.
     * </p>
     *
     * @param req LayoutSaveRequest
     * @throws Exception 저장/퍼블리시 처리 중 오류 발생 시
     */
    void save(LayoutSaveRequest req) throws Exception;
}

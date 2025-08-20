package kr.co.itid.cms.service.cms.core.render;

import kr.co.itid.cms.dto.cms.core.render.response.RenderResponse;

/**
 * 렌더링 서비스 인터페이스
 *
 * 책임 분리
 * - 컨트롤러: 파라미터 바인딩/검증 + 본 인터페이스 호출만 수행
 * - 서비스(구현체): 레이아웃 해석 → 템플릿 병합 → 위젯 치환 → 자산 주입 → Sanitizer → (옵션)접근성 보정
 *                  + 로깅/예외 래핑(전자정부 스타일) + 캐시 적용 지점 관리
 * - 보안 헤더(CSP/nonce), Cache-Control/ETag 등은 공통 필터에서 처리
 *
 * 캐시 원칙
 * - composed(HTML): published 모드에서만 페이지 단위 캐시(버전/퍼블리시 해시 포함)
 * - 위젯: Global/Segment/User 단위로 Redis 조각 캐시(정책은 위젯 핸들러에서 결정)
 * - draft/preview: no-store
 */
public interface RenderService {

    /**
     * 현재 사용자 컨텍스트(JWT 내 menuId 등)에 기반한 렌더 데이터(JSON)를 반환합니다.
     * 컨트롤러의 권한 체크(@PreAuthorize)는 컨트롤러에서 수행하며,
     * 로깅/예외 래핑 및 DB 접근/도메인 조합은 구현체에서 처리합니다.
     *
     * @return RenderResponse 렌더링에 필요한 타입/옵션/권한 데이터를 포함한 응답 DTO
     * @throws Exception 조회/매핑 중 예외 발생 시
     */
    RenderResponse getRenderData() throws Exception;

    /**
     * 퍼블리시/버전/위젯치환/자산주입/Sanitizer까지 적용된 최종 HTML을 생성합니다.
     * 파이프라인(권장):
     * 1) 레이아웃 해석(site, path, layoutVersion, mode)
     * 2) 템플릿 병합(슬롯/섹션)
     * 3) 위젯 치환(WidgetService.render; 위젯별 캐시 정책 적용)
     * 4) head 자산 주입(CSS/JS; CSP는 필터가 처리)
     * 5) Sanitizer 및 (옵션) 접근성 보정
     *
     * 주의:
     * - 반환값은 text/html 본문이며, 보안 헤더/캐시 헤더는 필터에서 설정
     * - published 모드에서만 페이지 단위 캐시 적용(구현체에서 @Cacheable 등)
     *
     * @param site          사이트 식별자(호스트/코드 등)
     * @param path          페이지 경로(예: "/")
     * @param layoutVersion 특정 레이아웃 버전 강제 조회(Null이면 mode 기반 자동 선택)
     * @param mode          렌더 모드("published" | "draft")
     * @return 최종 합성된 HTML 문자열(text/html)
     * @throws Exception 합성 중 예외 발생 시
     */
    String composePage(String site, String path, Long layoutVersion, String mode) throws Exception;

    /**
     * Vue Teleport를 얹기 위한 최소 Shell HTML을 반환합니다.
     * - #app 및 텔레포트 타깃 컨테이너만 포함
     * - 개인화/동적 블록은 클라이언트 위젯(API)로 주입
     * - 셸은 정적에 가까우므로 장기 캐시 권장(필터에서 Cache-Control 설정)
     *
     * @param site 사이트 식별자
     * @param path 페이지 경로
     * @param mode 렌더 모드("published" | "draft")
     * @return Shell HTML(text/html)
     * @throws Exception 생성 중 예외 발생 시
     */
    String buildShell(String site, String path, String mode) throws Exception;
}

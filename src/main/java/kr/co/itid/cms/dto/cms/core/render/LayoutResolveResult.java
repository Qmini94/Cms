package kr.co.itid.cms.dto.cms.core.render;

import kr.co.itid.cms.enums.LayoutKind;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 레이아웃 해석 결과 DTO.
 * LayoutService.resolveForRender(...) 가 반환하며,
 * RenderServiceImpl.composePage(...) 파이프라인의 입력으로 사용된다.
 *
 * 필수:
 * - htmlTemplate : 레이아웃의 최종 템플릿 HTML (슬롯/섹션 표기 포함 가능)
 * - cssUrl       : head에 주입할 CSS 자산 URL(없으면 null)
 * - jsUrl        : head/body에 주입할 JS 자산 URL(없으면 null)
 * - kind         : 레이아웃 종류(MAIN/SUB 등)
 *
 * 선택(추적용):
 * - layoutId     : 레이아웃 식별자(DB 키)
 * - version      : 레이아웃 버전(퍼블리시/워킹 버전 추적)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LayoutResolveResult {

    /** 레이아웃 템플릿 HTML (raw, 아직 위젯 치환/자산 주입 전) */
    private String htmlTemplate;

    /** CSS 자산 URL (null 허용) */
    private List<String> cssUrl;

    /** JS 자산 URL (null 허용) */
    private List<String> jsUrl;

    /** 레이아웃 종류 (예: MAIN, SUB) */
    private LayoutKind kind;

    /** 레이아웃 PK (선택) */
    private Long layoutId;

    /** 레이아웃 버전 (선택) */
    private int version;
}

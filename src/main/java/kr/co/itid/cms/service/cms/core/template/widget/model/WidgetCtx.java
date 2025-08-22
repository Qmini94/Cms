package kr.co.itid.cms.service.cms.core.template.widget.model;

import kr.co.itid.cms.enums.LayoutKind;
import lombok.Builder;
import lombok.Getter;

/**
 * 위젯 렌더링 컨텍스트
 * - 사이트/경로/레이아웃 종류/모드 등 런타임 정보를 담는다.
 * - 개인화가 필요하면 user/locale 같은 필드를 확장한다.
 */
@Getter
@Builder
public class WidgetCtx {

    /** 사이트 식별자 (hostName 또는 siteCode 등) */
    private final String siteIdent;

    /** 요청 경로 ("/", "/notice/list" 등) */
    private final String path;

    /** 레이아웃 종류 (MAIN | SUB) */
    private final LayoutKind kind;

    /** 렌더 모드 ("preview" | "published" | "draft" 등) */
    private final String mode;

    // ▼ 필요 시 확장 (인증/권한/로케일 등)
    // private final JwtAuthenticatedUser user;
    // private final Locale locale;
}
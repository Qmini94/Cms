package kr.co.itid.cms.util;

import kr.co.itid.cms.enums.LayoutKind;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.Map;

/**
 * 위젯 치환 컨텍스트
 * - compose 파이프라인에서 템플릿 병합/위젯치환 시 공통으로 사용하는 런타임 정보.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WidgetCtx {

    /** 사이트 식별자(호스트/코드 등) */
    private String site;

    /** 요청 경로 (예: "/") */
    private String path;

    /** 레이아웃 종류 (MAIN / SUB) */
    private LayoutKind kind;

    /** 사용자/권한 컨텍스트 (익명일 수 있음) */
    private Long userId;            // null 가능
    private int userLevel;        // null 가능
    private boolean anonymous;      // true면 비로그인

    /** 로케일/시간대/요청시각 */
    private Locale locale;          // null 가능
    private String timeZone;        // e.g., "Asia/Seoul"
    private ZonedDateTime now;

    /** 기타 파라미터(메뉴/카테고리/쿼리파라미터 등 자유롭게 확장) */
    private Map<String, Object> vars;
}

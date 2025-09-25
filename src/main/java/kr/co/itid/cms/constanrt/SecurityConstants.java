package kr.co.itid.cms.constanrt;

public class SecurityConstants {
    private SecurityConstants() {
        throw new UnsupportedOperationException("유틸리티 클래스이므로 인스턴스화할 수 없습니다");
    }
    // 쿠키 관련 상수: SameSite 속성 값을 지정하여 쿠키의 보안 정책 설정
    public static final String SAME_SITE_STRICT = "Strict";  // SameSite 속성: Strict 모드 (다른 도메인으로 전송 불가)
    public static final String SAME_SITE_LAX = "Lax";        // SameSite 속성: Lax 모드 (GET 요청에 한해 전송 허용)
    public static final String SAME_SITE_NONE = "None";      // SameSite 속성: None 모드 (타 도메인 전송 허용, secure와 함께 사용해야 함)

    // 쿠키 보안 속성
    public static final boolean HTTP_ONLY = true;  // HTTP 전용: 클라이언트 스크립트 접근 불가
    public static final boolean SECURE = true;    // HTTPS 전용: HTTPS를 통해서만 전송 (개발 환경에서는 false, 운영 환경에서는 true로 설정)

    // 쿠키 이름 설정: 클라이언트에서 사용할 토큰 쿠키 이름
    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";   // 액세스 토큰 쿠키 이름
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    public static final String SESSION_EXPIRES_COOKIE_NAME = "sessionExpires";

    // 헤더 이름
    public static final String X_SESSION_EXPIRES_HEADER = "X-Session-Expires";

    // 쿠키 공통 속성
    public static final String COOKIE_PATH = "/";

    // 필요 시 도메인 고정이 있다면 여기에 정의(없으면 null 유지)
    public static final String COOKIE_DOMAIN = null; // 예: "itid.co.kr"
}

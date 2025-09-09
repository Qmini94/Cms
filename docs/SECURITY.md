
# 🔐 EGOVFrame CMS 보안 시스템

## 📋 목차
1. [인증 시스템 개요](#인증-시스템-개요)
2. [JWT 기반 인증](#jwt-기반-인증)
3. [세션 관리](#세션-관리)
4. [권한 관리](#권한-관리)
5. [보안 메커니즘](#보안-메커니즘)
6. [보안 유틸리티](#보안-유틸리티)
7. [개발 환경 보안](#개발-환경-보안)
8. [보안 모범 사례](#보안-모범-사례)

---

## 인증 시스템 개요

### 기본 정보
- **인증 방식**: JWT (JSON Web Token) 기반 Stateless 인증
- **세션 관리**: 슬라이딩 세션 (Sliding Session) 패턴
- **토큰 저장**: HttpOnly 쿠키 + Redis (블랙리스트만)
- **토큰 만료 시간**: 1시간 (3600초)

### 보안 아키텍처
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Backend       │    │   Storage       │
│                 │    │                 │    │                 │
│ • HttpOnly      │◄──►│ • JWT Provider  │◄──►│ • MySQL DB      │
│   Cookie        │    │ • Auth Filter   │    │ • Redis Cache   │
│ • XSS 방지       │    │ • Auth Service  │    │   (블랙리스트)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

---

## JWT 기반 인증

### 토큰 구조
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user123",           // 사용자 ID
    "userLevel": 1,             // 사용자 레벨
    "userName": "홍길동",        // 사용자 이름
    "idx": 123,                 // 사용자 인덱스
    "jti": "uuid-1234",         // JWT ID (블랙리스트용)
    "iat": 1640995200,          // 발급 시간
    "exp": 1640998800           // 만료 시간 (1시간 후)
  }
}
```

### 토큰 생성 과정
- **사용자 정보 포함**: userId, userLevel, userName, idx
- **고유 식별자**: JTI (JSON Token Identifier) 생성
- **만료 시간**: 현재 시간 + 1시간
- **서명**: HMAC SHA256 알고리즘 사용

### 토큰 저장 방식
- **HttpOnly 쿠키**: XSS 공격 방지
- **Secure 플래그**: HTTPS 전용 전송
- **SameSite**: 크로스 도메인 요청 제어
- **Path**: 전체 경로 적용

### 토큰 검증 과정
1. **블랙리스트 확인**: Redis에서 JTI 검증
2. **서명 검증**: HMAC SHA256 서명 확인
3. **만료 시간 확인**: 토큰 만료 여부 검사
4. **클럭 스큐 허용**: 60초 오차 허용

---

## 세션 관리

### 슬라이딩 세션 (Sliding Session) 패턴

#### 동작 원리
- **매 요청마다 토큰 갱신**: 사용자 활동 시마다 토큰 자동 갱신
- **자동 만료 연장**: 사용자가 활성 상태일 때 세션 자동 연장
- **사용자 경험 향상**: 갑작스러운 로그아웃 방지

#### 토큰 갱신 과정
```java
// AuthInterceptor에서 매 요청마다 실행
public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
    if (user.isGuest() || user.isDev()) return true;
    
    // 슬라이딩 세션 처리
    jwtTokenProvider.refreshIfNeeded(user);
    return true;
}
```

### 블랙리스트 관리
- **로그아웃 시**: JTI를 Redis 블랙리스트에 추가
- **TTL 설정**: 토큰 만료 시간만큼 블랙리스트 유지
- **즉시 무효화**: 보안 침해 시 토큰 즉시 무효화

### 전통적인 세션과의 차이점

| 구분 | 전통적인 DB 세션 | 현재 JWT 시스템 |
|------|-----------------|----------------|
| **상태 관리** | Stateful (서버에 세션 저장) | Stateless (서버에 상태 저장 안함) |
| **확장성** | 서버 확장 시 세션 동기화 필요 | 서버 확장 자유로움 |
| **성능** | 매 요청마다 DB 조회 | 토큰 검증만 수행 |
| **보안** | 세션ID 탈취 시 위험 | JWT 서명 검증으로 보안 강화 |

---

## 권한 관리

### 메서드 레벨 권한 체크
- **@PreAuthorize**: 총 47개 메서드에 적용
- **SpEL 표현식**: 복잡한 권한 조건 표현
- **실시간 권한 검증**: 매 요청마다 권한 확인

### PermissionService
- **세밀한 권한 제어**: 메뉴별, 기능별 권한 분리
- **계층형 권한**: 상위 권한이 하위 권한 포함
- **동적 권한 관리**: 런타임 권한 변경 지원

### 권한 체계
```
관리자 (Level 1)
├── 메뉴 관리 권한
├── 사용자 관리 권한
└── 시스템 설정 권한

일반 사용자 (Level 11)
├── 게시판 읽기 권한
├── 댓글 작성 권한
└── 프로필 수정 권한
```

### IP 기반 접근 제어
- **사이트별 IP 제한**: X-Site-Hostname 헤더 기반
- **차단된 IP 감지**: 403 Forbidden 응답
- **IP 정규화**: IPv6 → IPv4 변환 처리

---

## 보안 메커니즘

### 1. XSS 방지
- **HttpOnly 쿠키**: 클라이언트 스크립트 접근 방지
- **HtmlSanitizerUtil**: HTML 태그 필터링
- **입력값 검증**: ValidationUtil로 입력값 검증

### 2. CSRF 방지
- **SameSite 쿠키**: 크로스 사이트 요청 제어
- **토큰 기반 인증**: CSRF 토큰 불필요
- **Origin 헤더 검증**: 요청 출처 확인

### 3. 토큰 탈취 방지
- **서명 검증**: 토큰 위조 불가능
- **블랙리스트**: 무효화된 토큰 즉시 차단
- **만료 시간**: 짧은 토큰 수명으로 위험 최소화

### 4. 보안 헤더 설정
```java
// 쿠키 보안 설정
ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, token)
    .httpOnly(true)                    // XSS 방지
    .secure(true)                      // HTTPS 전용
    .path("/")                         // 전체 경로 적용
    .maxAge(Duration.ofSeconds(3600))  // 1시간
    .sameSite("None")                  // 크로스 도메인 허용
    .build();
```

---

## 보안 유틸리티

### HtmlSanitizerUtil
- **XSS 방지**: HTML 태그 및 스크립트 필터링
- **AOP 적용**: @HtmlSanitizer 어노테이션으로 자동 처리
- **화이트리스트 방식**: 허용된 태그만 통과

### CryptoUtil
- **데이터 암호화**: 민감한 데이터 암호화
- **대칭키 암호화**: AES 알고리즘 사용
- **해시 함수**: 비밀번호 해싱

### ValidationUtil
- **입력값 검증**: 사용자 입력 데이터 검증
- **SQL 인젝션 방지**: 파라미터화된 쿼리 사용
- **데이터 타입 검증**: 타입 안전성 보장

### IpUtil
- **IP 주소 관리**: 클라이언트 IP 추출 및 정규화
- **IPv6 지원**: IPv6 주소를 IPv4로 변환
- **프록시 처리**: X-Forwarded-For 헤더 처리

---

## 개발 환경 보안

### 로컬 개발환경 자동 인증
```java
// https://localhost:3000 접근 시 자동 관리자 권한 부여
if ("https://localhost:3000".equalsIgnoreCase(origin)) {
    return new JwtAuthenticatedUser(
        0L, "DEV_ADMIN", "개발관리자", 1, exp, "dev-token", hostname, menuId
    );
}
```

### 개발 환경 특징
- **자동 관리자 권한**: 로컬 개발 시 권한 문제 해결
- **게스트 사용자**: 토큰 없는 경우 게스트로 처리
- **디버그 로깅**: 상세한 인증 과정 로그 출력

### 보안 고려사항
- **개발/운영 환경 분리**: 환경별 보안 설정 차별화
- **민감 정보 보호**: 개발 환경에서도 실제 비밀번호 사용 금지
- **로그 보안**: 개발 환경에서도 민감한 정보 로깅 주의

---

## 보안 모범 사례

### 1. 토큰 관리
- **짧은 만료 시간**: 1시간 (3600초)
- **자동 갱신**: 사용자 활동 시 토큰 갱신
- **즉시 무효화**: 로그아웃 시 블랙리스트 추가

### 2. 쿠키 보안
- **HttpOnly**: 클라이언트 스크립트 접근 방지
- **Secure**: HTTPS 전용 전송
- **SameSite**: 크로스 도메인 요청 제어

### 3. 입력값 검증
- **서버 사이드 검증**: 클라이언트 검증과 별도로 서버에서 검증
- **화이트리스트**: 허용된 값만 통과
- **이스케이프 처리**: 특수문자 이스케이프

### 4. 로깅 및 모니터링
- **보안 이벤트 로깅**: 인증 실패, 권한 오류 등
- **민감 정보 마스킹**: 로그에서 비밀번호, 토큰 등 마스킹
- **실시간 모니터링**: 비정상적인 접근 패턴 감지

### 5. 운영 환경 보안 설정
```yaml
# 운영 환경 권장 설정
jwt:
  secret: ${JWT_SECRET}  # 환경변수로 관리
  access-token-validity: 1800  # 30분으로 단축 고려

security:
  cookie:
    secure: true
    same-site: "Strict"  # 운영환경에서는 Strict 권장
```

---

## 보안 체크리스트

### 인증 보안
- [x] JWT 토큰 기반 인증
- [x] HttpOnly 쿠키 사용
- [x] 토큰 서명 검증
- [x] 블랙리스트 관리
- [x] IP 기반 접근 제어

### 권한 관리
- [x] 메서드 레벨 권한 체크
- [x] 계층형 권한 구조
- [x] 동적 권한 관리
- [x] 세밀한 권한 제어

### 데이터 보안
- [x] XSS 방지
- [x] SQL 인젝션 방지
- [x] 입력값 검증
- [x] 데이터 암호화

### 운영 보안
- [x] 보안 로깅
- [x] 환경별 설정 분리
- [x] 민감 정보 보호
- [x] 모니터링 체계

---
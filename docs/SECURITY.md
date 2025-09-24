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
9. [보안 체크리스트](#보안-체크리스트)

---

## 인증 시스템 개요

### 기본 정보
- **인증 방식**: JWT + Redis 세션 기반 인증
- **세션 관리**: 슬라이딩 세션 (Sliding Session) 패턴
- **토큰 저장**: HttpOnly 쿠키 (ACCESS / REFRESH)
- **토큰 만료 시간**
    - ACCESS: 약 15분
    - REFRESH: 약 1일
    - Redis 장애 시 Fallback ACCESS: 더 긴 TTL

### 보안 아키텍처
```
┌─────────────────┐ ┌──────────────────┐ ┌─────────────────┐
│     Frontend    │ │     Backend      │ │      Storage    │
│                 │ │                  │ │                 │
│ • HttpOnly     │◄─►│ • JWT Provider │◄─►│ • Redis        │
│ • Cookie        │ │ • Auth Filter    │ │  • MySQL DB     │
│ • XSRF-TOKEN    │ │ • Auth Service   │ │                 │
└─────────────────┘ └──────────────────┘ └─────────────────┘
```

---

## JWT 기반 인증

### 토큰 구조
```json
{
  "sub": "user123",         // 사용자 ID
  "userLevel": 1,
  "userName": "홍길동",
  "idx": 123,
  "sid": "session-uuid",    // Redis 세션 ID
  "jti": "uuid-1234",       // JWT ID
  "iat": 1640995200,        // 발급 시간
  "exp": 1640996100         // 만료 시간 (ACCESS: ~15분)
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

# 인증 및 보안 시스템

## 토큰 생성

### ACCESS 토큰
* 사용자 정보(userId, userLevel, userName, idx, sid) 포함
* TTL ≈ 15분 (Redis 정상 시)
* Redis 장애 시 fallback TTL 사용

### REFRESH 토큰
* 최소 정보(sid) 포함
* TTL ≈ 1일

### 서명 알고리즘
* HMAC SHA256 (HS256)

## 쿠키 저장 정책

### ACCESS 쿠키
* HttpOnly, Secure(운영), Path=/, SameSite=None
* TTL ≈ 15분

### REFRESH 쿠키
* HttpOnly, Secure(운영), Path=/, SameSite=Strict
* TTL ≈ 1일

### 개발 환경
* HTTPS 미사용 시 Secure=false 허용

## 토큰 검증

1. 서명 검증 (HS256)
2. 만료 시간 확인 (60초 clock skew 허용)
3. `sid`로 Redis 세션 존재 여부 확인
4. 세션 존재 시 TTL 슬라이딩 (touch)

## 세션 관리

### Redis 기반 세션
* 세션 생성 시: UUID 기반 sid 발급, 사용자 정보 저장
* TTL: 기본 15분, 요청 시 touch로 연장
* 삭제: 로그아웃 시 `deleteSession(sid)`

## 장애 대응

### Redis 정상
* ACCESS + 세션 검증 필수
* 만료 시 REFRESH + 세션 검증 후 ACCESS 재발급

### Redis 장애
* 로그인 시: fallback TTL ACCESS 발급
* 요청 시:
    * ACCESS 유효 → 서명만 검증, 세션 검증 스킵
    * ACCESS 만료/부재 → 재발급 불가 (401 또는 게스트 처리)
* 복구 시: 정상 정책으로 자동 전환

## 권한 관리

### 메서드 레벨 권한 체크
* **@PreAuthorize** 기반 메서드 단위 권한 제어
* SpEL 표현식으로 동적 권한 조건 처리

### PermissionService
* 메뉴별/기능별 세밀한 권한 제어
* 계층형 권한 구조 지원
* 런타임 시 권한 변경 반영 가능

## 보안 메커니즘

### 1. XSS 방지
* HttpOnly 쿠키로 클라이언트 접근 차단
* HtmlSanitizerUtil 적용
* ValidationUtil 통한 입력값 검증

### 2. CSRF 방지
* Spring Security `CookieCsrfTokenRepository.withHttpOnlyFalse()` 사용
* 프론트: `XSRF-TOKEN` 쿠키 읽어 `X-XSRF-TOKEN` 헤더로 전송
* `/back-api/auth/login` 요청만 예외 처리
* 그 외 POST/PUT/PATCH/DELETE 요청에 헤더 없으면 403

### 3. 토큰 탈취 방지
* HS256 서명 검증
* 세션 검증으로 즉시 무효화 가능
* 블랙리스트 미사용 (세션 철회로 대체)

### 4. 보안 헤더 설정 예시

```java
ResponseCookie cookie = ResponseCookie.from("ACCESS", token)
    .httpOnly(true)
    .secure(true)
    .path("/")
    .maxAge(Duration.ofMinutes(15))
    .sameSite("None")
    .build();
```

## 보안 유틸리티

### HtmlSanitizerUtil
* **XSS 방지**: HTML 태그 및 스크립트 필터링
* **AOP 적용**: @HtmlSanitizer 어노테이션으로 자동 처리
* **화이트리스트 방식**: 허용된 태그만 통과

### CryptoUtil
* **AES 암호화**: 대칭키 기반 데이터 암호화/복호화
* **해시 함수**: SHA-256, bcrypt 지원
* **솔트 적용**: 비밀번호 해싱 시 랜덤 솔트 사용

### ValidationUtil
* **SQL 인젝션 방지**: 입력값 패턴 검증
* **데이터 타입 검증**: 형식 및 범위 체크
* **특수문자 필터링**: 위험한 문자 이스케이프

### IpUtil
* **IPv6 정규화**: IPv6 주소 표준화
* **프록시 처리**: X-Forwarded-For 헤더 파싱
* **IP 대역 검증**: 허용/차단 IP 범위 체크

## 개발 환경 보안

### 로컬 개발환경 자동 인증

```java
if ("https://localhost:3000".equalsIgnoreCase(origin)) {
    return new JwtAuthenticatedUser(
        0L, "DEV_ADMIN", "개발관리자", 1, exp, "dev-token", hostname, menuId
    );
}
```

### 특징
* 로컬 개발 시 자동 관리자 권한 부여
* 토큰 없을 경우 게스트 처리
* 상세한 인증 과정 로그 출력

## 보안 모범 사례

### 토큰 관리
* ACCESS TTL: 15분
* REFRESH TTL: 1일
* 즉시 철회는 세션 삭제로 처리

### 쿠키 보안
* HttpOnly, Secure, Path=/ 적용
* 운영 환경: REFRESH SameSite=Strict 권장
* 로컬: HTTPS 미사용 시 Secure=false

### 입력값 검증
* 서버 단 Validation 필수
* 화이트리스트 기반 검증
* 특수문자 이스케이프 처리

### 로깅/모니터링
* 인증 실패, 권한 오류 로깅
* 민감 정보 마스킹
* 비정상 접근 패턴 탐지

## 보안 체크리스트

### 인증 보안
* JWT 쿠키 기반 인증
* Redis 세션 검증
* CSRF 활성화
* Secure/HttpOnly 쿠키

### 권한 관리
* 메서드 단위 권한 검증
* 메뉴별/기능별 권한 제어
* 런타임 권한 변경 반영

### 데이터 보안
* XSS 필터링
* SQL 인젝션 방지
* AES 암호화
* 입력값 Validation

### 운영 보안
* 환경별 보안 설정 분리
* 보안 로그 관리
* 민감 정보 보호
* 실시간 모니터링
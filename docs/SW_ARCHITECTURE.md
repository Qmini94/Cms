
## 소프트웨어 아키텍처

### 레이어드 아키텍처 구조

```
┌─────────────────────────────────────┐
│           Frontend Layer            │ (React/Vue/Angular)
└─────────────────┬───────────────────┘
                  │ HTTP/REST API
┌─────────────────▼───────────────────┐
│            API Gateway              │ (/back-api/*)
└─────────────────┬───────────────────┘
                  │
┌─────────────────▼───────────────────┐
│           Security Layer            │ (JWT + @PreAuthorize)
├─────────────────┬───────────────────┤
│         Controller Layer            │ (REST Controllers)
├─────────────────┬───────────────────┤
│           Service Layer             │ (Business Logic)
├─────────────────┬───────────────────┤
│         Repository Layer            │ (JPA + QueryDSL)
├─────────────────┬───────────────────┤
│          Database Layer             │ (MySQL + Redis)
└─────────────────────────────────────┘
```

### 패키지 구조
```
kr.co.itid.cms/
├── config/           # 설정 클래스들
│   ├── common/       # 공통 설정 (Web, Redis, QueryDSL)
│   ├── egov/         # 전자정부프레임워크 설정
│   ├── security/     # 보안 설정 (JWT, Security)
│   └── exception/    # 예외 처리
├── controller/       # REST API 컨트롤러
│   ├── auth/         # 인증 관련
│   └── cms/core/     # CMS 핵심 기능
├── service/          # 비즈니스 로직
├── repository/       # 데이터 접근 계층
├── entity/           # JPA 엔티티
├── dto/              # 데이터 전송 객체
├── mapper/           # MapStruct 매퍼
├── util/             # 유틸리티 클래스
└── enums/            # 열거형 상수
```

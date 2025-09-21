# 전자정부프레임워크 CMS Backend
전자정부프레임워크 기반의 콘텐츠 관리 시스템(CMS) 백엔드 애플리케이션입니다.

## 시스템 개요

### 주요 기능
- **사이트 관리**: 다중 사이트 환경 지원 및 사이트별 설정 관리
- **콘텐츠 관리**: 계층형 콘텐츠 구조 및 그룹 관리
- **메뉴 관리**: 동적 메뉴 구조 및 권한 기반 접근 제어
- **게시판 관리**: 동적 게시판 생성 및 관리
- **사용자 인증**: JWT 기반 인증 및 권한 관리
- **파일 관리**: 콘텐츠 첨부 파일 업로드 및 관리
- **렌더링**: JSON 기반 동적 페이지 렌더링

### 기술 스택
- **Framework**: Spring Boot 2.7.12, 전자정부프레임워크 4.2.0
- **Language**: Java 17
- **Database**: MySQL 8.0
- **Cache**: Redis
- **Security**: Spring Security + JWT
- **Build Tool**: Gradle
- **Documentation**: Swagger/OpenAPI 3
- **Query**: QueryDSL, JPA
- **Mapping**: MapStruct
- **HTML Sanitization**: OWASP Java HTML Sanitizer

## 빌드 방법

### 사전 요구사항
- Java 17 이상
- MySQL 8.0 이상
- Redis 6.0 이상
- Gradle 7.0 이상 (또는 Gradle Wrapper 사용)

### 빌드 및 실행

#### 1. 의존성 설치
```bash
./gradlew build
```

#### 2. QueryDSL Q클래스 생성
```bash
./gradlew generateQueryDSL
```

#### 3. 애플리케이션 실행
```bash
./gradlew bootRun
```

#### 4. JAR 파일 빌드
```bash
./gradlew bootJar
```

### 주요 Gradle 태스크
- `./gradlew build`: 전체 빌드 (테스트 포함)
- `./gradlew bootRun`: 개발 서버 실행
- `./gradlew bootJar`: 실행 가능한 JAR 파일 생성
- `./gradlew generateQueryDSL`: QueryDSL Q클래스 생성
- `./gradlew clean`: 빌드 파일 정리
- `./gradlew test`: 단위 테스트 실행

## 🔧 주요 사용법

### API 엔드포인트

#### 인증 API (`/back-api/auth`)
- `POST /back-api/auth/login`: 사용자 로그인
- `GET /back-api/auth/me`: 현재 사용자 정보 조회
- `DELETE /back-api/auth/logout`: 로그아웃

#### 사이트 관리 API (`/back-api/site`)
- `GET /back-api/site/list`: 활성 사이트 목록 조회
- `GET /back-api/site/list/all`: 전체 사이트 목록 조회
- `POST /back-api/site`: 사이트 생성
- `PUT /back-api/site/{hostname}`: 사이트 정보 수정

#### 콘텐츠 관리 API (`/back-api/content`)
- `GET /back-api/content`: 콘텐츠 목록 조회 (페이징, 검색)
- `GET /back-api/content/group/{idx}`: 그룹별 콘텐츠 조회
- `GET /back-api/content/{idx}`: 콘텐츠 상세 조회
- `POST /back-api/content`: 콘텐츠 생성
- `PUT /back-api/content/{idx}`: 콘텐츠 수정

#### 메뉴 관리 API (`/back-api/menu`)
- `GET /back-api/menu`: 메뉴 목록 조회
- `POST /back-api/menu`: 메뉴 생성
- `PUT /back-api/menu/{id}`: 메뉴 수정

#### 게시판 관리 API (`/back-api/boardMaster`)
- `GET /back-api/boardMaster`: 게시판 마스터 목록 조회
- `POST /back-api/boardMaster`: 게시판 생성

#### 렌더링 API (`/back-api/render`)
- `GET /back-api/render`: 현재 사용자 컨텍스트 기반 렌더 데이터 조회

### 보안 및 권한

#### JWT 토큰 기반 인증
- Access Token: 15분 유효 (Redis 기반 관리)
- HttpOnly 쿠키로 토큰 전송
- Redis 장애 시 Fallback 토큰 (1시간 유효)

#### 권한 레벨
- `ACCESS`: 읽기 권한
- `MODIFY`: 수정 권한  
- `MANAGE`: 관리 권한

#### 보안 기능
- XSS 방어 필터
- CSRF 보호
- IP 기반 접근 제어
- HTML Sanitization

### 설정 파일

#### application.yml 주요 설정
```yaml
# 데이터베이스 설정
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/EGOV_CSERVER
    username: itid
    password: Yubi2025!

# Redis 설정
  redis:
    host: 127.0.0.1
    port: 6379
    password: Yubi!!@@##5630

# JWT 설정
jwt:
  secret: your-super-secret-key-32bytes-or-more
  access-token-validity: 900    # 15분
  fallback-token-validity: 3600 # 1시간

# Swagger 설정
springdoc:
  swagger-ui:
    path: /swagger-ui
```

### 개발 환경 설정

#### 1. 데이터베이스 설정
- MySQL 데이터베이스 생성: `EGOV_CSERVER`
- 사용자 생성 및 권한 부여

#### 2. Redis 설정
- Redis 서버 실행
- 인증 정보 설정

#### 3. 애플리케이션 실행
```bash
./gradlew bootRun
```

#### 4. API 문서 확인
- Swagger UI: `http://localhost:8080/swagger-ui`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### 파일 구조
```
src/main/java/kr/co/itid/cms/
├── controller/          # REST API 컨트롤러
│   ├── auth/           # 인증 관련 API
│   └── cms/            # CMS 핵심 기능 API
├── service/            # 비즈니스 로직
├── repository/         # 데이터 접근 계층
├── entity/             # JPA 엔티티
├── dto/                # 데이터 전송 객체
├── config/             # 설정 클래스
│   ├── security/       # 보안 설정
│   └── common/         # 공통 설정
└── util/               # 유틸리티 클래스
```

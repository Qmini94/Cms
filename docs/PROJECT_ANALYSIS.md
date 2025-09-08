# 전자정부프레임워크 CMS 백엔드 프로젝트 분석

##  프로젝트 개요

**프로젝트명**: 전자정부프레임워크 CMS 백엔드  
**버전**: 1.0.0-SNAPSHOT  
**Java 버전**: 17  
**Spring Boot 버전**: 2.7.12  
**빌드 도구**: Gradle  

이 프로젝트는 전자정부프레임워크를 기반으로 한 CMS(Content Management System) 백엔드 API 서버입니다.

##  기술 스택 및 주요 라이브러리

### 핵심 프레임워크
- **Spring Boot 2.7.12**: 메인 애플리케이션 프레임워크
- **전자정부프레임워크 4.2.0**: 
  - `org.egovframe.rte.ptl.mvc`: MVC 패턴 지원
  - `org.egovframe.rte.fdl.property`: 설정 관리

### 데이터베이스 & 영속성
- **Spring Data JPA**: ORM 프레임워크
- **QueryDSL 4.4.0**: 타입 세이프 쿼리 빌더
- **MySQL 8.0.33**: 메인 데이터베이스
- **Redis**: 캐시 및 세션 관리
- **HikariCP**: 커넥션 풀링

### 보안 & 인증
- **Spring Security**: 보안 프레임워크
- **JWT (JJWT 0.11.5)**: 토큰 기반 인증
- **BCrypt**: 패스워드 암호화

### API 문서화
- **SpringDoc OpenAPI 1.7.0**: Swagger UI 지원

### 유틸리티 & 도구
- **MapStruct 1.5.5**: 객체 매핑
- **Lombok**: 보일러플레이트 코드 제거
- **OWASP Java HTML Sanitizer**: HTML 보안 처리
- **JSoup 1.18.1**: HTML 파싱

### 개발 도구
- **Spring Boot DevTools**: 개발 편의성
- **Logback**: 로깅 프레임워크

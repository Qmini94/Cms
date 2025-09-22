## 🔍 주요 기능 모듈

### 1. 인증/인가 모듈
- **AuthController**: 로그인, 로그아웃, 사용자 정보 조회
- **PermissionController**: 권한 관리
- **JwtTokenProvider**: JWT 토큰 생성/검증
- **PermissionService**: 권한 체크 로직

### 2. 게시판 관리 모듈
- **BoardMasterController**: 게시판 CRUD 및 필드 관리
- **DynamicBoardController**: 동적 게시판 데이터 처리
- **BoardMasterService**: 동적 테이블 생성/수정/삭제

### 3. 콘텐츠 관리 모듈
- **ContentController**: 콘텐츠 CRUD
- **ContentFileController**: 파일 업로드/다운로드
- **ContentService**: 계층형 콘텐츠 관리

### 4. 사이트/메뉴 관리 모듈
- **SiteController**: 사이트 설정 관리
- **MenuController**: 메뉴 구조 관리
- **JsonVersionController**: 메뉴 JSON 버전 관리

### 5. 회원 관리 모듈
- **MemberController**: 회원 CRUD
- **MemberService**: 회원 비즈니스 로직

### 6. 렌더링 모듈
- **RenderController**: 프론트엔드 렌더링 지원
- **LayoutController**: 레이아웃 템플릿 관리

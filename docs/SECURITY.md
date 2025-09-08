
## 보안 구현

### JWT 기반 인증
- **토큰 생성**: 사용자 정보 + 만료시간 + JTI
- **토큰 저장**: HttpOnly 쿠키 사용
- **토큰 검증**: 서명 검증 + 블랙리스트 체크
- **토큰 갱신**: 슬라이딩 세션 방식

### 권한 관리
- **@PreAuthorize**: 메서드 레벨 권한 체크 (총 47개 적용)
- **PermissionService**: 세밀한 권한 제어
- **계층형 권한**: 메뉴별, 기능별 권한 분리

### 보안 유틸리티
- **HtmlSanitizerUtil**: XSS 방지
- **CryptoUtil**: 데이터 암호화
- **ValidationUtil**: 입력값 검증
- **IpUtil**: IP 주소 관리
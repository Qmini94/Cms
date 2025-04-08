package kr.co.itid.cms.service.auth;

import kr.co.itid.cms.dto.auth.TokenResponse;

/**
 * AuthService 인터페이스
 * 사용자 인증 관련 서비스 메서드를 정의합니다.
 */
public interface AuthService {

    /**
     * 사용자 로그인 메소드
     * @param userId 사용자 ID
     * @param password 사용자 비밀번호
     * @return TokenResponse 엑세스 토큰 및 리프레시 토큰 반환
     */
    TokenResponse login(String userId, String password) throws Exception;

    /**
     * 사용자 로그아웃 메소드
     * @param token 엑세스 토큰
     */
    void logout(String token) throws Exception;

    /**
     * 엑세스 토큰 갱신 메소드
     * @param refreshToken 리프레시 토큰
     * @return TokenResponse 새로운 엑세스 토큰
     */
    TokenResponse refreshAccessToken(String refreshToken) throws Exception;

    /**
     * 관리자에 의한 강제 로그아웃 메소드
     * @param userId 사용자 ID
     * @param token 엑세스 토큰 (nullable)
     */
    void forceLogoutByAdmin(String userId, String token) throws Exception;
}

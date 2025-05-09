package kr.co.itid.cms.service.auth;

import kr.co.itid.cms.dto.auth.TokenResponse;
import kr.co.itid.cms.dto.auth.UserInfoResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * AuthService 인터페이스
 * 사용자 인증 관련 서비스 메서드를 정의합니다.
 */
public interface AuthService {

    /**
     * 사용자 로그인 메소드
     *
     * @param userId 사용자 ID
     * @param password 사용자 비밀번호
     * @return TokenResponse 엑세스 토큰 반환
     */
    TokenResponse login(String userId, String password) throws Exception;

    /**
     * 사용자 로그아웃 메소드
     *
     */
    void logout() throws Exception;

    /**
     * 토큰 기반 사용자 정보 조회 메소드
     *
     * @param request HttpServletRequest 객체
     * @return UserInfoResponse 사용자 정보 응답
     */
    UserInfoResponse getUserInfoFromToken(HttpServletRequest request) throws Exception;
}

package kr.co.itid.cms.service.auth;

/**
 * 토큰 블랙리스트 관리 서비스 인터페이스
 * Redis를 사용하여 블랙리스트와 리프레시 토큰을 관리합니다.
 */
public interface TokenBlacklistService {

    /**
     * 토큰을 블랙리스트에 추가
     * @param token 블랙리스트에 추가할 토큰
     * @param expirationMillis 블랙리스트 유효 기간 (밀리초)
     */
    void blacklistToken(String token, long expirationMillis) throws Exception;

    /**
     * 리프레시 토큰 삭제
     * @param userId 사용자 ID
     */
    void removeRefreshToken(String userId) throws Exception;

    /**
     * 사용자 세션 차단
     * @param token 사용자 엑세스 토큰
     * @param userId 사용자 ID
     * @param expirationMillis 블랙리스트 유효 기간 (밀리초)
     */
    void blockUserSession(String token, String userId, long expirationMillis) throws Exception;
}

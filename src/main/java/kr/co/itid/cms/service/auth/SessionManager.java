package kr.co.itid.cms.service.auth;

import kr.co.itid.cms.config.security.model.SessionData;
import kr.co.itid.cms.entity.cms.core.member.Member;

import java.util.Optional;

/**
 * Redis 기반 세션 관리 서비스 인터페이스
 */
public interface SessionManager {
    
    /**
     * 새로운 세션 생성
     * @param member 사용자 정보
     * @param hostname 사이트 호스트명
     * @return 세션 ID (sid)
     */
    String createSession(Member member, String hostname);
    
    /**
     * 세션 존재 확인 및 TTL 연장
     * @param sid 세션 ID
     * @return 세션 연장 성공 여부
     */
    boolean extendSession(String sid);
    
    /**
     * 세션 데이터 조회
     * @param sid 세션 ID
     * @return 세션 데이터 (없으면 Optional.empty())
     */
    Optional<SessionData> getSession(String sid);
    
    /**
     * 세션 삭제 (로그아웃 시)
     * @param sid 세션 ID
     */
    void deleteSession(String sid);
    
    /**
     * 세션의 남은 TTL 조회 (초 단위)
     * @param sid 세션 ID
     * @return 남은 TTL (초), 세션이 없으면 -1
     */
    long getSessionTtl(String sid);
    
    /**
     * Redis 상태 확인
     * @return Redis가 정상이면 true
     */
    boolean isRedisHealthy();
}

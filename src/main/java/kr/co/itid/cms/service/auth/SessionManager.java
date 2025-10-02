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
    String createSession(Member member, String hostname) throws Exception;

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
    void deleteSession(String sid) throws Exception;

    /**
     * 세션의 남은 TTL 조회 (초 단위)
     * @param sid 세션 ID
     * @return 남은 TTL (초), 세션이 없으면 -1
     */
    long getSessionTtl(String sid);

    /**
     * 세션 슬라이딩(활동 갱신): 세션이 존재하면 TTL을 연장한다.
     * 구현체에서 설정된 기본 TTL만큼 expire 시간을 갱신.
     * @param sid 세션 ID
     */
    void touchSession(String sid) throws Exception;

    /**
     * Redis 상태 확인
     * @return Redis가 정상이면 true
     */
    boolean isRedisHealthy();
}
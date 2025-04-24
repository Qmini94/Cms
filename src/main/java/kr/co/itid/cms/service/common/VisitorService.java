package kr.co.itid.cms.service.common;

import javax.servlet.http.HttpServletRequest;

/**
 * 방문자 수를 체크하고 카운팅하는 서비스 인터페이스입니다.
 * 클라이언트 요청을 기반으로 방문자 정보를 확인하고 카운트를 수행하는 메서드를 정의합니다.
 */
public interface VisitorService {

    /**
     * 방문자를 확인하고 방문자 수를 증가시킵니다.
     * 요청자 정보를 기반으로 IP + 날짜 + hostname 을 조합하여 중복 방문을 방지하고
     * Redis, DB를 활용해 일일 방문자 수를 체크 및 기록합니다.
     *
     * @param request  HttpServletRequest 객체 (클라이언트 정보 포함)
     */
    void checkAndCountVisitor(HttpServletRequest request) throws Exception;
}

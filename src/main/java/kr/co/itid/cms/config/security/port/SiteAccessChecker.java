package kr.co.itid.cms.config.security.port;

/**
 * 보안 필터에서 필요한 사이트 조회 기능(읽기 전용)
 */
public interface SiteAccessChecker {
    /**
     * 해당 사이트에서 주어진 IP가 접근 가능한지 여부를 반환
     *
     * 우선순위:
     * 1. allow_ip가 지정되어 있으면 → 그 목록에 포함된 IP만 허용
     * 2. deny_ip만 있을 경우 → 그 목록에 포함되지 않은 IP는 허용
     * 3. 둘 다 없으면 → 기본적으로 허용
     *
     * @param siteHostName 사이트 호스트명
     * @param clientIp 클라이언트 IP 주소
     * @return true = 접근 허용, false = 접근 차단
     * @throws Exception 예외 발생 시
     */
    boolean isIpAllowed(String siteHostName, String clientIp) throws Exception;

    /**
     * 해당 사이트가 닫혀있는지 여부 반환
     * @param siteHostName 사이트 호스트명
     * @return true = 닫힘("close"), false = 열림("open") 또는 찾을 수 없음
     */
    boolean isClosedSite(String siteHostName) throws Exception;
}

package kr.co.itid.cms.service.cms.core.site;

import kr.co.itid.cms.dto.cms.core.site.SiteResponse;
import java.util.List;

/**
 * 사이트 관리 서비스 인터페이스
 * 사이트 데이터를 조회하는 메서드를 정의합니다.
 */
public interface SiteService {

    /**
     * 모든 사이트 데이터를 조회
     * @return &lt;SiteResponse&gt; 사이트 데이터 목록
     */
    List<SiteResponse> getSiteAllData() throws Exception;

    /**
     * 사이트 호스트명으로 siteOption 값 조회
     * @param siteHostName 사이트 호스트명 (예: "admin", "www", "business")
     * @return siteOption 값 ("open" 또는 "close")
     */
    String getSiteOptionByHostName(String siteHostName) throws Exception;

    /**
     * 호스트명으로 금지어 배열 반환
     * @param siteHostName 사이트 호스트명
     * @return 금지어 리스트
     */
    List<String> getBadWordsByHostName(String siteHostName) throws Exception;

    /**
     * 해당 사이트가 닫혀있는지 여부 반환
     * @param siteHostName 사이트 호스트명
     * @return true = 닫힘("close"), false = 열림("open") 또는 찾을 수 없음
     */
    boolean isClosedSite(String siteHostName) throws Exception;
}

package kr.co.itid.cms.service.cms.core.site;

import kr.co.itid.cms.dto.cms.core.site.request.SiteRequest;
import kr.co.itid.cms.dto.cms.core.site.response.SiteResponse;
import java.util.List;

/**
 * 사이트 관리 서비스 인터페이스
 * 사이트 데이터를 조회하는 메서드를 정의합니다.
 */
public interface SiteService {

    /**
     * 사이트 호스트명으로 siteOption 값 조회
     * @param siteHostName 사이트 호스트명 (예: "admin", "www", "business")
     * @return siteOption 값 ("open" 또는 "close")
     */
    String getSiteOptionByHostName(String siteHostName) throws Exception;

    /**
     * 삭제되지 않은 사이트 목록 조회
     * @return &lt;SiteResponse&gt; 사이트 데이터 목록
     */
    List<SiteResponse> getSitesIsDeletedFalse() throws Exception;

    /**
     * 전체 사이트 목록 조회 (삭제 포함)
     * @return &lt;SiteResponse&gt; 사이트 데이터 목록
     */
    List<SiteResponse> getSiteAllData() throws Exception;

    /**
     * 해당 사이트가 닫혀있는지 여부 반환
     * @param siteHostName 사이트 호스트명
     * @return true = 닫힘("close"), false = 열림("open") 또는 찾을 수 없음
     */
    boolean isClosedSite(String siteHostName) throws Exception;

    /**
     * 사이트를 저장하거나 수정합니다.
     * siteHostName이 null이면 신규 등록, 존재하면 수정 처리됩니다.
     *
     * @param siteHostName 사이트 호스트명 (null인 경우 신규 등록)
     * @param request 사이트 요청 DTO
     */
    void saveSite(String siteHostName, SiteRequest request) throws Exception;

    /**
     * 사이트를 복구합니다.
     * is_deleted = false 로 설정됩니다.
     *
     * @param siteHostName 복구할 사이트 호스트명
     * @throws Exception 복구 중 오류 발생 시
     */
    void restoreSite(String siteHostName) throws Exception;

    /**
     * 사이트를 소프트 삭제합니다.
     * is_deleted = true 로 설정됩니다.
     *
     * @param siteHostName 삭제할 사이트 호스트명
     * @throws Exception 삭제 중 오류 발생 시
     */
    void softDeleteSite(String siteHostName) throws Exception;

    /**
     * 사이트를 완전 삭제합니다.
     * DB에서 완전히 제거됩니다.
     *
     * @param siteHostName 삭제할 사이트 호스트명
     * @throws Exception 삭제 중 오류 발생 시
     */
    void hardDeleteSite(String siteHostName) throws Exception;
}

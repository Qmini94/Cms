package kr.co.itid.cms.service.list;

import kr.co.itid.cms.dto.list.SiteResponse;
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
}

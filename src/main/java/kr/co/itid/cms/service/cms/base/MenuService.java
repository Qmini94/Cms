package kr.co.itid.cms.service.cms.base;

import kr.co.itid.cms.dto.cms.MenuResponse;
import java.util.List;

/**
 * 메뉴 관리 서비스 인터페이스
 * 메뉴 데이터를 조회하고 계층 구조를 구성하는 메서드를 정의합니다.
 */
public interface MenuService {

    /**
     * 모든 드라이브 메뉴를 조회
     * @return &lt;MenuResponse&gt; 드라이브 메뉴 목록
     */
    List<MenuResponse> getAllDrives() throws Exception;

    /**
     * 특정 이름을 가진 드라이브의 모든 하위 메뉴 조회
     * @param name 드라이브 이름
     * @return &lt;MenuResponse&gt; 하위 메뉴 목록
     */
    List<MenuResponse> getAllChildrenByName(String name) throws Exception;
}

package kr.co.itid.cms.service.cms.core.menu;

import kr.co.itid.cms.dto.cms.core.menu.response.MenuResponse;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuTreeLiteResponse;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuTreeResponse;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuTypeValueResponse;

import java.util.List;

/**
 * 메뉴 관리 서비스 인터페이스
 * 메뉴 데이터를 조회하고 계층 구조를 구성하는 메서드를 정의합니다.
 */
public interface MenuService {

    /**
     * 메뉴 ID로 단일 메뉴 정보를 조회합니다.
     *
     * @param id 메뉴 고유 ID
     * @return MenuResponse 메뉴 응답 객체
     * @throws Exception 데이터베이스 접근 오류 또는 처리 중 예외
     */
    MenuResponse getMenuById(Long id) throws Exception;

    /**
     * 메뉴 ID로 단일 메뉴 정보를 조회합니다.
     *
     * @param id 메뉴 고유 ID
     * @return MenuTypeValueResponse 메뉴 응답 객체
     * @throws Exception 데이터베이스 접근 오류 또는 처리 중 예외
     */
    MenuTypeValueResponse getMenuRenderById(Long id) throws Exception;

    /**
     * 모든 드라이브 메뉴를 조회
     * @return &lt;MenuResponse&gt; 드라이브 메뉴 목록
     */
    List<MenuResponse> getRootMenus() throws Exception;

    /**
     * 특정 이름을 가진 드라이브의 모든 하위 메뉴 조회(필요한 몇가지의 필드만)
     * @param name 드라이브 이름
     * @return &lt;MenuResponse&gt; 하위 메뉴 목록(필요한 몇가지의 필드만)
     */
    List<MenuTreeLiteResponse> getMenuTreeLiteByName(String name) throws Exception;

    /**
     * 특정 이름을 가진 드라이브의 모든 하위 메뉴 조회
     * @param name 드라이브 이름
     * @return &lt;MenuResponse&gt; 하위 메뉴 목록
     */
    List<MenuTreeResponse> getMenuTreeByName(String name) throws Exception;
}

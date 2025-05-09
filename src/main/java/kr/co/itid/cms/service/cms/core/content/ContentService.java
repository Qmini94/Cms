package kr.co.itid.cms.service.cms.core.content;

import kr.co.itid.cms.dto.cms.core.content.ContentRequest;
import kr.co.itid.cms.dto.cms.core.content.ContentResponse;

import java.util.List;

/**
 * 콘텐츠 관리 서비스 인터페이스입니다.
 * 콘텐츠 생성, 조회, 수정 등의 기능을 제공합니다.
 */
public interface ContentService {

    /**
     * menu_idx별 가장 최근 등록된 콘텐츠 1건씩 조회합니다.
     *
     * @return List&lt;ContentResponse&gt; 최신 콘텐츠 목록
     * @throws Exception DB 조회 또는 변환 중 예외 발생 시
     */
    List<ContentResponse> getLatestContentsPerMenu() throws Exception;

    /**
     * 특정 menu_idx에 해당하는 콘텐츠 전체를 조회합니다.
     *
     * @param menuIdx 메뉴 고유번호
     * @return List&lt;ContentResponse&gt; 해당 메뉴의 전체 콘텐츠 목록
     * @throws Exception DB 조회 또는 변환 중 예외 발생 시
     */
    List<ContentResponse> getContentsByMenuIdx(Integer menuIdx) throws Exception;

    /**
     * 특정 콘텐츠를 idx로 조회합니다.
     *
     * @param idx 콘텐츠 고유번호
     * @return ContentResponse 해당 콘텐츠의 상세 정보
     * @throws Exception DB 조회 또는 존재하지 않는 콘텐츠일 경우
     */
    ContentResponse getByIdx(Integer idx) throws Exception;

    /**
     * 콘텐츠를 등록합니다.
     * 동일한 menuIdx를 가진 기존 콘텐츠는 is_use=false로 변경되고,
     * 새로 저장되는 콘텐츠만 is_use=true로 설정됩니다.
     *
     * @param request 등록할 콘텐츠 요청 객체
     * @throws Exception 데이터 저장 또는 트랜잭션 중 오류 발생 시
     */
    void create(ContentRequest request) throws Exception;

    /**
     * 콘텐츠를 수정합니다.
     *
     * @param idx 수정할 콘텐츠 ID
     * @param request 수정할 데이터 요청 객체
     * @throws Exception DB 조회 실패 또는 저장 실패 시
     */
    void update(Integer idx, ContentRequest request) throws Exception;
}
package kr.co.itid.cms.service.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.response.FieldDefinitionResponse;
import kr.co.itid.cms.dto.cms.core.common.PaginationOption;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * 동적 게시판 처리 서비스 인터페이스입니다.
 * board_{boardId} 테이블 기반으로 게시글 CRUD 및 필드 정의 조회 기능을 제공합니다.
 * 게시판 식별은 menuId → boardId → board_{boardId} 방식으로 처리됩니다.
 */
public interface DynamicBoardService {

    /**
     * 현재 사용자의 메뉴에 연결된 게시판의 필드 정의 목록을 조회합니다.
     *
     * @return List&lt;FieldDefinitionResponse&gt; 필드 정의 목록
     */
    List<FieldDefinitionResponse> getFieldDefinitions() throws Exception;

    /**
     * 현재 사용자의 메뉴에 연결된 게시판의 게시글 목록을 검색 조건 및 페이징에 따라 조회합니다.
     *
     * @param option 검색 옵션 (키워드, 검색 키 등)
     * @param pagination 페이징 옵션 (페이지 번호, 사이즈 등)
     * @return Page&lt;Map&lt;String, Object&gt;&gt; 페이징된 게시글 목록
     */
    Page<Map<String, Object>> getList(SearchOption option, PaginationOption pagination) throws Exception;

    /**
     * 게시판에서 특정 게시글을 조회합니다.
     *
     * @param idx 게시글 고유 ID
     * @return Map&lt;String, Object&gt; 게시글 데이터
     */
    Map<String, Object> getOne(Long idx) throws Exception;

    /**
     * 게시판에서 특정 게시글의 등록자를 조회합니다.
     *
     * @param idx 게시글 고유 ID
     * @return String 게시글 데이터
     */
    String getRegIdByBoard(Long idx) throws Exception;

    /**
     * 게시글을 등록하거나 수정합니다.
     * ID가 null이면 등록, 존재하면 수정 처리됩니다.
     *
     * @param idx 게시글 ID (null이면 신규 등록)
     * @param data 게시글 데이터
     */
    void save(Long idx, Map<String, Object> data) throws Exception;

    /**
     * 게시판의 게시글을 삭제합니다.
     *
     * @param idx 삭제할 게시글 ID
     */
    void delete(Long idx) throws Exception;
}
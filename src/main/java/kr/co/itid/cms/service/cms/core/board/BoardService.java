package kr.co.itid.cms.service.cms.core.board;

import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.dto.cms.core.board.request.BoardRequest;
import kr.co.itid.cms.dto.cms.core.board.response.BoardResponse;
import kr.co.itid.cms.dto.cms.core.board.response.BoardViewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 게시글 관리 서비스 인터페이스입니다.
 * 게시글 데이터를 조회, 등록, 수정, 삭제하는 메서드를 제공합니다.
 */
public interface BoardService {

    /**
     * 게시글 목록을 검색 조건 및 페이징 옵션에 따라 조회합니다.
     *
     * @param option 게시글 검색 및 페이징 옵션
     * @param pageable Pageable 객체
     * @return Page&lt;BoardResponse&gt; 페이징 처리된 게시글 목록
     * @throws Exception 게시글 조회 중 오류 발생 시
     */
    Page<BoardResponse> searchBoardList(SearchOption option, Pageable pageable) throws Exception;

    /**
     * 게시글 고유 ID(idx)로 게시글 정보를 조회합니다.
     *
     * @param idx 게시글 고유 ID
     * @return BoardViewResponse 게시글 정보
     */
    BoardViewResponse getBoard(Long idx) throws Exception;

    /**
     * 게시글을 저장하거나 수정합니다.
     * idx가 null이면 신규 등록, 존재하면 수정 처리됩니다.
     *
     * @param idx 게시글 고유 ID (null인 경우 신규 등록)
     * @param request 게시글 요청 DTO
     */
    void saveBoard(Long idx, BoardRequest request) throws Exception;

    /**
     * 게시글을 삭제합니다.
     *
     * @param idx 삭제할 게시글 고유 ID
     */
    void deleteBoard(Long idx) throws Exception;
}
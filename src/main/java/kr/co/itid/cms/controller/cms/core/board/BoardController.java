package kr.co.itid.cms.controller.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.BoardSearchOption;
import kr.co.itid.cms.dto.cms.core.board.PaginationOption;
import kr.co.itid.cms.dto.cms.core.board.request.BoardRequest;
import kr.co.itid.cms.dto.cms.core.board.response.BoardResponse;
import kr.co.itid.cms.dto.cms.core.board.response.BoardViewResponse;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.cms.core.board.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

/**
 * 게시글 정보를 다루는 API 컨트롤러입니다.
 * 게시글 목록 조회, 단건 조회, 등록, 수정, 삭제 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/back-api/board")
@Validated
public class BoardController {

    private final BoardService boardService;

    /**
     * 게시글 목록을 조회합니다.
     *
     * @param option 게시글 검색 및 페이징 옵션
     * @return ApiResponse&lt;Page&lt;BoardResponse&gt;&gt; 게시글 목록
     */
    @PreAuthorize("@permService.hasAccess('ACCESS')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BoardResponse>>> getBoardList(
            @Valid @ModelAttribute BoardSearchOption option,
            @Valid @ModelAttribute PaginationOption pagination,
            BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(ApiResponse.error(400, errorMsg));
        }

        Pageable pageable = pagination.toPageable();
        Page<BoardResponse> boards = boardService.searchBoardList(option, pageable);
        return ResponseEntity.ok(ApiResponse.success(boards));
    }

    /**
     * 게시글 단건을 조회합니다.
     *
     * @param idx 게시글 고유 ID
     * @return ApiResponse&lt;BoardViewResponse&gt; 게시글 정보
     */
    @PreAuthorize("@permService.hasAccess('VIEW')")
    @GetMapping("/{idx}")
    public ResponseEntity<ApiResponse<BoardViewResponse>> getBoard(
            @PathVariable @Positive(message = "게시글 ID는 1 이상의 값이어야 합니다") Long idx) throws Exception {

        BoardViewResponse board = boardService.getBoard(idx);
        return ResponseEntity.ok(ApiResponse.success(board));
    }

    /**
     * 게시글을 등록합니다.
     *
     * @param request 게시글 등록 요청 DTO
     * @return ApiResponse&lt;Void&gt; 등록 완료 응답
     */
    @PreAuthorize("@permService.hasAccess('WRITE')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createBoard(
            @Valid @RequestBody BoardRequest request) throws Exception {

        boardService.saveBoard(null, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    /**
     * 게시글을 수정합니다.
     *
     * @param idx 수정할 게시글 고유 ID
     * @param request 게시글 수정 요청 DTO
     * @return ApiResponse&lt;Void&gt; 수정 완료 응답
     */
    @PreAuthorize("@permService.hasAccess('MODIFY')")
    @PutMapping("/{idx}")
    public ResponseEntity<ApiResponse<Void>> updateBoard(
            @PathVariable @Positive(message = "게시글 ID는 1 이상의 값이어야 합니다") Long idx,
            @Valid @RequestBody BoardRequest request) throws Exception {

        boardService.saveBoard(idx, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 게시글을 삭제합니다.
     *
     * @param idx 삭제할 게시글 고유 ID
     * @return ApiResponse&lt;Void&gt; 삭제 완료 응답
     */
    @PreAuthorize("@permService.hasAccess('REMOVE')")
    @DeleteMapping("/{idx}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(
            @PathVariable @Positive(message = "게시글 ID는 1 이상의 값이어야 합니다") Long idx) throws Exception {

        boardService.deleteBoard(idx);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

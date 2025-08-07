package kr.co.itid.cms.controller.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterResponse;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.dto.cms.core.common.PaginationOption;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterListResponse;
import kr.co.itid.cms.dto.cms.core.board.request.BoardMasterRequest;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.cms.core.board.BoardMasterService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/back-api/boardMaster")
@Validated
public class BoardMasterController {

    private final BoardMasterService boardMasterService;

    /**
     * 게시판 목록 조회 (검색 + 페이징)
     */
    @PreAuthorize("@permService.hasAccess('ACCESS')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<BoardMasterListResponse>>> getBoardMasters(
            @Valid @ModelAttribute SearchOption option,
            @Valid @ModelAttribute PaginationOption pagination,
            BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(ApiResponse.error(400, errorMsg));
        }

        Pageable pageable = pagination.toPageable();
        Page<BoardMasterListResponse> page = boardMasterService.searchBoardMasters(option, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    /**
     * 게시판 단일 조회
     */
    @PreAuthorize("@permService.hasAccess('ACCESS')")
    @GetMapping("/{idx}")
    public ResponseEntity<ApiResponse<BoardMasterResponse>> getBoardMaster(
            @PathVariable @Positive(message = "게시판 ID는 1 이상의 값이어야 합니다") Long idx) throws Exception {

        BoardMasterResponse response = boardMasterService.getBoardByIdx(idx);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 게시판 생성
     */
    @PreAuthorize("@permService.hasAccess('WRITE')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createBoard(
            @Valid @RequestBody BoardMasterRequest request) throws Exception {

        boardMasterService.createBoard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    /**
     * 게시판 수정
     */
    @PreAuthorize("@permService.hasAccess('MODIFY')")
    @PutMapping("/{idx}")
    public ResponseEntity<ApiResponse<Void>> updateBoard(
            @PathVariable @Positive(message = "게시판 ID는 1 이상의 값이어야 합니다") Long idx,
            @Valid @RequestBody BoardMasterRequest request) throws Exception {

        boardMasterService.updateBoard(idx, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 게시판 삭제
     */
    @PreAuthorize("@permService.hasAccess('REMOVE')")
    @DeleteMapping("/{idx}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(
            @PathVariable @Positive(message = "게시판 ID는 1 이상의 값이어야 합니다") Long idx) throws Exception {

        boardMasterService.deleteBoard(idx);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
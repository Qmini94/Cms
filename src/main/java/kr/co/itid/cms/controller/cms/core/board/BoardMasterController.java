package kr.co.itid.cms.controller.cms.core.board;

import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.dto.cms.core.common.PaginationOption;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterListResponse;
import kr.co.itid.cms.dto.cms.core.board.request.BoardMasterRequest;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterResponse;
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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

/**
 * 게시판 정의 정보를 다루는 API 컨트롤러입니다.
 * 게시판 목록 조회, 단건 조회, 등록, 수정, 삭제 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/back-api/boardMaster")  // 복수형으로 리소스 이름 변경
@Validated
public class BoardMasterController {

    private final BoardMasterService boardMasterService;

    /**
     * 게시판 마스터 목록을 조회합니다.
     *
     * @param option 검색 조건
     * @param pagination 페이징 조건
     * @param bindingResult 유효성 검사 결과
     * @return ApiResponse&lt;Page&lt;BoardMasterListResponse&gt;&gt; 게시판 마스터 목록
     * @throws Exception 예외 발생 시 처리됨
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
     * 새 게시판을 등록합니다.
     *
     * @param request 유효성 검증된 게시판 요청 DTO
     * @return ApiResponse&lt;BoardMasterResponse&gt; 생성된 게시판 정보
     * @throws Exception 예외 발생 시 처리됨
     */
    @PreAuthorize("@permService.hasAccess('WRITE')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createBoard(@Valid @RequestBody BoardMasterRequest request) throws Exception {

        boardMasterService.saveBoard(null, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    /**
     * 기존 게시판 정보를 수정합니다.
     *
     * @param idx 수정할 게시판 ID
     * @param request 유효성 검증된 게시판 요청 DTO
     * @return ApiResponse&lt;BoardMasterResponse&gt; 수정된 게시판 정보
     * @throws Exception 예외 발생 시 처리됨
     */
    @PreAuthorize("@permService.hasAccess('MODIFY')")
    @PutMapping("/{idx}")
    public ResponseEntity<ApiResponse<Void>> updateBoard(
            @PathVariable @Positive(message = "게시판 ID는 1 이상의 값이어야 합니다") Long idx,
            @Valid @RequestBody BoardMasterRequest request) throws Exception {

        boardMasterService.saveBoard(idx, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 게시판을 삭제합니다.
     *
     * @param idx 삭제할 게시판 고유번호 (양수 필수)
     * @return ApiResponse&lt;Void&gt; 삭제 성공 여부
     * @throws Exception 예외 발생 시 처리됨
     */
    @PreAuthorize("@permService.hasAccess('REMOVE')")
    @DeleteMapping("/{idx}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(
            @PathVariable @Positive(message = "게시판 ID는 1 이상의 값이어야 합니다") Long idx) throws Exception {

        boardMasterService.deleteBoard(idx);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
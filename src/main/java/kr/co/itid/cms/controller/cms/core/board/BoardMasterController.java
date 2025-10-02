package kr.co.itid.cms.controller.cms.core.board;

import kr.co.itid.cms.aop.ExecutionTime;
import kr.co.itid.cms.dto.cms.core.board.request.BoardCreateRequest;
import kr.co.itid.cms.dto.cms.core.board.request.BoardFieldDefinitionsUpsertRequest;
import kr.co.itid.cms.dto.cms.core.board.request.BoardUpdateRequest;
import kr.co.itid.cms.dto.cms.core.board.response.BoardFieldDefinitionResponse;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterListResponse;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterResponse;
import kr.co.itid.cms.dto.cms.core.common.PaginationOption;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
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
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/back-api/boardMaster")
@Validated
public class BoardMasterController {

    private final BoardMasterService boardMasterService;

    // 목록
    @PreAuthorize("@permService.hasAccess('MANAGE')")
    @GetMapping
    @ExecutionTime
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

    // 단건
    @PreAuthorize("@permService.hasAccess('MANAGE')")
    @GetMapping("/{idx}")
    public ResponseEntity<ApiResponse<BoardMasterResponse>> getBoardMaster(
            @PathVariable @Positive(message = "게시판 ID는 1 이상의 값이어야 합니다") Long idx) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(boardMasterService.getBoardByIdx(idx)));
    }

    // 생성: 메타 + 필드 동시
    @PreAuthorize("@permService.hasAccess('MANAGE')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createBoard(
            @Valid @RequestBody BoardCreateRequest request) throws Exception {

        boardMasterService.createBoard(request); // 내부에서: master insert → fields insert → CREATE TABLE
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    // 수정: 메타 + 필드 동시 (테이블 동기화까지)
    @PreAuthorize("@permService.hasAccess('MANAGE')")
    @PutMapping("/{idx}")
    public ResponseEntity<ApiResponse<Void>> updateBoard(
            @PathVariable @Positive Long idx,
            @Valid @RequestBody BoardUpdateRequest request) throws Exception {

        request.setIdx(idx);
        boardMasterService.updateBoard(request); // 내부에서: master update → fields replace → ALTER(sync)
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 삭제
    @PreAuthorize("@permService.hasAccess('MANAGE')")
    @DeleteMapping("/{idx}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(
            @PathVariable @Positive Long idx) throws Exception {

        boardMasterService.deleteBoard(idx); // 내부: defs 삭제 → master 삭제 → DROP TABLE
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // --- 필드 전용 엔드포인트(선택적 추가) ---

    // 필드 목록 조회
    @PreAuthorize("@permService.hasAccess('MANAGE')")
    @GetMapping("/{idx}/fields")
    public ResponseEntity<ApiResponse<List<BoardFieldDefinitionResponse>>> getFields(
            @PathVariable @Positive Long idx) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(boardMasterService.getFieldDefinitions(idx)));
    }

    // 필드 업서트 + 테이블 동기화
    @PreAuthorize("@permService.hasAccess('MANAGE')")
    @PutMapping("/{idx}/fields")
    public ResponseEntity<ApiResponse<Void>> upsertFields(
            @PathVariable @Positive Long idx,
            @Valid @RequestBody BoardFieldDefinitionsUpsertRequest req) throws Exception {

        // 보안: path idx와 body idx 일치 체크
        if (!idx.equals(req.getBoardMasterIdx())) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "잘못된 요청입니다."));
        }
        boardMasterService.upsertFieldDefinitionsAndSync(req); // defs 교체 → ALTER(sync)
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 강제 동기화(옵션)
    @PreAuthorize("@permService.hasAccess('MANAGE')")
    @PostMapping("/{idx}/sync")
    public ResponseEntity<ApiResponse<Void>> syncPhysicalTable(@PathVariable @Positive Long idx) throws Exception {
        boardMasterService.syncPhysicalTableWithDefinitions(idx);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
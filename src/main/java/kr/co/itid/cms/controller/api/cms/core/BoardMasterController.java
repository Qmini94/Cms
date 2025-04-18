package kr.co.itid.cms.controller.api.cms.core;

import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.entity.cms.core.BoardMaster;
import kr.co.itid.cms.service.cms.core.BoardMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 게시판 정의 정보를 다루는 API 컨트롤러입니다.
 * 게시판 목록 조회, 단건 조회, 등록, 수정, 삭제 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/list/board")
public class BoardMasterController {

    private final BoardMasterService boardMasterService;

    /**
     * 전체 게시판 목록을 조회합니다.
     *
     * @param menuId 접근 권한 검증용 메뉴 ID
     * @return ApiResponse<List<BoardMaster>> 전체 게시판 목록
     * @throws Exception 예외 발생 시 처리됨
     */
    @PreAuthorize("@permService.hasAccess(#menuId, 'VIEW')")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<BoardMaster>>> getAllBoards(@RequestParam long menuId) throws Exception {
        List<BoardMaster> list = boardMasterService.getAllBoards();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * 게시판을 ID로 조회합니다.
     *
     * @param menuId 접근 권한 검증용 메뉴 ID
     * @param id 게시판 고유번호
     * @return ApiResponse<BoardMaster> 게시판 데이터
     */
    @PreAuthorize("@permService.hasAccess(#menuId, 'VIEW')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BoardMaster>> getBoardById(@RequestParam long menuId, @PathVariable Long id) throws Exception {
        return boardMasterService.getBoardById(id)
                .map(board -> ResponseEntity.ok(ApiResponse.success(board)))
                .orElse(ResponseEntity.ok(ApiResponse.error(404,"Not found")));
    }

    /**
     * 게시판을 boardId로 조회합니다.
     *
     * @param menuId 접근 권한 검증용 메뉴 ID
     * @param boardId 게시판 식별 ID
     * @return ApiResponse<BoardMaster> 게시판 데이터
     */
    @PreAuthorize("@permService.hasAccess(#menuId, 'VIEW')")
    @GetMapping("/code/{boardId}")
    public ResponseEntity<ApiResponse<BoardMaster>> getBoardByBoardId(@RequestParam long menuId, @PathVariable String boardId) throws Exception {
        return boardMasterService.getBoardByBoardId(boardId)
                .map(board -> ResponseEntity.ok(ApiResponse.success(board)))
                .orElse(ResponseEntity.ok(ApiResponse.error(404,"Not found")));
    }

    /**
     * 게시판 정보를 등록하거나 수정합니다.
     *
     * @param menuId 접근 권한 검증용 메뉴 ID
     * @param board 게시판 정보
     * @return ApiResponse<BoardMaster> 저장된 게시판 정보
     */
    @PreAuthorize("@permService.hasAccess(#menuId, 'WRITE')")
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<BoardMaster>> saveBoard(@RequestParam long menuId, @RequestBody BoardMaster board) throws Exception {
        BoardMaster saved = boardMasterService.save(board);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    /**
     * 게시판을 삭제합니다.
     *
     * @param menuId 접근 권한 검증용 메뉴 ID
     * @param id 삭제할 게시판 ID
     * @return ApiResponse<Void> 삭제 성공 여부
     */
    @PreAuthorize("@permService.hasAccess(#menuId, 'REMOVE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(@RequestParam long menuId, @PathVariable Long id) throws Exception {
        boardMasterService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
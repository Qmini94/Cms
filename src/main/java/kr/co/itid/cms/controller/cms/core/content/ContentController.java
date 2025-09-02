package kr.co.itid.cms.controller.cms.core.content;

import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.dto.cms.core.common.PaginationOption;
import kr.co.itid.cms.dto.cms.core.content.request.ChildContentRequest;
import kr.co.itid.cms.dto.cms.core.content.request.ContentRequest;
import kr.co.itid.cms.dto.cms.core.content.response.ContentResponse;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.cms.core.content.ContentService;
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

/**
 * 콘텐츠 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/back-api/content")
@Validated
public class ContentController {

    private final ContentService contentService;

    /**
     * 콘텐츠 목록을 조회합니다.
     *
     * @param option 검색 조건
     * @param pagination 페이징 조건
     * @param bindingResult 유효성 검사 결과
     * @return 콘텐츠 목록 (페이징)
     * @throws Exception 예외 발생 시
     */
    @PreAuthorize("@permService.hasAccess('ACCESS')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ContentResponse>>> getContents(
            @Valid @ModelAttribute SearchOption option,
            @Valid @ModelAttribute PaginationOption pagination,
            BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(ApiResponse.error(400, errorMsg));
        }

        Pageable pageable = pagination.toPageable();
        Page<ContentResponse> page = contentService.searchContents(option, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }

    /**
     * 특정 컨텐츠의 같은 그룹의 콘텐츠 목록 조회
     *
     * @param idx 대표 콘텐츠 ID
     * @return 해당 그룹의 하위 콘텐츠 리스트
     */
    @PreAuthorize("@permService.hasAccess('ACCESS')")
    @GetMapping("/group/{idx}")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getGroupedContents(
            @PathVariable @Positive(message = "idx는 1 이상의 값이어야 합니다") Long idx) throws Exception {

        List<ContentResponse> list = contentService.getContentsByIdx(idx);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * 콘텐츠의 group중 실제 is_main이 true인 콘텐츠 상세 조회
     *
     * @param idx 콘텐츠 ID
     * @return 콘텐츠 상세 정보
     */
    @PreAuthorize("@permService.hasAccess('ACCESS')")
    @GetMapping("/{idx}/view")
    public ResponseEntity<ApiResponse<ContentResponse>> getContentsDetail(
            @PathVariable @Positive(message = "idx는 1 이상의 값이어야 합니다") Long idx) throws Exception {

        ContentResponse content = contentService.getContentByParentId(idx);
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    /**
     * 대표 콘텐츠(루트) 등록
     *
     * @param request 등록 요청 DTO
     */
    @PreAuthorize("@permService.hasAccess('WRITE')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createRootContent(
            @Valid @RequestBody ContentRequest request) throws Exception {

        contentService.createRootContent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    /**
     * 하위 콘텐츠 등록 (parentId 콘텐츠 그룹에 소속)
     *
     * @param idx 콘텐츠 ID
     * @param request 하위 콘텐츠 등록 요청
     */
    @PreAuthorize("@permService.hasAccess('WRITE')")
    @PostMapping("/{idx}")
    public ResponseEntity<ApiResponse<Void>> createChildContent(
            @PathVariable @Positive(message = "idx는 1 이상의 값이어야 합니다") Long idx,
            @Valid @RequestBody ContentRequest request) throws Exception {

        contentService.createChildContent(idx, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    /**
     * 콘텐츠 수정
     */
    @PreAuthorize("@permService.hasAccess('MODIFY')")
    @PutMapping("/{idx}")
    public ResponseEntity<ApiResponse<Void>> updateContents(
            @PathVariable @Positive(message = "idx는 1 이상의 값이어야 합니다") Long idx,
            @Valid @RequestBody ContentRequest request) throws Exception {

        contentService.updateContent(idx, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 콘텐츠 활성화
     */
    @PreAuthorize("@permService.hasAccess('MODIFY')")
    @PutMapping("/{idx}/active")
    public ResponseEntity<ApiResponse<Void>> activeContent(
            @PathVariable @Positive(message = "idx는 1 이상의 값이어야 합니다") Long idx) throws Exception {

        contentService.activeContent(idx);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 단일 콘텐츠 삭제
     *
     * @param idx 삭제할 콘텐츠 ID
     */
    @PreAuthorize("@permService.hasAccess('REMOVE')")
    @DeleteMapping("/{idx}")
    public ResponseEntity<ApiResponse<Void>> deleteContent(
            @PathVariable @Positive(message = "idx는 1 이상의 값이어야 합니다") Long idx) throws Exception {

        contentService.deleteContentByIdx(idx);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 콘텐츠 그룹 전체 삭제 (루트 포함)
     *
     * @param idx 삭제할 IDX
     */
    @PreAuthorize("@permService.hasAccess('REMOVE')")
    @DeleteMapping("/group/{idx}")
    public ResponseEntity<ApiResponse<Void>> deleteGroupContent(
            @PathVariable @Positive(message = "idx는 1 이상의 값이어야 합니다") Long idx) throws Exception {

        contentService.deleteContentByParentId(idx);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
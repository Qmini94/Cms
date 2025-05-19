package kr.co.itid.cms.controller.cms.core.content;

import kr.co.itid.cms.dto.cms.core.content.ContentRequest;
import kr.co.itid.cms.dto.cms.core.content.ContentResponse;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.cms.core.content.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/content")
@Validated
public class ContentController {

    private final ContentService contentService;

    /**
     * 전체 콘텐츠 중 sort=0인 대표 콘텐츠 목록 조회
     *
     * @return 대표 콘텐츠 리스트
     * @throws Exception 예외 발생 시
     */
    @PreAuthorize("@permService.hasAccess('VIEW')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getTopSortedContents() throws Exception {
        List<ContentResponse> list = contentService.getTopSortedContents();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * 특정 parentId 그룹의 하위 콘텐츠 목록 조회
     *
     * @param parentId 대표 콘텐츠 ID
     * @return 해당 그룹의 하위 콘텐츠 리스트
     */
    @PreAuthorize("@permService.hasAccess('VIEW')")
    @GetMapping("/group/{parentId}")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getGroupedContents(
            @PathVariable @Positive(message = "parentId는 1 이상의 값이어야 합니다") Long parentId) throws Exception {

        List<ContentResponse> list = contentService.getContentsByParentId(parentId);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * 특정 콘텐츠 상세 조회
     *
     * @param idx 콘텐츠 ID
     * @return 콘텐츠 상세 정보
     */
    @PreAuthorize("@permService.hasAccess('VIEW')")
    @GetMapping("/{idx}/view")
    public ResponseEntity<ApiResponse<ContentResponse>> getContentsDetail(
            @PathVariable @Positive(message = "idx는 1 이상의 값이어야 합니다") Long idx) throws Exception {

        ContentResponse content = contentService.getContentByIdx(idx);
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
     * @param parentId 루트 콘텐츠 ID
     * @param request 하위 콘텐츠 등록 요청
     */
    @PreAuthorize("@permService.hasAccess('WRITE')")
    @PostMapping("/{parentId}")
    public ResponseEntity<ApiResponse<Void>> createChildContent(
            @PathVariable @Positive(message = "parentId는 1 이상의 값이어야 합니다") Long parentId,
            @Valid @RequestBody ContentRequest request) throws Exception {

        contentService.createChildContent(parentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    /**
     * 콘텐츠 수정
     */
    @PreAuthorize("@permService.hasAccess('WRITE')")
    @PutMapping("/{idx}")
    public ResponseEntity<ApiResponse<Void>> updateContents(
            @PathVariable @Positive(message = "idx는 1 이상의 값이어야 합니다") Long idx,
            @Valid @RequestBody ContentRequest request) throws Exception {

        contentService.updateContent(idx, request);
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
     * @param parentId 삭제할 그룹의 루트 ID
     */
    @PreAuthorize("@permService.hasAccess('REMOVE')")
    @DeleteMapping("/group/{parentId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroupContent(
            @PathVariable @Positive(message = "parentId는 1 이상의 값이어야 합니다") Long parentId) throws Exception {

        contentService.deleteContentByParentId(parentId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
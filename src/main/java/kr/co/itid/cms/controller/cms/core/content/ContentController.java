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
@RequestMapping("/api/contents")
@Validated
public class ContentController {

    private final ContentService contentService;

    /**
     * menu_idx 별로 가장 최근 등록된 콘텐츠 1개씩 조회
     *
     * @return 최신 콘텐츠 목록
     * @throws Exception 데이터 조회 중 오류 발생 시
     */
    @PreAuthorize("@permService.hasAccess('VIEW')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getLatestContentsPerMenu() throws Exception {
        List<ContentResponse> list = contentService.getLatestContentsPerMenu();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * 특정 menu_idx에 해당하는 전체 콘텐츠 조회
     *
     * @param menuIdx 메뉴 고유번호
     * @return 해당 메뉴의 콘텐츠 목록
     * @throws Exception 데이터 조회 중 오류 발생 시
     */
    @PreAuthorize("@permService.hasAccess('VIEW')")
    @GetMapping("/{menuIdx}")
    public ResponseEntity<ApiResponse<List<ContentResponse>>> getContentsByMenuIdx(
            @PathVariable @Positive(message = "menuIdx는 양수여야 합니다") Integer menuIdx) throws Exception {

        List<ContentResponse> list = contentService.getContentsByMenuIdx(menuIdx);
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * 특정 콘텐츠 idx로 전체 정보 조회 (본문 포함)
     *
     * @param idx 콘텐츠 고유번호
     * @return 콘텐츠 상세 정보
     * @throws Exception 데이터 조회 중 오류 발생 시
     */
    @PreAuthorize("@permService.hasAccess('VIEW')")
    @GetMapping("/{idx}/view")
    public ResponseEntity<ApiResponse<ContentResponse>> getContentsDetail(
            @PathVariable @Positive(message = "idx는 1 이상의 값이어야 합니다") Integer idx) throws Exception {

        ContentResponse content = contentService.getByIdx(idx);
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    /**
     * 콘텐츠 등록 (해당 menuIdx의 다른 콘텐츠는 is_use=false 처리)
     *
     * @param request 콘텐츠 등록 요청 객체
     * @return 성공 응답
     * @throws Exception 저장 처리 중 오류 발생 시
     */
    @PreAuthorize("@permService.hasAccess('WRITE')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createContents(@Valid @RequestBody ContentRequest request) throws Exception {
        contentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    /**
     * 콘텐츠 수정
     *
     * @param idx 수정할 콘텐츠 ID
     * @param request 수정 요청 객체
     * @return 성공 응답
     * @throws Exception 수정 처리 중 오류 발생 시
     */
    @PreAuthorize("@permService.hasAccess('WRITE')")
    @PutMapping("/{idx}")
    public ResponseEntity<ApiResponse<Void>> updateContents(
            @PathVariable @Positive(message = "idx는 1 이상의 값이어야 합니다") Integer idx,
            @Valid @RequestBody ContentRequest request) throws Exception {

        contentService.update(idx, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
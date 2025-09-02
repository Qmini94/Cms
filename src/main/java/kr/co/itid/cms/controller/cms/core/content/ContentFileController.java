package kr.co.itid.cms.controller.cms.core.content;

import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.dto.cms.core.content.response.UploadedFileResponse;
import kr.co.itid.cms.service.cms.core.content.ContentFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * 콘텐츠 파일 관련 API 컨트롤러
 * - 저장 경로: ./public/contents/cms/{parentId}/{fileName}
 * - 접근 경로: /contents/cms/{parentId}/{fileName}
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/back-api/content")
@Validated
public class ContentFileController {

    private final ContentFileService contentFileService;

    /**
     * 파일 목록 조회
     * @param parentId 그룹 ID (루트/대표 콘텐츠의 parentId)
     */
    @PreAuthorize("@permService.hasAccess('ACCESS')")
    @GetMapping("/files")
    public ResponseEntity<ApiResponse<List<UploadedFileResponse>>> listFiles(
            @RequestParam @Positive(message = "parentId는 1 이상의 값이어야 합니다") Long parentId
    ) throws Exception {
        List<UploadedFileResponse> files = contentFileService.listFiles(parentId);
        return ResponseEntity.ok(ApiResponse.success(files));
    }

    /**
     * 파일 업로드 (여러 개)
     * @param parentId 그룹 ID
     * @param files Multipart 파일들
     */
    @PreAuthorize("@permService.hasAccess('WRITE')")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<UploadedFileResponse>>> uploadFiles(
            @RequestParam @Positive(message = "parentId는 1 이상의 값이어야 합니다") Long parentId,
            @RequestPart("files") List<MultipartFile> files
    ) throws Exception {
        List<UploadedFileResponse> saved = contentFileService.saveFiles(parentId, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(saved));
    }

    /**
     * 파일 삭제 (단일)
     * @param parentId 그룹 ID
     * @param filename 삭제할 파일명
     */
    @PreAuthorize("@permService.hasAccess('REMOVE')")
    @DeleteMapping("/files")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @RequestParam @Positive(message = "parentId는 1 이상의 값이어야 합니다") Long parentId,
            @RequestParam @NotBlank(message = "filename은 필수입니다") String filename
    ) throws Exception {
        contentFileService.deleteFile(parentId, filename);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

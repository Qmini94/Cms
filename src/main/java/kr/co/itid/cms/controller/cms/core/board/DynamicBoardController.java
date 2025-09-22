package kr.co.itid.cms.controller.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.response.FieldDefinitionResponse;
import kr.co.itid.cms.dto.cms.core.common.PaginationOption;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.cms.core.board.DynamicBoardService;
import kr.co.itid.cms.utils.HtmlSanitizer; // HTML 새니타이저 추가
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/back-api/board")
@RequiredArgsConstructor
@Validated
public class DynamicBoardController {

    private final DynamicBoardService dynamicBoardService;
    private final HtmlSanitizer htmlSanitizer; // HTML 새니타이저 의존성 추가

    @PreAuthorize("@permService.hasAccess('ACCESS')")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Map<String, Object>>>> getList(
            @Valid @ModelAttribute SearchOption option,
            @Valid @ModelAttribute PaginationOption pagination,
            BindingResult bindingResult
    ) throws Exception {
        if (bindingResult.hasErrors()) {
            String msg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(ApiResponse.error(400, msg));
        }

        Page<Map<String, Object>> result = dynamicBoardService.getList(option, pagination);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PreAuthorize("@permService.hasAccess('VIEW')")
    @GetMapping("/{idx}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getView(
            @PathVariable Long idx
    ) throws Exception {
        Map<String, Object> data = dynamicBoardService.getOne(idx);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PreAuthorize("@permService.hasAccess('WRITE')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(
            @RequestBody Map<String, Object> body
    ) throws Exception {
        // HTML 콘텐츠가 포함된 필드들에 대해 XSS 방어 처리
        sanitizeHtmlFields(body);
        
        dynamicBoardService.save(null, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @PreAuthorize("@permService.hasAccess('MODIFY', #idx)")
    @PutMapping("/{idx}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long idx,
            @RequestBody Map<String, Object> body
    ) throws Exception {
        // HTML 콘텐츠가 포함된 필드들에 대해 XSS 방어 처리
        sanitizeHtmlFields(body);
        
        dynamicBoardService.save(idx, body);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("@permService.hasAccess('REMOVE', #idx)")
    @DeleteMapping("/{idx}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long idx
    ) throws Exception {
        dynamicBoardService.delete(idx);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/fields")
    public ResponseEntity<ApiResponse<List<FieldDefinitionResponse>>> getFields() throws Exception {
        List<FieldDefinitionResponse> fields = dynamicBoardService.getFieldDefinitions();
        return ResponseEntity.ok(ApiResponse.success(fields));
    }

    /**
     * HTML 콘텐츠가 포함될 수 있는 필드들에 대해 XSS 방어 처리를 수행
     * 
     * @param body 요청 본문 데이터
     */
    private void sanitizeHtmlFields(Map<String, Object> body) {
        // HTML 콘텐츠가 포함될 수 있는 필드명들
        String[] htmlFields = {"content", "description", "summary", "body", "text"};
        
        for (String fieldName : htmlFields) {
            Object value = body.get(fieldName);
            if (value instanceof String) {
                String stringValue = (String) value;
                if (stringValue != null && !stringValue.trim().isEmpty()) {
                    // HTML 새니타이즈 처리
                    String sanitizedValue = htmlSanitizer.sanitize(stringValue);
                    body.put(fieldName, sanitizedValue);
                }
            }
        }
    }
}
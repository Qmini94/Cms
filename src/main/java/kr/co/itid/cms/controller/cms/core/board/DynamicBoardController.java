package kr.co.itid.cms.controller.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.response.FieldDefinitionResponse;
import kr.co.itid.cms.dto.cms.core.common.PaginationOption;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.security.JwtAuthenticatedUser;
import kr.co.itid.cms.service.cms.core.board.DynamicBoardService;
import kr.co.itid.cms.util.LoggingUtil;
import kr.co.itid.cms.util.SecurityUtil;
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
@RequestMapping("/api/board")
@RequiredArgsConstructor
@Validated
public class DynamicBoardController {

    private final DynamicBoardService dynamicBoardService;
    private final LoggingUtil loggingUtil;

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
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getView(
            @PathVariable Long id
    ) throws Exception {
        Map<String, Object> data = dynamicBoardService.getOne(id);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @PreAuthorize("@permService.hasAccess('WRITE')")
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(
            @RequestBody Map<String, Object> body
    ) throws Exception {
        dynamicBoardService.save(null, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null));
    }

    @PreAuthorize("@permService.hasAccess('MODIFY')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) throws Exception {
        dynamicBoardService.save(id, body);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PreAuthorize("@permService.hasAccess('REMOVE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id
    ) throws Exception {
        dynamicBoardService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/fields")
    public ResponseEntity<ApiResponse<List<FieldDefinitionResponse>>> getFields() throws Exception {
        List<FieldDefinitionResponse> fields = dynamicBoardService.getFieldDefinitions();
        return ResponseEntity.ok(ApiResponse.success(fields));
    }
}
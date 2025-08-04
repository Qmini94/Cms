package kr.co.itid.cms.controller.cms.core.render;

import kr.co.itid.cms.dto.cms.core.render.response.RenderResponse;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.cms.core.render.RenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 렌더링 데이터를 제공하는 API 컨트롤러입니다.
 * 사용자의 메뉴 접근 권한에 따라 메뉴 타입(board, contents 등)에 맞는 렌더링 데이터를 반환합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/back-api/render")
public class RenderController {

    private final RenderService renderService;

    /**
     * 현재 사용자의 menuId에 해당하는 메뉴의 타입(type),
     * 타입에 따르는 board 또는 contents key값,
     * 옵션, 사용자에 대한 권한데이터를 렌더링하여 응답합니다.
     *
     * 권한이 있는 사용자만 접근할 수 있으며,
     * JwtAuthenticatedUser에 포함된 menuId를 기준으로 렌더링 데이터를 제공합니다.
     *
     * @return ApiResponse&lt;RenderResponse&gt; 렌더링된 데이터와 타입 정보를 포함한 응답
     * @throws Exception 렌더링 처리 중 오류 발생 시
     */
    @PreAuthorize("@permService.hasAccess('ACCESS')")
    @GetMapping
    public ResponseEntity<ApiResponse<RenderResponse>> render() throws Exception {
        RenderResponse response = renderService.getRenderData();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 상세 보기 권한 확인
     */
    @PreAuthorize("@permService.hasAccess('VIEW')")
    @GetMapping("/view")
    public ResponseEntity<ApiResponse<RenderResponse>> renderView() throws Exception {
        RenderResponse response = renderService.getRenderData(); //TODO: 글 idx값을 가져와서 추가 처리해함.
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 글쓰기 권한 확인
     */
    @PreAuthorize("@permService.hasAccess('WRITE')")
    @GetMapping("/write")
    public ResponseEntity<ApiResponse<Boolean>> checkWriteAccess() {
        return ResponseEntity.ok(ApiResponse.success(true));
    }

    /**
     * 수정 권한 확인
     */
    @PreAuthorize("@permService.hasAccess('MODIFY')")
    @GetMapping("/modify")
    public ResponseEntity<ApiResponse<Boolean>> checkModifyAccess() {
        return ResponseEntity.ok(ApiResponse.success(true));
    }
}

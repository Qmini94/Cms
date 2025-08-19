package kr.co.itid.cms.controller.cms.core.render;

import kr.co.itid.cms.dto.cms.core.render.request.LayoutPreviewRequest;
import kr.co.itid.cms.dto.cms.core.render.request.LayoutSaveRequest;
import kr.co.itid.cms.service.cms.core.page.LayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/back-api/layouts")
@RequiredArgsConstructor
public class LayoutController {

    private final LayoutService layoutService;

    @PostMapping(value="/preview", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> preview(@RequestBody @Valid LayoutPreviewRequest req, HttpServletRequest httpReq) {
        String html = layoutService.renderPreview(req, httpReq);
        HttpHeaders h = new HttpHeaders();
        // 새 창으로 직접 열리므로 frame-ancestors는 none 권장, 스크립트/스타일은 self만 허용
        h.add("Content-Security-Policy",
                "default-src 'self'; img-src 'self' data: https:; " +
                        "style-src 'self' 'unsafe-inline'; script-src 'self'; frame-ancestors 'none';");
        return ResponseEntity.ok().headers(h).body(html);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> save(@RequestBody @Valid LayoutSaveRequest req) {
        Boolean res = layoutService.save(req);
        return ResponseEntity.ok(res);
    }
}
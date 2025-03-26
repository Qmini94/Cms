package kr.co.itid.cms.config.common.exception;

import kr.co.itid.cms.dto.common.ApiResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

/**
 * Spring Boot 기본 /error 경로를 JSON 응답으로 재정의하는 전역 에러 컨트롤러
 */
@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ApiResponse<Void>> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = (statusCode != null) ? statusCode : 500;
        String message = (status == 404) ? "리소스를 찾을 수 없습니다." : "에러가 발생했습니다.";

        return ResponseEntity.status(status)
                .body(ApiResponse.error(status, message));
    }
}

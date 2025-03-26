package kr.co.itid.cms.config.common.exception;

import kr.co.itid.cms.dto.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - 잘못된 요청 (파라미터 오류 등)
    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, "잘못된 요청입니다."));
    }

    // 401 - 인증 실패
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, "인증이 필요합니다."));
    }

    // 403 - 인가 실패
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, "권한이 없습니다."));
    }

    // 404 - 존재하지 않는 URL 또는 리소스 스프링에서 /error 보내버리기 때문에 따로 컨트롤러로 처리
//    @ExceptionHandler(NoHandlerFoundException.class)
//    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoHandlerFoundException e) {
//        return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                .body(ApiResponse.error(404, "리소스를 찾을 수 없습니다."));
//    }

    // 405 - 지원하지 않는 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(405, "지원하지 않는 HTTP 메서드입니다."));
    }

    // 500 - 서버 오류
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAll(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, "서버 오류가 발생했습니다."));
    }
}

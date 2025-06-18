package kr.co.itid.cms.config.exception;

import kr.co.itid.cms.dto.common.ApiResponse;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - 잘못된 요청
    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, extractMessage(e, "잘못된 요청입니다")));
    }

    // 400 - @Valid 사용 시 DTO 유효성 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, errorMessage));
    }

    // 400 - @Validated 사용 시 단일 파라미터 유효성 검증 실패
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, errorMessage));
    }

    // 401 - JWT 인증 오류
    @ExceptionHandler(org.springframework.security.authentication.AuthenticationServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleJwtAuthenticationException(Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, extractMessage(e, "토큰이 유효하지 않거나 만료되었습니다")));
    }

    // 401 - 인증 실패
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(401, extractMessage(e, "인증에 실패하였습니다")));
    }

    // 403 - 인가 실패
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(403, extractMessage(e, "권한이 없습니다")));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(404, extractMessage(e, "Resource not found")));
    }

    // 405 - 지원하지 않는 메서드
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(405, extractMessage(e, "지원하지 않는 HTTP 메서드입니다")));
    }

    // 500 - 비즈니스 예외
    @ExceptionHandler(EgovBizException.class)
    public ResponseEntity<ApiResponse<Void>> handleEgovBizException(EgovBizException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, extractMessage(e, "서버 오류가 발생했습니다")));
    }

    // 500 - 그 외 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(500, extractMessage(e, "예상치 못한 서버 오류")));
    }

    // 503 - 데이터베이스 접근 예외
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccess(DataAccessException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE) // 503
                .body(ApiResponse.error(503, extractMessage(e, "데이터베이스 접근 오류")));
    }

    /**
     * 예외 메시지 추출 메서드
     */
    private String extractMessage(Exception e, String defaultMessage) {
        if (e instanceof EgovBizException) {
            EgovBizException bizEx = (EgovBizException) e;
            String msg = bizEx.getMessageKey();
            Throwable cause = e.getCause();
            if (cause != null) {
                return defaultMessage + ": " + msg;
            }
            return defaultMessage + ": " + msg;
        }

        return defaultMessage + ": " + (e.getMessage() != null ? e.getMessage() : "");
    }
}
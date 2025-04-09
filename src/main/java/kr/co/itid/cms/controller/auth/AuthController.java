package kr.co.itid.cms.controller.auth;

import kr.co.itid.cms.config.security.JwtTokenProvider;
import kr.co.itid.cms.dto.auth.LoginRequest;
import kr.co.itid.cms.dto.auth.TokenResponse;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static kr.co.itid.cms.config.security.SecurityConstants.*;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;

/**
 * 인증(Authentication) 관련 API를 처리하는 컨트롤러입니다.
 * 로그인, 로그아웃, 토큰 갱신 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    /**
     * 로그인 요청을 처리합니다.
     *
     * @param request 사용자 로그인 요청 (아이디, 비밀번호 포함)
     * @return 로그인 성공 시 토큰 응답 및 쿠키 설정, 실패 시 에러 응답
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody LoginRequest request) {
        try {
            TokenResponse tokenResponse = authService.login(request.getUserId(), request.getPassword());

            // 쿠키 설정
            ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, tokenResponse.getAccessToken())
                    .httpOnly(HTTP_ONLY)
                    .secure(SECURE)
                    .path("/")
                    .maxAge(Duration.ofSeconds(accessTokenValidity))
                    .sameSite(SAME_SITE_STRICT)
                    .build();

            ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, tokenResponse.getRefreshToken())
                    .httpOnly(HTTP_ONLY)
                    .secure(SECURE)
                    .path("/")
                    .maxAge(Duration.ofSeconds(refreshTokenValidity))
                    .sameSite(SAME_SITE_STRICT)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(ApiResponse.success(tokenResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "로그인 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 로그아웃 요청을 처리합니다.
     *
     * @param token Authorization 헤더에 담긴 액세스 토큰
     * @return 로그아웃 성공 여부 응답
     * @throws Exception 로그아웃 처리 중 발생한 예외
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String token) throws Exception {
        authService.logout(token.replace(TOKEN_PREFIX, ""));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 리프레시 토큰을 이용해 액세스 토큰을 갱신합니다.
     *
     * @param request HttpServletRequest 객체로부터 쿠키 또는 헤더에서 리프레시 토큰을 추출
     * @return 갱신된 액세스 토큰 응답, 실패 시 에러 응답
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(HttpServletRequest request) {
        try {
            String refreshToken = jwtTokenProvider.resolveRefreshToken(request);

            if (refreshToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(401, "리프레시 토큰이 존재하지 않습니다."));
            }

            TokenResponse newTokenResponse = authService.refreshAccessToken(refreshToken);

            ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, newTokenResponse.getAccessToken())
                    .httpOnly(HTTP_ONLY)
                    .secure(SECURE)
                    .path("/")
                    .maxAge(Duration.ofSeconds(accessTokenValidity))
                    .sameSite(SAME_SITE_STRICT)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                    .body(ApiResponse.success(newTokenResponse));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "토큰 갱신 중 오류 발생: " + e.getMessage()));
        }
    }
}

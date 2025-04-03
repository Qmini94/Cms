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

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;

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

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody LoginRequest request) {
        try {
            TokenResponse tokenResponse = authService.login(request.getUserId(), request.getPassword());

            // 쿠키 설정
            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", tokenResponse.getAccessToken())
                    .httpOnly(true)
                    .secure(false) // 개발 환경에서는 false
                    .path("/")
                    .maxAge(Duration.ofSeconds(accessTokenValidity))
                    .sameSite("Strict")
                    .build();

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", tokenResponse.getRefreshToken())
                    .httpOnly(true)
                    .secure(false) // 개발 환경에서는 false
                    .path("/")
                    .maxAge(Duration.ofSeconds(refreshTokenValidity))
                    .sameSite("Strict")
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(ApiResponse.success(tokenResponse));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "로그인 중 오류 발생: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token.replace("Bearer ", ""));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(HttpServletRequest request) {
        try {
            // 1. 클라이언트 쿠키에서 Refresh Token 가져오기
            String refreshToken = jwtTokenProvider.resolveRefreshToken(request);

            // 2. Refresh Token이 없는 경우 처리
            if (refreshToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error(401, "리프레시 토큰이 존재하지 않습니다."));
            }

            // 3. 토큰 갱신 요청
            TokenResponse newTokenResponse = authService.refreshAccessToken(refreshToken);

            // 4. Access Token 쿠키 설정
            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", newTokenResponse.getAccessToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(Duration.ofSeconds(accessTokenValidity))
                    .sameSite("Strict")
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
package kr.co.itid.cms.controller.auth;

import kr.co.itid.cms.config.security.JwtTokenProvider;
import kr.co.itid.cms.dto.auth.LoginRequest;
import kr.co.itid.cms.dto.auth.TokenResponse;
import kr.co.itid.cms.dto.auth.UserInfoResponse;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException; // ★
import org.springframework.security.core.userdetails.UsernameNotFoundException; // ★
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 인증(Authentication) 관련 API를 처리하는 컨트롤러입니다.
 * 로그인, 로그아웃, 사용자 정보 조회 기능을 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/back-api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 로그인: 서비스가 반환한 access/refresh 토큰을 각각 HttpOnly 쿠키로 세팅
     * (CSRF는 Security 설정에서 /back-api/auth/login만 예외)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody LoginRequest request,
                                                            HttpServletResponse response) {
        try {
            TokenResponse tokenResponse = authService.login(request.getUserId(), request.getPassword());

            // ACCESS 쿠키
            ResponseCookie access = jwtTokenProvider.createAccessTokenCookie(tokenResponse.getAccessToken());
            response.addHeader(HttpHeaders.SET_COOKIE, access.toString());

            // REFRESH 쿠키
            if (tokenResponse.getRefreshToken() != null) {
                ResponseCookie refresh = jwtTokenProvider.createRefreshTokenCookie(tokenResponse.getRefreshToken());
                response.addHeader(HttpHeaders.SET_COOKIE, refresh.toString());
            }

            return ResponseEntity.ok(ApiResponse.success(tokenResponse));

            // ★ 인증 실패는 401로 구분
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "인증 실패: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "로그인 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 현재 로그인된 사용자 정보 반환
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> me(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(authService.getUserInfoFromToken(request)));
    }

    /**
     * 로그아웃: 서비스 처리 + ACCESS/REFRESH 쿠키 삭제
     */
    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) throws Exception {
        authService.logout();

        // 발급 시와 동일 속성으로 삭제 쿠키를 JwtTokenProvider에서 생성
        ResponseCookie delAccess = jwtTokenProvider.deleteAccessTokenCookie();
        ResponseCookie delRefresh = jwtTokenProvider.deleteRefreshTokenCookie();

        response.addHeader(HttpHeaders.SET_COOKIE, delAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, delRefresh.toString());

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
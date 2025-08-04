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
     * 로그인 요청을 처리합니다.
     *
     * @param request 사용자 로그인 요청 (아이디, 비밀번호 포함)
     * @return 로그인 성공 시 토큰 응답 및 쿠키 설정, 실패 시 에러 응답
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody LoginRequest request,
                                                            HttpServletResponse response) {
        try {
            TokenResponse tokenResponse = authService.login(request.getUserId(), request.getPassword());

            // HttpOnly 쿠키로 AccessToken 설정
            ResponseCookie accessTokenCookie = jwtTokenProvider.createAccessTokenCookie(tokenResponse.getAccessToken());
            response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

            return ResponseEntity.ok(ApiResponse.success(tokenResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "로그인 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 현재 로그인된 사용자 정보를 반환합니다.
     *
     * @param request 현재 요청 객체
     * @return 사용자 이름, 역할, 인덱스를 담은 응답
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> me(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(ApiResponse.success(authService.getUserInfoFromToken(request)));
    }

    /**
     * 로그아웃 요청을 처리합니다.
     *
     * 클라이언트는 HttpOnly 쿠키에 저장된 ACCESS_TOKEN을 자동으로 전송합니다.
     * 이 메서드는 해당 쿠키에서 토큰 값을 추출하여 로그아웃 처리 (블랙리스트 등록 등)를 수행합니다.
     *
     * @return 로그아웃 성공 여부를 나타내는 응답 객체
     * @throws Exception 로그아웃 처리 중 오류가 발생한 경우
     */
    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() throws Exception {
        authService.logout();

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
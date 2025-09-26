package kr.co.itid.cms.controller.auth;

import kr.co.itid.cms.config.security.JwtTokenProvider;
import kr.co.itid.cms.config.security.model.JwtProperties;
import kr.co.itid.cms.dto.auth.LoginRequest;
import kr.co.itid.cms.dto.auth.TokenResponse;
import kr.co.itid.cms.dto.auth.UserInfoResponse;
import kr.co.itid.cms.dto.common.ApiResponse;
import kr.co.itid.cms.service.auth.AuthService;
import kr.co.itid.cms.util.AuthCookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Claims;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * 인증(Authentication) 관련 API 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/back-api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    /**
     * 로그인
     * 1) 서비스에 인증 위임 → 토큰 발급
     * 2) 발급 토큰을 HttpOnly 쿠키로 세팅 (TTL은 yml 값 사용)
     * 3) 세션 만료(=SID TTL 기준) 값을 헤더/쿠키로 내려 클라이언트 타이머와 동기화
     * 4) ACCESS 토큰의 Claims를 서비스에 전달해 사용자 정보 DTO 생성(/auth/me와 동일 스키마)
     * (CSRF: Security에서 /back-api/auth/login 예외 처리)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserInfoResponse>> login(@RequestBody LoginRequest request,
                                                            HttpServletResponse response) {
        try{
            TokenResponse tokenResponse = authService.login(request.getUserId(), request.getPassword());

            // ACCESS: yml TTL
            AuthCookieUtil.setAccessToken(response, tokenResponse.getAccessToken(),
                    Duration.ofSeconds(jwtProperties.getAccessTokenValidity()));

            // REFRESH: yml TTL
            if (StringUtils.hasText(tokenResponse.getRefreshToken())) {
                AuthCookieUtil.setRefreshToken(response, tokenResponse.getRefreshToken(),
                        Duration.ofSeconds(jwtProperties.getRefreshTokenValidity()));
            }

            // 세션 만료(SessionExp)는 SID 기준 → 로그인 직후엔 세션 생성 시각 + sessionTtlSeconds
            long sessionExpEpoch = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
                    .plusSeconds(jwtProperties.getSessionTtlSeconds()).toEpochSecond();
            AuthCookieUtil.setSessionExpires(response, sessionExpEpoch);

            Claims claims = jwtTokenProvider.getClaimsFromToken(tokenResponse.getAccessToken());
            UserInfoResponse info = authService.getUserInfoFromToken(claims);

            return ResponseEntity.ok(ApiResponse.success(info));
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "인증 실패: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "로그인 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 현재 로그인 사용자 정보 + 만료 헤더/쿠키 최신화
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> me() throws Exception {
        UserInfoResponse info = authService.getCurrentUserInfo();

        return ResponseEntity.ok(ApiResponse.success(info));
    }

    /**
     * 로그아웃: 서버 세션 정리 + 모든 인증 쿠키 삭제
     */
    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        try {
            authService.logout(); // 내부에서 세션 제거/무효화
        } catch (Exception ignore) {
            // 로그아웃은 idempotent하게
        }

        // ACCESS/REFRESH/SESSION-EXPIRES 모두 삭제 + X-Session-Expires: 0
        AuthCookieUtil.clearAll(response);

        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

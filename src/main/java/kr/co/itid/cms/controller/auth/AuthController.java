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

import javax.servlet.http.HttpServletRequest;
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
     * 로그인: 토큰 발급 + 쿠키 세팅 + 만료 헤더 세팅
     * (CSRF: Security에서 /back-api/auth/login 예외 처리)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody LoginRequest request,
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

            return ResponseEntity.ok(ApiResponse.success(tokenResponse));
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
    public ResponseEntity<ApiResponse<UserInfoResponse>> me(HttpServletRequest request,
                                                            HttpServletResponse response) throws Exception {
        // 유저 정보
        UserInfoResponse info = authService.getUserInfoFromToken(request);

        // 만료 정보 갱신(가능하면 커스텀 exp, 없으면 표준 exp 사용)
        String access = jwtTokenProvider.extractAccessTokenFromRequest(request);
        if (StringUtils.hasText(access)) {
            Claims claims = jwtTokenProvider.getClaimsFromToken(access);
            Long customExp = claims.get("exp", Long.class);
            long expEpoch = (customExp != null)
                    ? customExp
                    : claims.getExpiration().toInstant().getEpochSecond();
            AuthCookieUtil.setSessionExpires(response, expEpoch);
        }

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

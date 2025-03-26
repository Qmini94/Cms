package kr.co.itid.cms.controller.auth;

import kr.co.itid.cms.dto.auth.LoginRequest;
import kr.co.itid.cms.dto.auth.TokenResponse;
import kr.co.itid.cms.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        // 1. 로그인 처리 (ID/PW 검증 → 토큰 생성)
        TokenResponse tokenResponse = authService.login(request.getUserId(), request.getPassword());

        // 2. 토큰 바디로 내려줌 (Access + Refresh)
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token.replace("Bearer ", ""));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        TokenResponse tokenResponse = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(tokenResponse);
    }
}


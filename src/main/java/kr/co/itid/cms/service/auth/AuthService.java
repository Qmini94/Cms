package kr.co.itid.cms.service.auth;

import kr.co.itid.cms.config.security.JwtTokenProvider;
import kr.co.itid.cms.dto.auth.TokenResponse;
import kr.co.itid.cms.entity.cms.Member;
import kr.co.itid.cms.repository.cms.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final PasswordEncoder passwordEncoder;

    public TokenResponse login(String userId, String password) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(password, member.getUserPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        Map<String, String> claims = jwtTokenProvider.getClaims(member);
        String accessToken = jwtTokenProvider.generateToken(userId, claims, false);
        String refreshToken = jwtTokenProvider.generateToken(userId, claims, true);

        // Redis에 Refresh Token 저장
        jwtTokenProvider.storeRefreshToken(userId, refreshToken);

        // 로그인 시간 갱신
        member.setLastLoginDate(LocalDateTime.now());
        memberRepository.save(member);

        return new TokenResponse(accessToken, refreshToken);
    }

    public void logout(String token) {
        String userId = jwtTokenProvider.getUserId(token);
        long expiration = jwtTokenProvider.getExpiration(token);
        tokenBlacklistService.blockUserSession(token, userId, expiration);
    }

    public TokenResponse refreshAccessToken(String refreshToken) {
        try {
            // 1. 토큰에서 userId 추출 후 해당 member 조회
            String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            Member member = memberRepository.findByUserId(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다."));

            // 2. Redis에서 Refresh Token 유효성 검사
            if (!jwtTokenProvider.validateRefreshToken(userId, refreshToken)) {
                throw new BadCredentialsException("유효하지 않은 리프레시 토큰입니다.");
            }

            // 3. 사용자 정보로 새로운 Access Token 발급
            Map<String, String> claims = jwtTokenProvider.getClaims(member);
            String accessToken = jwtTokenProvider.generateToken(userId, claims, false);

            return new TokenResponse(accessToken, null);
        } catch (Exception e) {
            throw new RuntimeException("토큰 갱신 중 오류 발생: " + e.getMessage());
        }
    }

    public void forceLogoutByAdmin(String userId, @Nullable String token) {
        if (token != null) {
            long expiration = jwtTokenProvider.getExpiration(token);
            tokenBlacklistService.blockUserSession(token, userId, expiration);
        } else {
            tokenBlacklistService.removeRefreshToken(userId);
        }
    }
}


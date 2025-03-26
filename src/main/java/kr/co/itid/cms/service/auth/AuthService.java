package kr.co.itid.cms.service.auth;

import kr.co.itid.cms.config.security.JwtTokenProvider;
import kr.co.itid.cms.dto.auth.TokenResponse;
import kr.co.itid.cms.entity.Member;
import kr.co.itid.cms.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;

    public TokenResponse login(String userId, String password) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(password, member.getUserPassword())) {
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        List<String> roles = resolveRoles(member.getUserLevel());
        String accessToken = jwtTokenProvider.generateToken(userId, roles, false);
        String refreshToken = jwtTokenProvider.generateToken(userId, roles, true);

        // Redis에 Refresh Token 저장 (1일)
        redisTemplate.opsForValue().set("refresh:" + userId, refreshToken, 1, TimeUnit.DAYS);

        // 로그인 시간 갱신
        member.setLastLoginDate(LocalDateTime.now());
        memberRepository.save(member);

        return new TokenResponse(accessToken, refreshToken);
    }

    private List<String> resolveRoles(Integer level) {
        if (level == 1) return List.of("ROLE_ADMIN");
        else if (level == 6) return List.of("ROLE_STAFF");
        else return List.of("ROLE_USER");
    }

    public void logout(String token) {
        String userId = jwtTokenProvider.getUserId(token);
        long expiration = jwtTokenProvider.getExpiration(token);
        tokenBlacklistService.blockUserSession(token, userId, expiration);
    }

    public TokenResponse refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.isRefreshTokenValid(refreshToken)) {
            throw new BadCredentialsException("유효하지 않은 리프레시 토큰입니다.");
        }

        String userId = jwtTokenProvider.getUserId(refreshToken);
        String storedRefreshToken = redisTemplate.opsForValue().get("refresh:" + userId);

        // Redis에 저장된 리프레시 토큰과 일치하는지 확인
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new BadCredentialsException("이미 만료되었거나 저장되지 않은 토큰입니다.");
        }

        // 새 Access Token만 발급
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다."));

        List<String> roles = resolveRoles(member.getUserLevel());
        String newAccessToken = jwtTokenProvider.generateToken(userId, roles, false);

        return new TokenResponse(newAccessToken, null); // refreshToken은 그대로
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


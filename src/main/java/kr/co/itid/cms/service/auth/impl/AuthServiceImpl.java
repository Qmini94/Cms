package kr.co.itid.cms.service.auth.impl;

import kr.co.itid.cms.config.security.JwtTokenProvider;
import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.dto.auth.TokenResponse;
import kr.co.itid.cms.dto.auth.UserInfoResponse;
import kr.co.itid.cms.entity.cms.core.Member;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.repository.cms.core.MemberRepository;
import kr.co.itid.cms.service.auth.AuthService;
import kr.co.itid.cms.util.LoggingUtil;
import kr.co.itid.cms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

@Service("authService")
@RequiredArgsConstructor
public class AuthServiceImpl extends EgovAbstractServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final LoggingUtil loggingUtil;

    @Override
    public TokenResponse login(String userId, String password) throws Exception {
        loggingUtil.logAttempt(Action.LOGIN, "Try login: " + userId);

        try {
            Member member = memberRepository.findByUserId(userId)
                    .orElseThrow(() -> {
                        loggingUtil.logFail(Action.LOGIN, "User not found: " + userId);
                        return processException("User not found", new UsernameNotFoundException("User not found"));
                    });

            if (!passwordEncoder.matches(password, member.getUserPassword())) {
                loggingUtil.logFail(Action.LOGIN, "Wrong password: " + userId);
                throw processException("Wrong password", new BadCredentialsException("Invalid password"));
            }

            Map<String, Object> claims = jwtTokenProvider.getClaims(member);
            String accessToken = jwtTokenProvider.createToken(userId, claims);

            member.setLastLoginDate(LocalDateTime.now());
            memberRepository.save(member);

            loggingUtil.logSuccess(Action.LOGIN, "Login success: " + userId);
            return new TokenResponse(accessToken);
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            loggingUtil.logFail(Action.LOGIN, "Login error: " + e.getMessage());
            throw processException("Login error", e);
        }
    }

// TODO: [보완 예정] user_tokens:{userId} 에서 제거된 jti를 별도 expired_jti:{jti} 키에 백업하도록 개선 고려
//       - TTL(accessTokenValidity)과 함께 저장하여 만료되도록 설정
//       - isBlacklisted() 메서드에서 blacklist:{jti} 외에 expired_jti:{jti}도 함께 검사
//       - 현재는 최대 20개 jti만 유지되므로, 오래된 토큰이 살아있을 수 있음
//       - 실무적으로 큰 문제는 없지만, 보안 커버리지를 높이기 위한 선택 사항
//       - 장기적으로 Set → List 구조로 변경하여 jti 정렬 및 관리 방식 개선 여부도 판단 필요

    @Override
    public void logout() throws Exception {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        String token = user.token();

        loggingUtil.logAttempt(Action.LOGOUT, "Try logout: " + token);

        try {
            String userId = jwtTokenProvider.getUserId(token);
            jwtTokenProvider.addAllTokensToBlacklist(userId); // 블랙리스트 등록
            loggingUtil.logSuccess(Action.LOGOUT, "Logout success: " + userId);
        } catch (BadCredentialsException e) {
            loggingUtil.logFail(Action.LOGOUT, "Invalid token: " + token);
            throw processException("Invalid token", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.LOGOUT, "Logout error: " + e.getMessage());
            throw processException("Logout error", e);
        }
    }

    @Override
    public void forceLogoutByAdmin(String userId, @Nullable String token) throws Exception {
        loggingUtil.logAttempt(Action.FORCE, "Try force logout: " + userId);

        try {
            if (token != null) {
                jwtTokenProvider.addAllTokensToBlacklist(userId);
                loggingUtil.logSuccess(Action.FORCE, "Force logout with token: " + userId);
            } else {
                loggingUtil.logSuccess(Action.FORCE, "Force logout without token: " + userId);
            }
        } catch (Exception e) {
            loggingUtil.logFail(Action.FORCE, "Force logout error: " + e.getMessage());
            throw processException("Force logout error", e);
        }
    }

    @Override
    public UserInfoResponse getUserInfoFromToken(HttpServletRequest request) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try get user info from token");

        try {
            JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();

            if (user.isGuest()) {
                return new UserInfoResponse(null, null, null, null);
            }

            loggingUtil.logSuccess(Action.RETRIEVE, "User info retrieved from token");
            return new UserInfoResponse(
                    user.userName(),
                    user.userId(),
                    String.valueOf(user.userLevel()),
                    user.userIdx().intValue()
            );
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Failed to get user info: " + e.getMessage());
            throw processException("Failed to retrieve user info", e);
        }
    }
}
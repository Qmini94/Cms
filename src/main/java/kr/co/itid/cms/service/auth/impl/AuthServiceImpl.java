package kr.co.itid.cms.service.auth.impl;

import kr.co.itid.cms.config.security.JwtTokenProvider;
import kr.co.itid.cms.dto.auth.TokenResponse;
import kr.co.itid.cms.entity.cms.Member;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.repository.cms.MemberRepository;
import kr.co.itid.cms.service.auth.AuthService;
import kr.co.itid.cms.service.auth.TokenBlacklistService;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service("authService")
@RequiredArgsConstructor
public class AuthServiceImpl extends EgovAbstractServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
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
            String accessToken = jwtTokenProvider.generateToken(userId, claims, false);
            String refreshToken = jwtTokenProvider.generateToken(userId, claims, true);

            jwtTokenProvider.storeRefreshToken(userId, refreshToken);
            member.setLastLoginDate(LocalDateTime.now());
            memberRepository.save(member);

            loggingUtil.logSuccess(Action.LOGIN, "Login success: " + userId);
            return new TokenResponse(accessToken, refreshToken);
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            loggingUtil.logFail(Action.LOGIN, "Login error: " + e.getMessage());
            throw processException("Login error", e);
        }
    }

    @Override
    public void logout(String token) throws Exception {
        loggingUtil.logAttempt(Action.LOGOUT, "Try logout: " + token);

        try {
            String userId = jwtTokenProvider.getUserId(token);
            long expiration = jwtTokenProvider.getExpiration(token);
            tokenBlacklistService.blockUserSession(token, userId, expiration);

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
    public TokenResponse refreshAccessToken(String refreshToken) throws Exception {
        loggingUtil.logAttempt(Action.REFRESH, "Try refresh token");

        try {
            String userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            Member member = memberRepository.findByUserId(userId)
                    .orElseThrow(() -> {
                        loggingUtil.logFail(Action.REFRESH, "User not found: " + userId);
                        return processException("User not found", new UsernameNotFoundException("User not found"));
                    });

            if (!jwtTokenProvider.validateRefreshToken(userId, refreshToken)) {
                loggingUtil.logFail(Action.REFRESH, "Invalid refresh token: " + userId);
                throw processException("Invalid refresh token", new BadCredentialsException("Invalid refresh token"));
            }

            Map<String, Object> claims = jwtTokenProvider.getClaims(member);
            String accessToken = jwtTokenProvider.generateToken(userId, claims, false);

            loggingUtil.logSuccess(Action.REFRESH, "Token refresh success: " + userId);
            return new TokenResponse(accessToken, null);
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            loggingUtil.logFail(Action.REFRESH, "Token refresh error: " + e.getMessage());
            throw processException("Token refresh error", e);
        }
    }

    @Override
    public void forceLogoutByAdmin(String userId, @Nullable String token) throws Exception {
        loggingUtil.logAttempt(Action.FORCE, "Try force logout: " + userId);

        try {
            if (token != null) {
                long expiration = jwtTokenProvider.getExpiration(token);
                tokenBlacklistService.blockUserSession(token, userId, expiration);
                loggingUtil.logSuccess(Action.FORCE, "Force logout with token: " + userId);
            } else {
                tokenBlacklistService.removeRefreshToken(userId);
                loggingUtil.logSuccess(Action.FORCE, "Force logout without token: " + userId);
            }
        } catch (Exception e) {
            loggingUtil.logFail(Action.FORCE, "Force logout error: " + e.getMessage());
            throw processException("Force logout error", e);
        }
    }
}
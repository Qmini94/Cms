package kr.co.itid.cms.service.auth.impl;

import kr.co.itid.cms.config.security.JwtTokenProvider;
import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.dto.auth.TokenResponse;
import kr.co.itid.cms.dto.auth.UserInfoResponse;
import kr.co.itid.cms.entity.cms.base.Member;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.repository.cms.MemberRepository;
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

    @Override
    public void logout() throws Exception {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        String token = user.token();

        loggingUtil.logAttempt(Action.LOGOUT, "Try logout: " + token);

        try {
            String userId = jwtTokenProvider.getUserId(token);
            jwtTokenProvider.addToBlacklist(token); // 블랙리스트 등록
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
                jwtTokenProvider.addToBlacklist(token);
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
                return new UserInfoResponse(null, null, null);
            }

            loggingUtil.logSuccess(Action.RETRIEVE, "User info retrieved from token");
            return new UserInfoResponse(
                    user.userName(),
                    String.valueOf(user.userLevel()),
                    user.userIdx().intValue()
            );
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Failed to get user info: " + e.getMessage());
            throw processException("Failed to retrieve user info", e);
        }
    }
}
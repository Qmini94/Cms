package kr.co.itid.cms.service.auth.impl;

import kr.co.itid.cms.config.security.JwtTokenProvider;
import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.dto.auth.TokenResponse;
import kr.co.itid.cms.dto.auth.UserInfoResponse;
import kr.co.itid.cms.entity.cms.core.member.Member;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.repository.cms.core.member.MemberRepository;
import kr.co.itid.cms.service.auth.AuthService;
import kr.co.itid.cms.service.auth.SessionManager;
import kr.co.itid.cms.util.LoggingUtil;
import kr.co.itid.cms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

@Service("authService")
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl extends EgovAbstractServiceImpl implements AuthService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final LoggingUtil loggingUtil;
    private final SessionManager sessionManager;

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
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

            // 1. Redis 세션 생성
            String hostname = SecurityUtil.getCurrentUser() != null ? 
                SecurityUtil.getCurrentUser().hostname() : "unknown";
            String sessionId = sessionManager.createSession(member, hostname);
            
            // 2. JWT 토큰 생성 (세션 ID 포함)
            Map<String, Object> claims = jwtTokenProvider.getClaims(member, sessionId);
            String accessToken = jwtTokenProvider.createToken(userId, claims);

            member.setLastLoginDate(LocalDateTime.now());
            memberRepository.save(member);

            loggingUtil.logSuccess(Action.LOGIN, "Login success: " + userId);
            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .build();
        } catch (IllegalArgumentException e) {
            loggingUtil.logFail(Action.CREATE, "입력값 오류: " + e.getMessage());
            throw processException("Invalid input detected", e);
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            loggingUtil.logFail(Action.LOGIN, "Login error: " + e.getMessage());
            throw processException("Login error", e);
        }
    }

    @Override
    public UserInfoResponse getUserInfoFromToken(HttpServletRequest request) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try get user info from token");

        try {
            JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();

            if (user.isGuest()) {
                return UserInfoResponse.builder()
                        .idx(-1)
                        .build();
            }

            // 세션 TTL 조회 (Redis 기반 idle 시간)
            long idleRemainSec = -1;
            String sessionId = user.sessionId();
            if (sessionId != null) {
                idleRemainSec = sessionManager.getSessionTtl(sessionId);
            }

            loggingUtil.logSuccess(Action.RETRIEVE, "User info retrieved from token");
            return UserInfoResponse.builder()
                    .userName(user.userName())
                    .userId(user.userId())
                    .level(String.valueOf(user.userLevel()))
                    .idx(user.userIdx().intValue())
                    .exp(user.exp())
                    .idleRemainSec(idleRemainSec)  // Redis TTL 기반 idle 시간 추가
                    .build();

        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Failed to get user info: " + e.getMessage());
            throw processException("Failed to retrieve user info", e);
        }
    }

    @Override
    public void logout() throws Exception {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        String token = user.token();

        loggingUtil.logAttempt(Action.LOGOUT, "Try logout: " + token);

        try {
            // 1. JWT 블랙리스트 추가
            String userJti = jwtTokenProvider.getJti(token);
            jwtTokenProvider.addTokenToBlacklist(userJti);
            
            // 2. Redis 세션 삭제
            String sessionId = user.sessionId();
            if (sessionId != null) {
                sessionManager.deleteSession(sessionId);
                loggingUtil.logSuccess(Action.LOGOUT, "Session deleted: " + sessionId);
            }
            
            loggingUtil.logSuccess(Action.LOGOUT, "Logout success: " + user.userId());
        } catch (BadCredentialsException e) {
            loggingUtil.logFail(Action.LOGOUT, "Invalid token: " + token);
            throw processException("Invalid token", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.LOGOUT, "Logout error: " + e.getMessage());
            throw processException("Logout error", e);
        }
    }
}
package kr.co.itid.cms.service.auth.impl;

import io.jsonwebtoken.Claims;
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
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

@Service("authService")
@RequiredArgsConstructor
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

            // 1) Redis 세션 생성 (세션 권위)
            String hostname = SecurityUtil.getCurrentUser() != null ?
                    SecurityUtil.getCurrentUser().hostname() : "unknown";
            String sessionId = sessionManager.createSession(member, hostname);

            // 2) ACCESS 토큰 생성 (sid 포함)
            boolean isRedisDown = !sessionManager.isRedisHealthy();
            Map<String, Object> claims = jwtTokenProvider.getClaims(member, sessionId);
            String accessToken = jwtTokenProvider.createToken(userId, claims, isRedisDown); // 장애면 fallback TTL

            // 3) REFRESH 토큰 생성 (sid 포함)
            String refreshToken = jwtTokenProvider.createRefreshToken(member.getUserId(), sessionId);

            // 4) 최종 처리
            member.setLastLoginDate(LocalDateTime.now());
            memberRepository.save(member);

            loggingUtil.logSuccess(Action.LOGIN, "Login success: " + userId);

            // 컨트롤러에서 Set-Cookie 헤더로 내려줄 수 있도록 토큰 반환(기존 계약 유지)
            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
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
    public UserInfoResponse getCurrentUserInfo() throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try get user info from token");

        try {
            JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();

            if (user.isGuest()) {
                return UserInfoResponse.builder()
                        .idx(-1)
                        .build();
            }

            // 세션 TTL 조회 (idle 남은 시간)
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
                    .idleRemainSec(idleRemainSec)
                    .build();

        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Failed to get user info: " + e.getMessage());
            throw processException("Failed to retrieve user info", e);
        }
    }

    @Override
    public UserInfoResponse getUserInfoFromToken(Claims claims) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try get user info from token");
        try {
            String userId   = claims.getSubject();
            Long   idxLong  = getLong(claims, "idx");
            String userName = getString(claims, "userName");
            Integer level   = getInt(claims, "userLevel");
            String sid      = getString(claims, "sid");

            long expEpoch       = resolveExpEpoch(claims);
            long idleRemainSec  = safeGetSessionTtl(sid);

            loggingUtil.logSuccess(Action.RETRIEVE, "User info retrieved from token");

            return UserInfoResponse.builder()
                    .userId(userId)
                    .idx(idxLong != null ? idxLong.intValue() : null)
                    .userName(userName)
                    .level(level != null ? String.valueOf(level) : null) // DTO가 Integer면 level 로 교체
                    .exp(expEpoch)
                    .idleRemainSec(idleRemainSec)
                    .build();

        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Failed to get user info from token: " + e.getMessage());
            throw processException("Failed to retrieve user info from token", e);
        }
    }

    @Override
    public void logout() throws Exception {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        String token = (user != null) ? user.token() : null;

        loggingUtil.logAttempt(Action.LOGOUT, "Try logout: " + token);

        try {
            // 블랙리스트 미사용: 즉시 철회는 세션 삭제로 달성
            String sessionId = (user != null) ? user.sessionId() : null;
            if (sessionId != null) {
                sessionManager.deleteSession(sessionId); // 즉시 무효화
                loggingUtil.logSuccess(Action.LOGOUT, "Session deleted: " + sessionId);
            }

            loggingUtil.logSuccess(Action.LOGOUT, "Logout success: " + (user != null ? user.userId() : "unknown"));
        } catch (BadCredentialsException e) {
            loggingUtil.logFail(Action.LOGOUT, "Invalid token: " + token);
            throw processException("Invalid token", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.LOGOUT, "Logout error: " + e.getMessage());
            throw processException("Logout error", e);
        }
    }

    /* ===== 안전 추출 헬퍼 ===== */

    // 중첩 3항 제거: 명시적 분기
    private long resolveExpEpoch(Claims claims) {
        Long customExp = getLong(claims, "exp");
        if (customExp != null) {
            return customExp;
        }
        Date stdExp = claims.getExpiration();
        if (stdExp != null) {
            return stdExp.toInstant().getEpochSecond();
        }
        return 0L;
    }

    // 중첩 try 제거: 별도 메서드로 캡슐화
    private long safeGetSessionTtl(String sid) {
        if (sid == null) return -1;
        try {
            return sessionManager.getSessionTtl(sid);
        } catch (Exception ignore) {
            return -1;
        }
    }

    private static Long getLong(Claims claims, String key) {
        Number n = claims.get(key, Number.class);
        return n != null ? n.longValue() : null;
    }
    private static Integer getInt(Claims claims, String key) {
        Number n = claims.get(key, Number.class);
        return n != null ? n.intValue() : null;
    }
    private static String getString(Claims claims, String key) {
        Object v = claims.get(key);
        return v != null ? String.valueOf(v) : null;
    }
}
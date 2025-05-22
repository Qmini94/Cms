package kr.co.itid.cms.service.common.impl;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.service.common.VisitorService;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;

import static kr.co.itid.cms.constanrt.RedisConstants.PERMISSION_TTL;

@Service("visitorService")
@RequiredArgsConstructor
public class VisitorServiceImpl extends EgovAbstractServiceImpl implements VisitorService {

    private final StringRedisTemplate redisTemplate;
    private final LoggingUtil loggingUtil;

    @Override
    public void checkAndCountVisitor(HttpServletRequest request) throws Exception {
        loggingUtil.logAttempt(Action.CREATE, "Try to count visitor");

        try {
            String ip = getClientIp(request);
            String deviceType = isMobile(request.getHeader("User-Agent")) ? "mobile" : "web";
            String today = LocalDate.now().toString();
            String hostname = getHostnameFromAuthentication();

            String visitKey = String.format("visitor:%s:%s:%s", ip, today, hostname);
            String countKey = String.format("visitors:daily:%s:%s:%s", today, hostname, deviceType);

            Boolean isNew = redisTemplate.opsForValue().setIfAbsent(visitKey, "1", PERMISSION_TTL);

            if (Boolean.TRUE.equals(isNew)) {
                redisTemplate.opsForValue().increment(countKey);
            }

            loggingUtil.logSuccess(Action.CREATE, "Visitor counted: " + ip + " (" + deviceType + ")");
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.CREATE, "Redis error: " + e.getMessage());
            throw processException("Redis error. " + e.getMessage(), e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.CREATE, "Unexpected error: " + e.getMessage());
            throw processException("Visitor count failed. " + e.getMessage(), e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return (xfHeader != null && !xfHeader.isEmpty()) ? xfHeader.split(",")[0].trim() : request.getRemoteAddr();
    }

    private boolean isMobile(String userAgent) {
        return userAgent != null && userAgent.toLowerCase().matches(".*(iphone|android|mobile).*");
    }

    private String getHostnameFromAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof JwtAuthenticatedUser user) {
            return user.hostname();
        }
        return "unknown";
    }
}
package kr.co.itid.cms.util;

import kr.co.itid.cms.enums.Action;
import org.egovframe.rte.fdl.cmmn.trace.LeaveaTrace;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LoggingUtil {

    private final LeaveaTrace leaveaTrace;

    /**
     * 성공 로그 기록
     */
    public void logSuccess(Action action, String detail) {
        String messageKey = getLogMessageKey("success", action.getValue());
        leaveaTrace.trace(messageKey, new String[]{action.getValue(), detail}, LoggingUtil.class);
    }

    /**
     * 실패 로그 기록
     */
    public void logFail(Action action, String detail) {
        String messageKey = getLogMessageKey("fail", action.getValue());
        leaveaTrace.trace(messageKey, new String[]{action.getValue(), detail}, LoggingUtil.class);
    }

    /**
     * 시도 로그 기록
     */
    public void logAttempt(Action action, String detail) {
        String messageKey = getLogMessageKey("attempt", action.getValue());
        leaveaTrace.trace(messageKey, new String[]{action.getValue(), detail}, LoggingUtil.class);
    }

    /**
     * 로그 메시지 키 생성
     */
    private String getLogMessageKey(String result, String action) {
        if ("retrieve".equals(action) || "create".equals(action) || "update".equals(action) || "delete".equals(action) ||
                "login".equals(action) || "logout".equals(action) || "refresh".equals(action) || "force".equals(action)) {
            return "log." + result + "." + action;
        }
        return "log." + result;
    }

}
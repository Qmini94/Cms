package kr.co.itid.cms.config.egov;

import org.egovframe.rte.fdl.cmmn.trace.LeaveaTrace;
import org.egovframe.rte.fdl.cmmn.trace.handler.TraceHandler;
import org.egovframe.rte.fdl.cmmn.trace.manager.DefaultTraceHandleManager;
import org.egovframe.rte.fdl.cmmn.trace.manager.TraceHandlerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

@Configuration
public class EgovConfig {

    @Bean(name = "leaveaTrace")
    public LeaveaTrace leaveaTrace(DefaultTraceHandleManager traceHandleManager) {
        LeaveaTrace leaveaTrace = new LeaveaTrace();
        leaveaTrace.setTraceHandlerServices(new TraceHandlerService[]{traceHandleManager});
        return leaveaTrace;
    }

    @Bean
    public DefaultTraceHandleManager traceHandleManager(AntPathMatcher antPathMatcher, CustomTraceHandler customTraceHandler) {
        DefaultTraceHandleManager defaultTraceHandleManager = new DefaultTraceHandleManager();
        defaultTraceHandleManager.setReqExpMatcher(antPathMatcher);
        defaultTraceHandleManager.setPatterns(new String[]{"*"});
        defaultTraceHandleManager.setHandlers(new TraceHandler[]{customTraceHandler});
        return defaultTraceHandleManager;
    }

    @Bean
    public AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }

    @Bean
    public CustomTraceHandler customTraceHandler() {
        return new CustomTraceHandler();
    }
}
package kr.co.itid.cms.config.egov;

import org.egovframe.rte.fdl.cmmn.trace.handler.TraceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomTraceHandler implements TraceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomTraceHandler.class);

    @Override
    public void todo(Class<?> clazz, String message) {
        LOGGER.info("Log Message: {}", message);
    }
}
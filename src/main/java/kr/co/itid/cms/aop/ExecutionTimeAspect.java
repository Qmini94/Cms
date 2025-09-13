package kr.co.itid.cms.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 메소드 실행 시간을 측정하고 로그로 출력하는 AOP Aspect
 */
@Aspect
@Component
@Slf4j
public class ExecutionTimeAspect {

    /**
     * @ExecutionTime 어노테이션이 붙은 메소드의 실행 시간을 측정
     */
    @Around("@annotation(executionTime)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint, ExecutionTime executionTime) throws Throwable {
        
        // 메소드 정보 추출
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        
        // 설명 설정 (기본값: 클래스명.메소드명)
        String description = executionTime.description();
        if (description.isEmpty()) {
            description = className + "." + methodName;
        }
        
        // 실행 시간 측정 시작
        long startTime = System.nanoTime();
        
        try {
            // 실제 메소드 실행
            Object result = joinPoint.proceed();
            
            // 실행 시간 계산
            long endTime = System.nanoTime();
            long executionTimeNanos = endTime - startTime;
            
            // 시간 단위 변환 및 로그 출력
            logExecutionTime(description, executionTimeNanos, executionTime.unit(), executionTime.level());
            
            return result;
            
        } catch (Exception e) {
            // 예외 발생 시에도 실행 시간 측정
            long endTime = System.nanoTime();
            long executionTimeNanos = endTime - startTime;
            
            logExecutionTime(description + " [EXCEPTION]", executionTimeNanos, executionTime.unit(), executionTime.level());
            
            // 예외 재발생
            throw e;
        }
    }
    
    /**
     * 실행 시간을 로그로 출력
     */
    private void logExecutionTime(String description, long executionTimeNanos, ExecutionTime.TimeUnit unit, ExecutionTime.LogLevel level) {
        
        // 시간 단위 변환
        double convertedTime = convertTime(executionTimeNanos, unit);
        String unitString = getUnitString(unit);
        
        // 로그 메시지 생성
        String logMessage = String.format("[EXECUTION_TIME] %s: %.2f %s", description, convertedTime, unitString);
        
        // 로그 레벨에 따라 출력
        switch (level) {
            case DEBUG:
                log.debug(logMessage);
                break;
            case INFO:
                log.info(logMessage);
                break;
            case WARN:
                log.warn(logMessage);
                break;
            case ERROR:
                log.error(logMessage);
                break;
            default:
                log.info(logMessage);
        }
    }
    
    /**
     * 나노초를 지정된 단위로 변환
     */
    private double convertTime(long nanos, ExecutionTime.TimeUnit unit) {
        switch (unit) {
            case NANOSECONDS:
                return nanos;
            case MICROSECONDS:
                return nanos / 1_000.0;
            case MILLISECONDS:
                return nanos / 1_000_000.0;
            case SECONDS:
                return nanos / 1_000_000_000.0;
            default:
                return nanos / 1_000_000.0; // 기본값: 밀리초
        }
    }
    
    /**
     * 시간 단위 문자열 반환
     */
    private String getUnitString(ExecutionTime.TimeUnit unit) {
        switch (unit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "μs";
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            default:
                return "ms";
        }
    }
}

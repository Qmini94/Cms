package kr.co.itid.cms.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 메소드 실행 시간을 측정하고 로그로 출력하는 어노테이션
 * 
 * 사용법:
 * @ExecutionTime
 * public void someMethod() { ... }
 * 
 * @ExecutionTime(description = "사이트 생성")
 * public void createSite() { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecutionTime {
    
    /**
     * 로그에 출력할 설명 (기본값: 메소드명)
     */
    String description() default "";
    
    /**
     * 로그 레벨 (기본값: INFO)
     */
    LogLevel level() default LogLevel.INFO;
    
    /**
     * 실행 시간 단위 (기본값: MILLISECONDS)
     */
    TimeUnit unit() default TimeUnit.MILLISECONDS;
    
    /**
     * 로그 레벨 열거형
     */
    enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
    
    /**
     * 시간 단위 열거형
     */
    enum TimeUnit {
        NANOSECONDS, MICROSECONDS, MILLISECONDS, SECONDS
    }
}

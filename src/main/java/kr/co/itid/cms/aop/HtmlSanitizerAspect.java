package kr.co.itid.cms.aop;

import kr.co.itid.cms.util.HtmlSanitizerUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Aspect
@Component
public class HtmlSanitizerAspect {

    @Around("execution(* kr.co.itid.cms.service.cms..*(..)) && @annotation(org.springframework.transaction.annotation.Transactional)")
    public Object sanitizeOnlyTransactional(ProceedingJoinPoint joinPoint) throws Throwable {
        for (Object arg : joinPoint.getArgs()) {
            sanitizeObjectFields(arg);
        }
        return joinPoint.proceed();
    }

    private void sanitizeObjectFields(Object obj) {
        if (obj == null) return;

        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(String.class)) {
                field.setAccessible(true);
                try {
                    String value = (String) field.get(obj);
                    if (value != null) {
                        try {
                            String sanitized = HtmlSanitizerUtil.sanitize(value);
                            field.set(obj, sanitized);
                        } catch (IllegalArgumentException ex) {
                            throw new IllegalArgumentException("입력 필드 [" + field.getName() + "]에 허용되지 않은 HTML이 포함되어 있습니다.", ex);
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("필드 접근 중 오류 발생: " + field.getName(), e);
                }
            }
        }
    }
}
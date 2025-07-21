package kr.co.itid.cms.util;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.service.cms.core.site.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ValidationUtil {

    private final SiteService siteService;

    /**
     * DTO 내의 모든 문자열 필드에 금지어가 포함되어 있는지 검사
     */
    public void validateBadWords(Object targetDto, JwtAuthenticatedUser user) throws Exception {
        String siteHostName = user.hostname();
        List<String> badWords = siteService.getBadWordsByHostName(siteHostName);

        if (badWords.isEmpty()) return;

        for (Field field : targetDto.getClass().getDeclaredFields()) {
            if (field.getType() == String.class) {
                field.setAccessible(true);
                try {
                    String value = (String) field.get(targetDto);
                    if (value != null) {
                        for (String badWord : badWords) {
                            if (value.contains(badWord)) {
                                throw new IllegalArgumentException(
                                        String.format("'%s' 필드에 금지어 '%s'가 포함되어 있습니다.", field.getName(), badWord)
                                );
                            }
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("금지어 검사 중 오류 발생", e);
                }
            }
        }
    }
}
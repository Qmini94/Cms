package kr.co.itid.cms.util;

import java.util.*;
import java.util.stream.Collectors;

public class MapKeyConverterUtil {

    private MapKeyConverterUtil() {} // 인스턴스화 방지

    /**
     * Map의 key를 snake_case → camelCase로 변환 (underscore 없으면 스킵)
     * @param original 변환할 원본 Map
     * @return key가 camelCase로 변환된 새로운 Map
     */
    public static Map<String, Object> convertKeysToCamelCase(Map<String, Object> original) {
        if (original == null || original.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new HashMap<>();
        original.forEach((k, v) -> {
            if (k != null && k.indexOf('_') != -1) {
                result.put(toCamelCase(k), v);
            } else {
                result.put(k, v);
            }
        });
        return result;
    }

    /**
     * snake_case → camelCase 변환
     * @param snake 변환할 문자열
     * @return camelCase 문자열
     */
    public static String toCamelCase(String snake) {
        if (snake == null || snake.isEmpty()) {
            return snake;
        }
        String[] parts = snake.split("_");
        if (parts.length == 1) {
            return snake;
        }
        return parts[0] + Arrays.stream(parts, 1, parts.length)
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining());
    }
}

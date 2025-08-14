package kr.co.itid.cms.util;

import kr.co.itid.cms.constanrt.PermissionConstants;

import java.util.*;

/**
 * 권한 키 정규화/동의어/맵 변환 유틸
 */
public final class PermissionKeyUtil {
    private PermissionKeyUtil() {}

    /** 정규 키 집합 (대문자) */
    public static final Set<String> CANON_KEYS = Set.of(
            PermissionConstants.ADMIN,
            PermissionConstants.MANAGE,
            PermissionConstants.ACCESS,
            PermissionConstants.VIEW,
            PermissionConstants.WRITE,
            PermissionConstants.MODIFY,
            PermissionConstants.REMOVE,
            PermissionConstants.REPLY
    );

    /** 선택적으로 허용할 확장 키 */
    public static final String ASSIGNEE = "ASSIGNEE";

    /** 동의어 매핑 (대문자) */
    private static final Map<String, String> SYNONYMS = Map.of(
            "DELETE",       PermissionConstants.REMOVE,
            "COMMENT",      PermissionConstants.REPLY,
            "MENU_ACCESS",  PermissionConstants.ACCESS,
            // 선택 동의어
            "READ",         PermissionConstants.VIEW,
            "UPDATE",       PermissionConstants.MODIFY
    );

    /** 키 정규화: trim → upper → 동의어 치환 */
    public static String canon(String raw) {
        if (raw == null) return null;
        String up = raw.trim().toUpperCase(Locale.ROOT);
        return SYNONYMS.getOrDefault(up, up);
    }

    /** 정규 키 여부 */
    public static boolean isCanon(String key) {
        return key != null && CANON_KEYS.contains(key);
    }

    /** 맵에서 정규 키 기준으로 boolean 값 얻기 (동의어 포함 탐색) */
    public static boolean getBool(Map<String, Boolean> map, String canonicalKey, String... extraSynonyms) {
        if (map == null || canonicalKey == null) return false;
        // 정규 키 우선
        Boolean v = map.get(canonicalKey);
        if (v != null) return v;

        // 추가 동의어/키 탐색
        for (String s : extraSynonyms) {
            String c = canon(s);
            v = map.get(c);
            if (v != null) return v;
        }
        return false;
    }

    /** 권한 맵 정규화: 키 캐논 + 허용 키만 유지 (+ 필요시 ASSIGNEE 허용) */
    public static Map<String, Boolean> normalizePermissions(Map<String, Boolean> src, boolean includeAssignee) {
        if (src == null || src.isEmpty()) return new LinkedHashMap<>();
        Map<String, Boolean> out = new LinkedHashMap<>();
        for (Map.Entry<String, Boolean> e : src.entrySet()) {
            String k = canon(e.getKey());
            Boolean v = e.getValue();
            if (k == null || v == null) continue;

            if (CANON_KEYS.contains(k) || (includeAssignee && ASSIGNEE.equals(k))) {
                out.put(k, v);
            }
        }
        return out;
    }

    /** "y"/"n" 문자열 변환 */
    public static String yn(boolean b) { return b ? "y" : "n"; }

    /** "y"/"n" → boolean */
    public static boolean ynToBool(String yn) { return yn != null && "y".equalsIgnoreCase(yn); }

    /** 엔티티(Y/N)에서 맵으로 넣기 */
    public static void putYN(Map<String, Boolean> map, String key, String yn) {
        if (yn == null) return;
        map.put(key, ynToBool(yn));
    }
}
package kr.co.itid.cms.constanrt;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** LEVEL 코드 → 표시 이름 매핑 상수 */
public final class LevelConstants {

    private LevelConstants() {}

    /** ex) "1"→"관리자", "6"→"직원", "9"→"회원", "11"→"비회원" */
    public static final Map<String, String> LEVEL_NAME_MAP;
    static {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        m.put("1", "관리자");
        m.put("6", "직원");
        m.put("9", "회원");
        m.put("11", "비회원");
        LEVEL_NAME_MAP = Collections.unmodifiableMap(m);
    }
}

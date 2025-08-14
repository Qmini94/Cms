package kr.co.itid.cms.dto.auth.permission;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kr.co.itid.cms.util.PermissionKeyUtil;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class PermBooleanMapDeserializer extends JsonDeserializer<Map<String, Boolean>> {

    @Override
    public Map<String, Boolean> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.readValueAsTree();
        Map<String, Boolean> out = new LinkedHashMap<>();
        if (node == null || node.isNull() || !node.isObject()) return out;

        ObjectNode obj = (ObjectNode) node;
        Iterator<Map.Entry<String, JsonNode>> it = obj.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> e = it.next();
            String canonKey = PermissionKeyUtil.canon(e.getKey());
            if (canonKey == null) continue;

            Boolean val = parseToBool(e.getValue());
            if (val != null) {
                out.put(canonKey, val);
            }
        }
        return out;
    }

    private Boolean parseToBool(JsonNode v) {
        if (v == null || v.isNull()) return null;
        if (v.isBoolean()) return v.booleanValue();
        if (v.isInt() || v.isLong()) return v.longValue() != 0;
        if (v.isTextual()) {
            String s = v.textValue();
            if (s == null) return null;
            String up = s.trim().toUpperCase();
            if ("ALLOW".equals(up) || "TRUE".equals(up) || "1".equals(up)) return true;
            if ("DENY".equals(up)  || "FALSE".equals(up) || "0".equals(up)) return false;
        }
        return null;
    }
}
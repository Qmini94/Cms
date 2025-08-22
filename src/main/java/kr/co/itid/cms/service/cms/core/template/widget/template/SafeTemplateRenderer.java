package kr.co.itid.cms.service.cms.core.template.widget.template;

import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * SafeTemplateRenderer
 *
 * - {{var}} → HTML escape
 * - {{var|html}} → raw 삽입 (주의!)
 */
@Component
public class SafeTemplateRenderer {

    public String render(String template, Map<String, Object> vars) {
        if (template == null || template.isBlank() || vars == null || vars.isEmpty()) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, Object> e : vars.entrySet()) {
            String key = e.getKey();
            String val = (e.getValue() == null) ? "" : String.valueOf(e.getValue());

            // escape
            result = result.replace("{{" + key + "}}", StringEscapeUtils.escapeHtml4(val));

            // raw
            result = result.replace("{{" + key + "|html}}", val);
        }

        return result;
    }
}
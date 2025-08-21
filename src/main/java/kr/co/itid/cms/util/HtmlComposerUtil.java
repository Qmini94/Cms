package kr.co.itid.cms.util;

import kr.co.itid.cms.service.cms.core.page.widget.model.WidgetCtx;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HtmlComposerUtil {

    private HtmlComposerUtil() {}

    private static final Pattern SLOT_TAG = Pattern.compile("<cms-slot\\s+name\\s*=\\s*\"([^\"]+)\"\\s*/?>", Pattern.CASE_INSENSITIVE);
    private static final Pattern HEAD_CLOSE = Pattern.compile("</head\\s*>", Pattern.CASE_INSENSITIVE);

    public static String merge(String templateHtml, WidgetCtx ctx) {
        return templateHtml == null ? "" : templateHtml;
    }

    public static String composeLayout(String layoutHtml, String pageHtml, WidgetCtx ctx) {
        String base = layoutHtml == null ? "" : layoutHtml;
        String body = pageHtml == null ? "" : pageHtml;
        Matcher m = SLOT_TAG.matcher(base);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String name = m.group(1);
            String replacement = "content".equalsIgnoreCase(name) ? body : "";
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static String injectHeadAssets(String html, List<String> cssUrls, List<String> jsUrls) {
        return injectHeadAssets(html, cssUrls, jsUrls, null);
    }

    public static String injectHeadAssets(String html, List<String> cssUrls, List<String> jsUrls, String inlineCss) {
        String doc = html == null ? "" : html;
        List<String> css = cssUrls == null ? List.of() : cssUrls;
        List<String> js  = jsUrls  == null ? List.of() : jsUrls;

        StringBuilder headAdd = new StringBuilder();
        for (String href : dedup(css)) {
            if (isBlank(href)) continue;
            headAdd.append("<link rel=\"stylesheet\" href=\"").append(escapeAttr(href)).append("\"/>");
        }
        if (inlineCss != null && !inlineCss.isBlank()) {
            headAdd.append("<style>").append(escapeStyle(inlineCss)).append("</style>");
        }
        for (String src : dedup(js)) {
            if (isBlank(src)) continue;
            headAdd.append("<script defer src=\"").append(escapeAttr(src)).append("\"></script>");
        }

        Matcher headMatcher = HEAD_CLOSE.matcher(doc);
        if (headMatcher.find()) {
            StringBuilder out = new StringBuilder();
            headMatcher.appendReplacement(out, Matcher.quoteReplacement(headAdd.toString() + "</head>"));
            headMatcher.appendTail(out);
            return out.toString();
        }
        return "<!doctype html><html><head>" + headAdd + "</head><body>" + doc + "</body></html>";
    }

    public static String applyAccessibilityFix(String html) {
        return html == null ? "" : html;
    }

    public static String minimalShell(UnaryOperator<ShellBuilder> customizer) {
        ShellBuilder b = customizer.apply(new ShellBuilder());
        StringBuilder dataAttrs = new StringBuilder();
        b.data.forEach((k, v) -> dataAttrs.append(" data-").append(escapeAttr(k)).append("=\"").append(escapeAttr(String.valueOf(v))).append("\""));
        StringBuilder containers = new StringBuilder();
        for (String sel : b.containers) {
            String id = sel.startsWith("#") ? sel.substring(1) : sel;
            containers.append("<div id=\"").append(escapeAttr(id)).append("\"></div>");
        }
        String head = "<meta charset=\"utf-8\"/><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>"
                + "<title>" + escapeText(b.title == null ? "Shell" : b.title) + "</title>";
        String doc = "<!doctype html><html><head>" + head + "</head><body" + dataAttrs + ">" + containers + "</body></html>";
        return HtmlSanitizerUtil.clean(doc);
    }

    public static final class ShellBuilder {
        private String title;
        private final List<String> containers = new ArrayList<>();
        private final Map<String, Object> data = new LinkedHashMap<>();

        public ShellBuilder title(String title) { this.title = title; return this; }
        public ShellBuilder addContainer(String cssSelectorOrId) { if (cssSelectorOrId != null && !cssSelectorOrId.isBlank()) containers.add(cssSelectorOrId); return this; }
        public ShellBuilder putData(String key, Object value) { if (key != null && !key.isBlank()) data.put(key, value); return this; }
    }

    private static Collection<String> dedup(List<String> list) {
        if (list == null || list.isEmpty()) return List.of();
        LinkedHashSet<String> s = new LinkedHashSet<>();
        for (String v : list) if (!isBlank(v)) s.add(v.trim());
        return s;
    }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static String escapeAttr(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("\"","&quot;").replace("<","&lt;").replace(">","&gt;");
    }
    private static String escapeText(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
    private static String escapeStyle(String s) {
        if (s == null) return "";
        return s.replace("</","&lt;/");
    }
}
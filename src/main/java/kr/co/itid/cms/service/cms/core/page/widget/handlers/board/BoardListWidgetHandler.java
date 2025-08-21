package kr.co.itid.cms.service.cms.core.page.widget.handlers.board;

import kr.co.itid.cms.repository.cms.core.board.BoardValidationDao;
import kr.co.itid.cms.repository.cms.core.board.DynamicBoardDao;
import kr.co.itid.cms.service.cms.core.page.widget.engine.WidgetHandler;
import kr.co.itid.cms.service.cms.core.page.widget.model.WidgetCtx;
import kr.co.itid.cms.service.cms.core.page.widget.template.SafeTemplateRenderer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 게시판 목록 위젯 (board.list)
 *
 * 사용 예)
 * <cms-widget type="board.list" board="notice" limit="5" fields="title,createdAt" class="notice-list">
 *   <template header><h3 class="sr-only">공지사항</h3></template>
 *   <template item>
 *     <li>
 *       <a href="/board/{{slug}}">{{title}}</a>
 *       <time>{{createdAt|date:yyyy.MM.dd}}</time>
 *     </li>
 *   </template>
 *   <template empty><li class="empty">게시글이 없습니다.</li></template>
 *   <template wrapper><ul class="{{class}}">{{items}}</ul></template>
 * </cms-widget>
 *
 * 속성
 * - board   (필수): 게시판 코드
 * - limit   (선택): 기본 5, 1~50
 * - fields  (선택): 기본 "title,createdAt" 허용 {id,slug,title,createdAt,author,views,hasAttach}
 * - class   (선택): wrapper에 전달
 * - template(선택): "ul"|"table" (템플릿 미제공 시 디폴트)
 *
 * 데이터 모델(Projection)
 * - id(Long), slug(String), title(String), createdAt(OffsetDateTime), author(String), views(Long), hasAttach(Boolean)
 *
 * 비고
 * - XSS 방지는 최종 Sanitizer 단계에서 수행. 여기선 기본 escape를 지향.
 */
@Component
@RequiredArgsConstructor
public class BoardListWidgetHandler implements WidgetHandler {

    private final DynamicBoardDao dynamicBoardDao;
    private final BoardValidationDao boardValidationDao;
    private final SafeTemplateRenderer tpl;

    private static final Set<String> ALLOWED_FIELDS = Set.of(
            "id", "slug", "title", "createdAt", "author", "views", "hasAttach"
    );

    @Override
    public boolean supports(String type) {
        return "board.list".equalsIgnoreCase(type);
    }

    @Override
    public String render(Element widgetTag, WidgetCtx ctx) throws Exception {
        final String board = reqAttr(widgetTag, "board");
        final int limit = clampInt(widgetTag.hasAttr("limit") ? widgetTag.attr("limit") : "5", 1, 50, 5);
        final String fieldsAttr = widgetTag.hasAttr("fields") ? widgetTag.attr("fields") : "title,createdAt";
        final String template = widgetTag.hasAttr("template") ? widgetTag.attr("template").toLowerCase(Locale.ROOT) : "ul";
        final String extraClass = widgetTag.hasAttr("class") ? widgetTag.attr("class") : "";

        final List<String> fields = Arrays.stream(fieldsAttr.split(","))
                .map(String::trim)
                .filter(ALLOWED_FIELDS::contains)
                .collect(Collectors.toList());

        // 1) boardId 검증 (보안 체크)
        String validatedBoardId = boardValidationDao.validateBoardId(board);

        // 2) 데이터 조회 - 메서드명 수정
        List<BoardPostSummary> rows = dynamicBoardDao.findRecentSummaryByBoardId(validatedBoardId, limit);

        // 나머지 코드는 그대로...
        // 3) 템플릿 수집
        String headerTpl  = pickTemplate(widgetTag, "header", null);
        String itemTpl    = pickTemplate(widgetTag, "item", defaultItemTemplate(template, fields));
        String emptyTpl   = pickTemplate(widgetTag, "empty", defaultEmptyTemplate(template));
        String wrapperTpl = pickTemplate(widgetTag, "wrapper", defaultWrapperTemplate(template));

        // 4) header 렌더
        String headerHtml = (headerTpl == null) ? "" : tpl.render(headerTpl, Map.of());

        // 5) items 렌더
        String itemsHtml;
        if (rows == null || rows.isEmpty()) {
            itemsHtml = tpl.render(emptyTpl, Map.of());
        } else {
            StringBuilder items = new StringBuilder();
            for (BoardPostSummary r : rows) {
                Map<String, Object> model = new HashMap<>();
                model.put("id", r.getId());
                model.put("slug", nz(r.getSlug()));
                model.put("title", nz(r.getTitle()));
                model.put("createdAt", r.getCreatedAt());
                model.put("author", nz(r.getAuthor()));
                model.put("views", r.getViews() == null ? 0 : r.getViews());
                model.put("hasAttach", Boolean.TRUE.equals(r.getHasAttach()) ? "Y" : "N");
                items.append(tpl.render(itemTpl, model));
            }
            itemsHtml = items.toString();
        }

        // 5) wrapper에 삽입
        Map<String, Object> wrapModel = new HashMap<>();
        wrapModel.put("items", itemsHtml);
        wrapModel.put("class", StringEscapeUtils.escapeHtml4(extraClass));
        String wrapHtml = tpl.render(wrapperTpl, wrapModel);

        // 6) header + wrapper 결합
        return headerHtml + wrapHtml;
    }

    /* ------------------------- 내부 유틸 ------------------------- */

    private static String reqAttr(Element el, String name) {
        String v = el.hasAttr(name) ? el.attr(name).trim() : "";
        if (v.isEmpty()) throw new IllegalArgumentException("missing attr: " + name);
        return v;
    }

    private static int clampInt(String s, int min, int max, int defVal) {
        try {
            int v = Integer.parseInt(s.trim());
            return Math.max(min, Math.min(max, v));
        } catch (Exception e) {
            return defVal;
        }
    }

    private static String pickTemplate(Element widgetTag, String name, String defVal) {
        // <template name> 또는 <template name="...">, <template item> 같은 형태 모두 지원
        Element byAttr = widgetTag.selectFirst("template[" + name + "]");
        if (byAttr != null) return byAttr.html();

        // <template item> 형태
        for (Element child : widgetTag.children()) {
            if ("template".equalsIgnoreCase(child.tagName()) && child.hasAttr(name)) {
                return child.html();
            }
        }
        Element byClass = widgetTag.selectFirst("template." + name);
        if (byClass != null) return byClass.html();

        // 태그명 그대로(<template item>) 지원
        for (Element child : widgetTag.children()) {
            if ("template".equalsIgnoreCase(child.tagName()) && name.equalsIgnoreCase(child.attr("name"))) {
                return child.html();
            }
            if ("template".equalsIgnoreCase(child.tagName()) && name.equalsIgnoreCase(child.id())) {
                return child.html();
            }
        }

        return defVal;
    }

    private static String defaultItemTemplate(String template, List<String> fields) {
        if ("table".equals(template)) {
            StringBuilder row = new StringBuilder("<tr>");
            if (fields.contains("title"))     row.append("<td class=\"title\"><a href=\"/board/{{slug}}\">{{title}}</a></td>");
            if (fields.contains("createdAt")) row.append("<td class=\"date\">{{createdAt|date:yyyy-MM-dd}}</td>");
            if (fields.contains("author"))    row.append("<td class=\"author\">{{author}}</td>");
            if (fields.contains("views"))     row.append("<td class=\"views\">{{views}}</td>");
            if (fields.contains("hasAttach")) row.append("<td class=\"file\">{{hasAttach}}</td>");
            row.append("</tr>");
            return row.toString();
        } else { // ul
            StringBuilder li = new StringBuilder("<li>");
            if (fields.contains("title"))     li.append("<a class=\"title\" href=\"/board/{{slug}}\">{{title}}</a>");
            if (fields.contains("createdAt")) li.append(" <time class=\"date\">{{createdAt|date:yyyy.MM.dd}}</time>");
            if (fields.contains("author"))    li.append(" <span class=\"author\">{{author}}</span>");
            if (fields.contains("views"))     li.append(" <span class=\"views\">{{views}}</span>");
            if (fields.contains("hasAttach")) li.append(" <span class=\"file\">{{hasAttach}}</span>");
            li.append("</li>");
            return li.toString();
        }
    }

    private static String defaultEmptyTemplate(String template) {
        return "table".equals(template)
                ? "<tr class=\"empty\"><td colspan=\"5\">데이터가 없습니다.</td></tr>"
                : "<li class=\"empty\">데이터가 없습니다.</li>";
    }

    private static String defaultWrapperTemplate(String template) {
        return "table".equals(template)
                ? "<table class=\"{{class}}\"><tbody>{{items}}</tbody></table>"
                : "<ul class=\"{{class}}\">{{items}}</ul>";
    }

    private static String nz(String s) { return (s == null) ? "" : s; }
}
package kr.co.itid.cms.service.cms.core.page.impl;

import kr.co.itid.cms.service.cms.core.page.WidgetService;
import kr.co.itid.cms.util.WidgetCtx;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WidgetServiceImpl implements WidgetService {
    @Override
    public String render(String pageHtml, WidgetCtx ctx) {
        // Jsoup 파싱
        Document doc = Jsoup.parseBodyFragment(pageHtml);

        // <cms-board-list> 토큰 찾아서 위젯 치환
        for (Element el : doc.select("cms-board-list")) {
            String board = el.attr("board");
            int limit = parseInt(el.attr("limit"), 5);

            // TODO: 실제 서비스에선 boardSvc.findLatest(site, board, limit) 같은 호출로 교체
            List<Map<String, String>> raw = List.of(
                    Map.of("title","공지 1","url","/"+ctx.site()+"/board/"+board+"/1"),
                    Map.of("title","공지 2","url","/"+ctx.site()+"/board/"+board+"/2"),
                    Map.of("title","공지 3","url","/"+ctx.site()+"/board/"+board+"/3")
            );

            // limit 적용
            List<Map<String,String>> items = raw.stream().limit(limit).toList();

            StringBuilder ul = new StringBuilder("<ul class='board-list'>");
            for (var it : items) {
                ul.append("<li><a href='")
                        .append(it.get("url"))
                        .append("'>")
                        .append(escape(it.get("title")))
                        .append("</a></li>");
            }
            ul.append("</ul>");

            el.after(ul.toString());
            el.remove();
        }

        return doc.body().html();
    }

    private int parseInt(String s, int d) {
        try { return Integer.parseInt(s); }
        catch(Exception e){ return d; }
    }

    private String escape(String s) {
        return s==null? "" : s.replace("&","&amp;")
                .replace("<","&lt;")
                .replace(">","&gt;");
    }
}
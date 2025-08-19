package kr.co.itid.cms.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Map;

/**
 * 레이아웃 HTML 안의 <cms-slot name="..."> 태그를 찾아
 * 동적으로 치환해주는 유틸리티
 */
public final class HtmlComposerUtil {

    private HtmlComposerUtil() {}

    /**
     * @param layoutHtml 관리자에서 저장한 전체 레이아웃 HTML
     * @param slots key=슬롯 name, value=치환할 HTML
     * @return 슬롯이 치환된 최종 HTML
     */
    public static String compose(String layoutHtml, Map<String, String> slots) {
        if (layoutHtml == null) return "";

        // Jsoup 파싱
        Document doc = Jsoup.parse(layoutHtml);

        // 슬롯 위치 찾아서 치환
        for (Map.Entry<String, String> entry : slots.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();

            for (Element slot : doc.select("cms-slot[name=" + name + "]")) {
                slot.after(value);   // 본문 HTML을 슬롯 뒤에 추가
                slot.remove();       // 슬롯 태그 제거
            }
        }

        // body 내부만 반환
        return doc.body().html();
    }
}
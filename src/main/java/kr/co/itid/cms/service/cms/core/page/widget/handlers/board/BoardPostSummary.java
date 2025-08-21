package kr.co.itid.cms.service.cms.core.page.widget.handlers.board;

import java.time.OffsetDateTime;

/** 게시글 목록용 Projection DTO */
public interface BoardPostSummary {
    Long getId();
    String getSlug();
    String getTitle();
    OffsetDateTime getCreatedAt();
    String getAuthor();
    Long getViews();
    Boolean getHasAttach();
}

package kr.co.itid.cms.service.cms.core.template.widget.handlers.board;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * BoardPostSummary interface의 JDBC용 구현체
 */
@Getter
@AllArgsConstructor
public class BoardPostSummaryImpl implements BoardPostSummary {

    private final Long id;
    private final String slug;
    private final String title;
    private final OffsetDateTime createdAt;
    private final String author;
    private final Long views;
    private final Boolean hasAttach;
}
package kr.co.itid.cms.repository.cms.core.content.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.entity.cms.core.content.Content;
import kr.co.itid.cms.entity.cms.core.content.QContent;
import kr.co.itid.cms.repository.cms.core.content.ContentRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ContentRepositoryImpl implements ContentRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QContent qContent = QContent.content1;

    @Override
    public Page<Content> searchByCondition(SearchOption option, Pageable pageable) {
        BooleanBuilder condition = new BooleanBuilder();

        // 기본 조건: sort = 0
        condition.and(qContent.sort.eq(0));

        if (option.getKeyword() != null && !option.getKeyword().isBlank() && option.getSearchKeys() != null) {
            BooleanBuilder keywordBuilder = new BooleanBuilder();
            for (String key : option.getSearchKeys()) {
                switch (key) {
                    case "title":
                        keywordBuilder.or(qContent.title.containsIgnoreCase(option.getKeyword()));
                        break;
                    case "hostname":
                        keywordBuilder.or(qContent.hostname.containsIgnoreCase(option.getKeyword()));
                        break;
                    case "isUse":
                        if ("true".equalsIgnoreCase(option.getKeyword())) {
                            keywordBuilder.or(qContent.isUse.isTrue());
                        } else if ("false".equalsIgnoreCase(option.getKeyword())) {
                            keywordBuilder.or(qContent.isUse.isFalse());
                        }
                        break;
                    case "createdBy":
                        keywordBuilder.or(qContent.createdBy.containsIgnoreCase(option.getKeyword()));
                        break;
                }
            }
            condition.and(keywordBuilder);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (option.getStartDate() != null && !option.getStartDate().isBlank()) {
            LocalDate start = LocalDate.parse(option.getStartDate(), formatter);
            condition.and(qContent.createdDate.goe(start.atStartOfDay()));
        }

        if (option.getEndDate() != null && !option.getEndDate().isBlank()) {
            LocalDate end = LocalDate.parse(option.getEndDate(), formatter);
            condition.and(qContent.createdDate.loe(end.atTime(23, 59, 59)));
        }

        List<Content> result = queryFactory
                .selectFrom(qContent)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(qContent.createdDate.desc()) // 기본 정렬
                .fetch();

        return PageableExecutionUtils.getPage(result, pageable, () ->
                queryFactory.select(qContent.count())
                        .from(qContent)
                        .where(condition)
                        .fetchOne()
        );
    }
}
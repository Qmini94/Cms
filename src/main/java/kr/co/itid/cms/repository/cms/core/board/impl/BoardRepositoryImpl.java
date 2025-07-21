package kr.co.itid.cms.repository.cms.core.board.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.itid.cms.dto.cms.core.board.BoardSearchOption;
import kr.co.itid.cms.entity.cms.core.board.Board;
import kr.co.itid.cms.entity.cms.core.board.QBoard;
import kr.co.itid.cms.repository.cms.core.board.BoardRepositoryCustom;
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
public class BoardRepositoryImpl implements BoardRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QBoard qBoard = QBoard.board;

    @Override
    public Page<Board> searchByCondition(String boardId, BoardSearchOption option, Pageable pageable) {
        BooleanBuilder condition = new BooleanBuilder();

        condition.and(qBoard.boardId.eq(boardId));
        condition.and(qBoard.isDeleted.isFalse());

        if (option.getKeyword() != null && !option.getKeyword().isBlank() && option.getSearchKeys() != null) {
            BooleanBuilder keywordBuilder = new BooleanBuilder();
            for (String key : option.getSearchKeys()) {
                switch (key) {
                    case "title":
                        keywordBuilder.or(qBoard.title.containsIgnoreCase(option.getKeyword()));
                        break;
                    case "content":
                        keywordBuilder.or(qBoard.content.containsIgnoreCase(option.getKeyword()));
                        break;
                    case "regName":
                        keywordBuilder.or(qBoard.regName.containsIgnoreCase(option.getKeyword()));
                        break;
                    case "regId":
                        keywordBuilder.or(qBoard.regId.containsIgnoreCase(option.getKeyword()));
                        break;
                }
            }
            condition.and(keywordBuilder);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (option.getStartDate() != null && !option.getStartDate().isBlank()) {
            LocalDate start = LocalDate.parse(option.getStartDate(), formatter);
            condition.and(qBoard.createdDate.goe(start.atStartOfDay()));
        }

        if (option.getEndDate() != null && !option.getEndDate().isBlank()) {
            LocalDate end = LocalDate.parse(option.getEndDate(), formatter);
            condition.and(qBoard.createdDate.loe(end.atTime(23, 59, 59)));
        }

        List<Board> result = queryFactory
                .selectFrom(qBoard)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(qBoard.createdDate.desc()) // 정렬 기본
                .fetch();

        return PageableExecutionUtils.getPage(result, pageable, () ->
                queryFactory.select(qBoard.count())
                        .from(qBoard)
                        .where(condition)
                        .fetchOne()
        );
    }
}

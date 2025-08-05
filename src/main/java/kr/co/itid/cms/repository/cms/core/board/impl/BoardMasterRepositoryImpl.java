package kr.co.itid.cms.repository.cms.core.board.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.itid.cms.dto.cms.core.board.BoardSearchOption;
import kr.co.itid.cms.entity.cms.core.board.BoardMaster;
import kr.co.itid.cms.entity.cms.core.board.QBoardMaster;
import kr.co.itid.cms.repository.cms.core.board.BoardMasterRepositoryCustom;
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
public class BoardMasterRepositoryImpl implements BoardMasterRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QBoardMaster qBoardMaster = QBoardMaster.boardMaster;

    @Override
    public Page<BoardMaster> searchByCondition(BoardSearchOption option, Pageable pageable) {
        BooleanBuilder condition = new BooleanBuilder();

        // 키워드 검색
        if (option.getKeyword() != null && !option.getKeyword().isBlank() && option.getSearchKeys() != null) {
            BooleanBuilder keywordBuilder = new BooleanBuilder();
            for (String key : option.getSearchKeys()) {
                switch (key) {
                    case "boardName":
                        keywordBuilder.or(qBoardMaster.boardName.containsIgnoreCase(option.getKeyword()));
                        break;
                    case "boardId":
                        keywordBuilder.or(qBoardMaster.boardId.containsIgnoreCase(option.getKeyword()));
                        break;
                    case "boardType":
                        keywordBuilder.or(qBoardMaster.boardType.containsIgnoreCase(option.getKeyword()));
                        break;
                    case "description":
                        keywordBuilder.or(qBoardMaster.description.containsIgnoreCase(option.getKeyword()));
                        break;
                }
            }
            condition.and(keywordBuilder);
        }

        // 날짜 필터 (createdDate 기준)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (option.getStartDate() != null && !option.getStartDate().isBlank()) {
            LocalDate start = LocalDate.parse(option.getStartDate(), formatter);
            condition.and(qBoardMaster.createdDate.goe(start.atStartOfDay()));
        }

        if (option.getEndDate() != null && !option.getEndDate().isBlank()) {
            LocalDate end = LocalDate.parse(option.getEndDate(), formatter);
            condition.and(qBoardMaster.updatedDate.loe(end.atTime(23, 59, 59)));
        }

        // 실제 데이터 조회
        List<BoardMaster> result = queryFactory
                .selectFrom(qBoardMaster)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(qBoardMaster.updatedDate.desc()) // 기본 정렬
                .fetch();

        // 카운트 쿼리 포함한 Page 생성
        return PageableExecutionUtils.getPage(result, pageable, () ->
                queryFactory.select(qBoardMaster.count())
                        .from(qBoardMaster)
                        .where(condition)
                        .fetchOne()
        );
    }
}
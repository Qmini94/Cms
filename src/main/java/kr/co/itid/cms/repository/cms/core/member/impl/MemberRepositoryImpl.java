package kr.co.itid.cms.repository.cms.core.member.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.entity.cms.core.member.Member;
import kr.co.itid.cms.entity.cms.core.member.QMember;
import kr.co.itid.cms.repository.cms.core.member.MemberRepositoryCustom;
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
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private static final QMember qMember = QMember.member;

    @Override
    public Page<Member> searchByCondition(SearchOption option, Pageable pageable) {
        BooleanBuilder condition = new BooleanBuilder();

        // 키워드 + 검색키 처리
        if (option != null
                && hasText(option.getKeyword())
                && option.getSearchKeys() != null) {

            BooleanBuilder keywordBuilder = new BooleanBuilder();
            String kw = option.getKeyword();

            for (String key : option.getSearchKeys()) {
                if (key == null) continue;
                switch (key) {
                    case "userId":
                        keywordBuilder.or(qMember.userId.containsIgnoreCase(kw));
                        break;
                    case "userName":
                        keywordBuilder.or(qMember.userName.containsIgnoreCase(kw));
                        break;
                    case "userLevel":
                        try {
                            Integer lv = Integer.valueOf(kw.trim());
                            keywordBuilder.or(qMember.userLevel.eq(lv));
                        } catch (NumberFormatException ignore) { /* 무시 */ }
                        break;
                    default:
                        // 그 외 키는 무시
                }
            }
            condition.and(keywordBuilder);
        }

        // 날짜 범위(yyyy-MM-dd 문자열) → regDate 기준
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (option != null && hasText(option.getStartDate())) {
            LocalDate start = LocalDate.parse(option.getStartDate(), formatter);
            condition.and(qMember.regDate.goe(start.atStartOfDay()));
        }
        if (option != null && hasText(option.getEndDate())) {
            LocalDate end = LocalDate.parse(option.getEndDate(), formatter);
            condition.and(qMember.regDate.loe(end.atTime(23, 59, 59)));
        }

        List<Member> result = queryFactory
                .selectFrom(qMember)
                .where(condition)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(qMember.regDate.desc()) // 기본 정렬
                .fetch();

        return PageableExecutionUtils.getPage(result, pageable, () ->
                queryFactory.select(qMember.count())
                        .from(qMember)
                        .where(condition)
                        .fetchOne()
        );
    }

    // ------------------------------------------------------
    // 자동완성(권한 대상 추가용)
    // userId / userName LIKE 검색, 상위 limit개 반환
    // ------------------------------------------------------
    @Override
    public List<Member> searchForSuggest(String keyword, int limit) {
        final String kw = (keyword == null) ? "" : keyword.trim();
        if (!hasText(kw)) {
            return java.util.Collections.emptyList();
        }
        final int lim = Math.max(1, Math.min(limit, 20)); // 상한 20

        BooleanBuilder condition = new BooleanBuilder()
                .and(
                        qMember.userId.containsIgnoreCase(kw)
                                .or(qMember.userName.containsIgnoreCase(kw))
                );

        // 정렬 정책: 최근 등록 우선 → 동일 점수 시 userName ASC
        return queryFactory
                .selectFrom(qMember)
                .where(condition)
                .orderBy(qMember.regDate.desc(), qMember.userName.asc())
                .limit(lim)
                .fetch();
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
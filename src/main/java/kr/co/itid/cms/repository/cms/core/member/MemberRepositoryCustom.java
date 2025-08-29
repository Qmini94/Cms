package kr.co.itid.cms.repository.cms.core.member;

import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.entity.cms.core.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {
    Page<Member> searchByCondition(SearchOption option, Pageable pageable);

    /**
     * 자동완성(권한 대상 추가용) 회원 검색
     * - 이름/아이디 일부 일치 검색
     * - 반환 개수는 limit 상한 적용
     *
     * @param keyword 검색어
     * @param limit   최대 결과 수
     * @return 회원 목록
     */
    List<Member> searchForSuggest(String keyword, int limit);
}
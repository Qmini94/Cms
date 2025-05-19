package kr.co.itid.cms.repository.cms.core.content;

import kr.co.itid.cms.entity.cms.core.content.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {

    /**
     * sort가 0이면서 사용 중인 콘텐츠 목록 (대표 콘텐츠)
     */
    List<Content> findBySortAndIsUseTrue(int sort);

    /**
     * 특정 parentId 그룹의 콘텐츠 목록 (정렬 순서대로)
     */
    List<Content> findByParentIdOrderBySortAsc(Long parentId);

    /**
     * 특정 parentId에 속한 콘텐츠의 최대 sort값 조회
     */
    @Query("SELECT MAX(c.sort) FROM Content c WHERE c.parentId = :parentId")
    Integer findMaxSortByParentId(Long parentId);

    /**
     * 대표 콘텐츠 및 그 하위 콘텐츠 전체 삭제 (parentId 또는 idx가 일치하는 경우)
     */
    void deleteAllByParentIdOrIdx(Long parentId, Long idx);
}

package kr.co.itid.cms.repository.cms.core.content;

import kr.co.itid.cms.entity.cms.core.content.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Long>, ContentRepositoryCustom {

    /**
     * 특정 parentId 그룹의 콘텐츠 목록 (정렬 순서대로)
     */
    List<Content> findByParentIdOrderBySortAsc(Long parentId);

    /**
     * 특정 parentId에 속한 콘텐츠의 최대 sort값 조회
     */
    @Query("SELECT MAX(c.sort) FROM Content c WHERE c.parentId = :parentId")
    Integer findMaxSortByParentId(@Param("parentId") Long parentId);

    /**
     * 대표 콘텐츠 및 그 하위 콘텐츠 전체 삭제 (parentId 또는 idx가 일치하는 경우)
     */
    void deleteAllByParentIdOrIdx(Long parentId, Long idx);

    /**
     * parentId 기준으로 사용 중인 콘텐츠 1건만 조회 (isUse = true)
     */
    Optional<Content> findFirstByParentIdAndIsUseTrue(Long parentId);

    /**
     * 저장 또는 수정 시, 동일 parentId의 기존 콘텐츠들 중 사용 중인 항목 전부 비활성화
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Content c SET c.isUse = false WHERE c.parentId = :parentId AND c.isUse = true")
    void updateIsUseFalseByParentId(@Param("parentId") Long parentId);
}

package kr.co.itid.cms.repository.cms.core.content;

import kr.co.itid.cms.entity.cms.core.content.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ContentRepository extends JpaRepository<Content, Long>, ContentRepositoryCustom {

    /**
     * 특정 parentId 그룹의 콘텐츠 목록 (정렬 순서대로)
     */
    List<Content> findByParentIdOrderByCreatedDateAsc(Long parentId);

    /**
     * 대표 콘텐츠 및 그 하위 콘텐츠 전체 삭제 (parentId 또는 idx가 일치하는 경우)
     */
    void deleteAllByParentIdOrIdx(Long parentId, Long idx);

    /**
     * parentId 기준으로 사용 중인 콘텐츠 1건만 조회 (isUse = true)
     */
    Optional<Content> findFirstByParentIdAndIsUseTrue(Long parentId);

    /* ====== 메인 버전 활성화 (is_main) ====== */
    /** parentId 그룹 전체를 메인 해제 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Content c SET c.isMain = false WHERE c.parentId = :parentId")
    int updateIsMainFalseByParentId(@Param("parentId") Long parentId);

    /** 특정 idx만 메인 지정 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Content c SET c.isMain = true WHERE c.idx = :idx")
    int updateIsMainTrueByIdx(@Param("idx") Long idx);

    /* ====== 사용 플래그 일괄 동기화용 ====== */
    /**
     * contentId가 ids에 포함된 것만 is_use = true
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Content c SET c.isUse = true WHERE c.idx IN :ids")
    int updateIsUseTrueByContentIdIn(@Param("ids") Set<Long> ids);

    /**
     * contentId가 ids에 포함되지 않은 것만 is_use = false
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Content c SET c.isUse = false WHERE c.idx NOT IN :ids")
    int updateIsUseFalseByContentIdNotIn(@Param("ids") Set<Long> ids);
}

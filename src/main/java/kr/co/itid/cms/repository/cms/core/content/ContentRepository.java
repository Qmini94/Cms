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
    List<Content> findByParentIdOrderByCreatedDateDesc(Long parentId);

    /**
     * 대표 콘텐츠 및 그 하위 콘텐츠 전체 삭제 (parentId가 일치하는 경우)
     */
    void deleteAllByParentId(Long parentId);

    /**
     * parentId 기준으로 사용 중인 콘텐츠 1건만 조회 (isMain = true)
     */
    Optional<Content> findFirstByParentIdAndIsMainTrue(Long parentId);

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
    List<Content> findByIdxIn(@Param("ids") Set<Long> ids);

    // hostname 기준, 해당 parentId들만 ON
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Content c SET c.isUse = true " +
            "WHERE c.hostname = :hostname AND c.parentId IN :parentIds")
    int updateIsUseTrueByHostnameAndParentIdIn(@Param("hostname") String hostname,
                                               @Param("parentIds") Set<Long> parentIds);

    // hostname 기준, 위 parentId 외 나머지 전부 OFF
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Content c SET c.isUse = false " +
            "WHERE c.hostname = :hostname AND c.parentId NOT IN :parentIds")
    int updateIsUseFalseByHostnameAndParentIdNotIn(@Param("hostname") String hostname,
                                                   @Param("parentIds") Set<Long> parentIds);
}

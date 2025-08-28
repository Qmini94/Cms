package kr.co.itid.cms.service.cms.core.menu;

import java.util.List;

/**
 * 메뉴 조회 전용 서비스 (Read-Only)
 * - 다른 도메인에서 메뉴 경로/후손 조회가 필요할 때 사용합니다.
 * - 쓰기/변경 책임은 MenuService(명령)에서 수행하고,
 *   본 컴포넌트는 조회만 담당해 순환 의존을 방지합니다.
 */
public interface MenuQueryService {

    /**
     * 메뉴 ID로 pathId를 조회합니다.
     * 예: "1.3.8"
     *
     * @param menuId 메뉴 ID
     * @return pathId (없으면 null)
     * @throws Exception 조회 중 오류 발생 시
     */
    String getPathIdById(Long menuId) throws Exception;

    /**
     * pathId 프리픽스를 기준으로 모든 하위 메뉴의 ID 목록을 조회합니다.
     * 예: prefix = "1.3." → 1.3.* 하위 모든 메뉴 ID
     *
     * @param pathPrefix pathId 프리픽스
     * @return 하위 메뉴 ID 목록 (없으면 빈 리스트)
     * @throws Exception 조회 중 오류 발생 시
     */
    List<Long> getDescendantIdsByPathPrefix(String pathPrefix) throws Exception;
}

package kr.co.itid.cms.service.scheduler.visitor;

/**
 * 방문자 통계 마이그레이션 서비스 인터페이스입니다.
 * Redis 등 임시 저장소에 누적된 일일 방문자 수 데이터를
 * 정해진 시간에 영구 저장소로 이전하는 작업을 수행합니다.
 */
public interface VisitorMigrationService {

    /**
     * 일일 방문자 통계를 영구 저장소로 마이그레이션합니다.
     * 일반적으로 스케줄러에서 12시 정각에 호출되며,
     * Redis에 저장된 웹/모바일 방문자 수를 DB의 `visit_site` 테이블 등으로 이전합니다.
     */
    void migrateDailyVisitorStats() throws Exception;
}

package kr.co.itid.cms.service.cms.core.render;

import kr.co.itid.cms.dto.cms.core.render.response.RenderResponse;

/**
 * 렌더링 서비스 인터페이스
 *
 */
public interface RenderService {

    /**
     * 현재 사용자 컨텍스트(JWT 내 menuId 등)에 기반한 렌더 데이터(JSON)를 반환합니다.
     * 컨트롤러의 권한 체크(@PreAuthorize)는 컨트롤러에서 수행하며,
     * 로깅/예외 래핑 및 DB 접근/도메인 조합은 구현체에서 처리합니다.
     *
     * @return RenderResponse 렌더링에 필요한 타입/옵션/권한 데이터를 포함한 응답 DTO
     * @throws Exception 조회/매핑 중 예외 발생 시
     */
    RenderResponse getRenderData() throws Exception;
}

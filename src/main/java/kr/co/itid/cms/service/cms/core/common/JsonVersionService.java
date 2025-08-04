package kr.co.itid.cms.service.cms.core.common;

import kr.co.itid.cms.dto.cms.core.common.version.VersionListResponse;

/**
 * JSON 버전 관리 서비스 인터페이스
 * 도메인별 JSON 버전 파일의 목록, 활성화 여부, 수정, 삭제 등을 처리합니다.
 */
public interface JsonVersionService {

    /**
     * 주어진 도메인의 버전 파일 목록을 조회합니다.
     *
     * @param domain 도메인 이름 (예: "www", "admin")
     * @return 버전 파일 이름 목록 (예: "v1.json", "2024-08-01.json")
     * @throws Exception 조회 중 예외 발생 시
     */
    VersionListResponse getVersionFiles(String domain) throws Exception;

    /**
     * 주어진 도메인의 현재 활성화된 버전 파일명을 조회합니다.
     * (active.json의 내용)
     *
     * @param domain 도메인 이름
     * @return 활성화된 파일 이름 (예: "v1.json")
     * @throws Exception 조회 중 예외 발생 시
     */
    String getActiveFile(String domain) throws Exception;

    /**
     * 특정 버전 파일의 내용을 조회합니다.
     *
     * @param domain 도메인 이름
     * @param fileName 파일 이름 (예: "v1.json")
     * @return 파일 내 JSON 문자열
     * @throws Exception 조회 중 예외 발생 시
     */
    String readJsonContent(String domain, String fileName) throws Exception;

    /**
     * 특정 버전 파일을 활성화(active.json에 기록)합니다.
     *
     * @param domain 도메인 이름
     * @param fileName 활성화할 파일 이름
     * @throws Exception 처리 중 예외 발생 시
     */
    void activateVersion(String domain, String fileName) throws Exception;

    /**
     * 특정 버전 파일을 삭제합니다.
     *
     * @param domain 도메인 이름
     * @param fileName 삭제할 파일 이름
     * @throws Exception 처리 중 예외 발생 시
     */
    void deleteVersion(String domain, String fileName) throws Exception;
}
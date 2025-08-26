package kr.co.itid.cms.service.cms.core.menu;

import kr.co.itid.cms.dto.cms.core.menu.response.VersionListResponse;
import kr.co.itid.cms.dto.cms.core.menu.request.MenuRequest;

import java.util.List;

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
     * 특정 메뉴 버전 파일을 읽어 DB에 동기화한 후, 해당 버전을 활성화 상태로 기록합니다.
     *
     * &lt;p&gt;
     * 이 메서드는 다음 작업을 수행합니다:
     * &lt;ol&gt;
     *     &lt;li&gt;지정된 메뉴 버전(JSON) 파일을 읽어 메뉴 트리를 파싱합니다.&lt;/li&gt;
     *     &lt;li&gt;해당 메뉴 트리를 DB에 저장(동기화)합니다.&lt;/li&gt;
     *     &lt;li&gt;DB 저장이 성공한 경우에만 active.json에 해당 버전을 활성화 상태로 기록합니다.&lt;/li&gt;
     * &lt;/ol&gt;
     * &lt;/p&gt;
     *
     * &lt;p&gt;
     * 만약 DB 저장 도중 오류가 발생하면, active.json은 수정되지 않습니다.
     * &lt;/p&gt;
     *
     * @param domain 도메인 이름 (driveName과 동일)
     * @param fileName 활성화할 메뉴 버전 파일 이름
     * @throws Exception 파일 읽기, JSON 파싱, DB 저장 또는 기록 중 예외 발생 시
     */
    void activateVersion(String domain, String fileName) throws Exception;

    /**
     * 특정 드라이브 이름을 기준으로 전체 메뉴 트리를 새로운 버전파일으로 저장합니다.
     *
     * @param driveName 드라이브 이름 (예: www, admin)
     * @param tree 저장할 메뉴 트리
     * @throws Exception 처리 중 오류 발생 시
     */
    void saveTree(String driveName, List<MenuRequest> tree) throws Exception;

    /**
     * 특정 버전 파일을 삭제합니다.
     *
     * @param domain 도메인 이름
     * @param fileName 삭제할 파일 이름
     * @throws Exception 처리 중 예외 발생 시
     */
    void deleteVersion(String domain, String fileName) throws Exception;
}
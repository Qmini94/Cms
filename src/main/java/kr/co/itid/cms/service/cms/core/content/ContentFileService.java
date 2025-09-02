package kr.co.itid.cms.service.cms.core.content;

import kr.co.itid.cms.dto.cms.core.content.response.UploadedFileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 콘텐츠 파일 관리 서비스 인터페이스입니다.
 * 저장 경로 규칙: ./public/contents/cms/{parentId}/{fileName}
 * 공개 URL 규칙:  /contents/cms/{parentId}/{fileName}
 *
 * 업로드, 조회, 삭제 기능을 제공합니다.
 */
public interface ContentFileService {

    /**
     * parentId 기준으로 업로드된 파일 목록을 조회합니다.
     *
     * @param parentId 파일 그룹(콘텐츠 parentId)
     * @return 업로드된 파일 정보 목록
     * @throws Exception 파일 시스템 접근 실패 등
     */
    List<UploadedFileResponse> listFiles(Long parentId) throws Exception;

    /**
     * 여러 파일을 업로드합니다. (파일명 sanitize/중복시 유니크 처리는 구현체 책임)
     *
     * @param parentId 파일 그룹(콘텐츠 parentId)
     * @param files 업로드할 파일 목록
     * @return 저장된 파일 정보 목록
     * @throws Exception 저장 실패, 용량/타입 제약 위반 등
     */
    List<UploadedFileResponse> saveFiles(Long parentId, List<MultipartFile> files) throws Exception;


    /**
     * 업로드된 파일을 삭제합니다.
     *
     * @param parentId 파일 그룹(콘텐츠 parentId)
     * @param filename 삭제할 파일명
     * @throws Exception 파일 미존재, 권한/경로 오류 등
     */
    void deleteFile(Long parentId, String filename) throws Exception;
}

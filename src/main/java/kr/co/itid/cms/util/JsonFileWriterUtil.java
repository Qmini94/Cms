package kr.co.itid.cms.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class JsonFileWriterUtil {

    @Value("${json.path}")
    private String basePath;
    private final ObjectMapper objectMapper;

    public void writeJsonFile(String domain, String fileName, Object data, boolean versioned) {
        Path domainDir = Paths.get(basePath, domain);
        Path targetFile = domainDir.resolve(fileName + ".json");

        try {
            // 1. 도메인 폴더 확인 및 생성
            if (!Files.exists(domainDir)) {
                Files.createDirectories(domainDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("JSON 도메인 디렉토리 생성 실패: " + domainDir, e);
        }

        try {
            // 2. 기존 파일 백업 (버전 관리)
            if (versioned && Files.exists(targetFile)) {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                Path backupFile = domainDir.resolve(fileName + "_" + timestamp + ".json");
                Files.move(targetFile, backupFile);
            }
        } catch (IOException e) {
            throw new RuntimeException("기존 JSON 파일 백업 실패: " + targetFile, e);
        }

        try {
            // 3. JSON 직렬화 및 저장
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            Files.writeString(targetFile, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("JSON 직렬화 실패: 객체를 JSON 문자열로 변환할 수 없습니다", e);
        } catch (IOException e) {
            throw new RuntimeException("JSON 파일 쓰기 실패: " + targetFile, e);
        }
    }
}
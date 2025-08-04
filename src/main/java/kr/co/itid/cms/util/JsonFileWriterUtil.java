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

    public void writeJsonFile(String domain, String fileNamePrefix, Object data, boolean versioned) {
        Path domainDir = Paths.get(basePath, domain);

        try {
            if (!Files.exists(domainDir)) {
                Files.createDirectories(domainDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("JSON 도메인 디렉토리 생성 실패: " + domainDir, e);
        }

        String fileName = versioned
                ? fileNamePrefix + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".json"
                : fileNamePrefix + ".json";

        Path targetFile = domainDir.resolve(fileName);

        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            Files.writeString(targetFile, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("JSON 파일 쓰기 실패: " + targetFile, e);
        }
    }
}
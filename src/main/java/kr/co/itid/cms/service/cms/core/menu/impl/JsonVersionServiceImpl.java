package kr.co.itid.cms.service.cms.core.menu.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuTreeLiteResponse;
import kr.co.itid.cms.dto.cms.core.menu.response.VersionListResponse;
import kr.co.itid.cms.dto.cms.core.menu.request.MenuRequest;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.service.cms.core.menu.JsonVersionService;
import kr.co.itid.cms.service.cms.core.menu.MenuService;
import kr.co.itid.cms.util.JsonFileWriterUtil;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("jsonVersionService")
@RequiredArgsConstructor
public class JsonVersionServiceImpl extends EgovAbstractServiceImpl implements JsonVersionService {

    private final MenuService menuService;
    private final JsonFileWriterUtil jsonFileWriterUtil;
    private final LoggingUtil loggingUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${json.path}")
    private String basePath;

    private Path getMenuPath() {
        return Paths.get(basePath, "menu");
    }

    private Path getVersionFilePath(String fileName) {
        return getMenuPath().resolve(fileName);
    }

    private Path getActiveFilePath() {
        return getMenuPath().resolve("active.json");
    }

    @Override
    public VersionListResponse getVersionFiles(String domain) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "[Version List] domain=" + domain);

        Path menuDir = getMenuPath();
        if (!Files.exists(menuDir)) Files.createDirectories(menuDir);

        try (Stream<Path> stream = Files.list(menuDir)) {
            List<String> result = stream
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.startsWith("menu_" + domain + "_") && name.endsWith(".json"))
                    .sorted()
                    .collect(Collectors.toList());

            String activeFileName = getActiveFile(domain);

            loggingUtil.logSuccess(Action.RETRIEVE, "[Version List] Loaded successfully: count=" + result.size());

            return new VersionListResponse(result, activeFileName);
        } catch (IOException e) {
            loggingUtil.logFail(Action.RETRIEVE, "[Version List] I/O error: " + e.getMessage());
            throw processException("Failed to load version list", e);
        }
    }

    @Override
    public String getActiveFile(String domain) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "[Get Active Version] domain=" + domain);
        Path activePath = getActiveFilePath();

        try {
            if (!Files.exists(activePath)) return null;

            String rawJson = Files.readString(activePath, StandardCharsets.UTF_8);
            Map<String, String> activeMap = objectMapper.readValue(rawJson, new TypeReference<>() {});
            String fileName = activeMap.get(domain);

            if (fileName == null) return null;

            loggingUtil.logSuccess(Action.RETRIEVE, "[Get Active Version] Active file: " + fileName);
            return fileName;
        } catch (IOException e) {
            loggingUtil.logFail(Action.RETRIEVE, "[Get Active Version] I/O error: " + e.getMessage());
            throw processException("Failed to get active version", e);
        }
    }

    @Override
    public String readJsonContent(String domain, String fileName) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "[Read JSON File] domain=" + domain + ", fileName=" + fileName);
        Path filePath = getVersionFilePath(fileName);

        try {
            if (!Files.exists(filePath)) {
                throw processException("File not found: " + fileName);
            }

            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            loggingUtil.logSuccess(Action.RETRIEVE, "[Read JSON File] Read successful: " + fileName);
            return content;
        } catch (IOException e) {
            loggingUtil.logFail(Action.RETRIEVE, "[Read JSON File] I/O error: " + e.getMessage());
            throw processException("Failed to read JSON file", e);
        }
    }

    @Override
    public void activateVersion(String domain, String fileName) throws Exception {
        loggingUtil.logAttempt(Action.UPDATE, "[Activate Version] domain=" + domain + ", fileName=" + fileName);

        try {
            String json = readJsonContent(domain, fileName);
            List<MenuRequest> tree = objectMapper.readValue(json, new TypeReference<>() {});

            // 2. DB 반영
            menuService.syncMenuTree(domain, tree); // domain == driveName

            // 2-1) DB에서 '정규화된' children 배열 재조회 (기존 서비스 메서드 재사용)
            List<MenuTreeLiteResponse> liteChildren = menuService.getMenuTreeLiteByName(domain);

            // 2-2) 프론트 저장 규격(MenuRequest[])으로 맞추고 싶으면 convertValue로 1:1 매핑
            // (필드명이 동일/유사하면 별도 매퍼 없이 Jackson이 변환)
            List<MenuRequest> normalizedChildren = objectMapper.convertValue(
                    liteChildren, new TypeReference<>() {}
            );

            // 2-3) 해당 버전 파일을 정규화본으로 "덮어쓰기" (음수 ID 제거본)
            Path versionPath = getVersionFilePath(fileName);
            String normalizedJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(normalizedChildren);
            Files.writeString(
                    versionPath,
                    normalizedJson,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            // 3. 성공 시에만 활성화 처리
            Path activePath = getActiveFilePath();
            Map<String, String> activeMap = new HashMap<>();

            if (Files.exists(activePath)) {
                String activeJson = Files.readString(activePath, StandardCharsets.UTF_8);
                activeMap = objectMapper.readValue(activeJson, new TypeReference<>() {});
            }

            activeMap.put(domain, fileName);
            Files.writeString(
                    activePath,
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(activeMap),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            loggingUtil.logSuccess(Action.UPDATE, "[Activate Version] Activated: " + fileName);

        } catch (IOException e) {
            loggingUtil.logFail(Action.UPDATE, "[Activate Version] File read/write error: " + e.getMessage());
            throw processException("파일 처리 중 오류 발생", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "[Activate Version] DB sync failed: " + e.getMessage());
            throw processException("메뉴 DB 동기화 중 오류 발생", e);
        }
    }

    @Override
    public void saveTree(String driveName, List<MenuRequest> tree) throws Exception {
        loggingUtil.logAttempt(Action.CREATE, "Try to save menu tree JSON for drive: " + driveName);

        try {
            // JSON 파일 이름: menu_<driveName>.json
            String fileName = "menu_" + driveName;

            // JSON 파일 쓰기
            jsonFileWriterUtil.writeJsonFile("menu", fileName, tree, true);

            loggingUtil.logSuccess(Action.CREATE, "Saved menu tree JSON for drive: " + driveName);

        } catch (Exception e) {
            loggingUtil.logFail(Action.CREATE, "Failed to save menu tree JSON for drive: " + driveName);
            throw processException("메뉴 JSON 파일 저장 실패", e);
        }
    }

    @Override
    public void deleteVersion(String domain, String fileName) throws Exception {
        loggingUtil.logAttempt(Action.DELETE, "[Delete Version] domain=" + domain + ", fileName=" + fileName);
        Path filePath = getVersionFilePath(fileName);

        try {
            if (!Files.exists(filePath)) {
                loggingUtil.logFail(Action.DELETE, "[Delete Version] File not found: " + fileName);
                throw processException("File does not exist: " + fileName);
            }

            Files.delete(filePath);
            loggingUtil.logSuccess(Action.DELETE, "[Delete Version] Deleted: " + fileName);
        } catch (IOException e) {
            loggingUtil.logFail(Action.DELETE, "[Delete Version] I/O error: " + e.getMessage());
            throw processException("Failed to delete version", e);
        }
    }
}
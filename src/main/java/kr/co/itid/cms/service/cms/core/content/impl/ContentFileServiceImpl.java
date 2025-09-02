package kr.co.itid.cms.service.cms.core.content.impl;

import kr.co.itid.cms.dto.cms.core.content.response.UploadedFileResponse;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.service.cms.core.content.ContentFileService;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("contentFileService")
@RequiredArgsConstructor
public class ContentFileServiceImpl extends EgovAbstractServiceImpl implements ContentFileService {

    private final LoggingUtil loggingUtil;

    @Value("${content.files.base-path}")
    private String baseDir;

    /** 공개 URL prefix */
    @Value("${content.files.public-url-prefix}")
    private String publicUrlPrefix;

    /** 허용 확장자 */
    private static final Set<String> ALLOWED_EXT = new HashSet<>(Set.of("jpg","jpeg","png","gif","webp","svg","pdf"));

    /** 최대 업로드 크기: 25MB */
    private static final long MAX_BYTES = 25L * 1024 * 1024;

    /* ===================== public APIs ===================== */

    @Override
    public List<UploadedFileResponse> listFiles(Long parentId) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "[Files] list parentId=" + parentId);
        Path dir = resolveParentDir(parentId);

        if (!Files.exists(dir)) {
            loggingUtil.logSuccess(Action.RETRIEVE, "[Files] list empty (dir not exists)");
            return new ArrayList<>();
        }

        try (Stream<Path> stream = Files.list(dir)) {
            List<UploadedFileResponse> list = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().startsWith(".")) // dotfiles 제외
                    .map(p -> {
                        try {
                            return toResponse(parentId, p);
                        } catch (Exception ex) {
                            loggingUtil.logFail(Action.RETRIEVE, "[Files] stat fail: " + p + " - " + ex.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(UploadedFileResponse::getFilename))
                    .collect(Collectors.toList());

            loggingUtil.logSuccess(Action.RETRIEVE, "[Files] list count=" + list.size());
            return list;
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "[Files] list error: " + e.getMessage());
            throw processException("파일 목록 조회 실패", e);
        }
    }

    @Override
    public List<UploadedFileResponse> saveFiles(Long parentId, List<MultipartFile> files) throws Exception {
        int count = (files == null) ? 0 : files.size();
        loggingUtil.logAttempt(Action.CREATE, "[Files] upload count=" + count + ", parentId=" + parentId);

        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        Path dir = resolveParentDir(parentId);
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (Exception e) {
            loggingUtil.logFail(Action.CREATE, "[Files] mkdir error: " + e.getMessage());
            throw processException("디렉토리 생성 실패", e);
        }

        List<UploadedFileResponse> saved = new ArrayList<>();
        for (MultipartFile mf : files) {
            if (mf == null || mf.isEmpty()) continue;
            try {
                saved.add(saveOne(parentId, dir, mf));
            } catch (Exception e) {
                loggingUtil.logFail(Action.CREATE, "[Files] save one failed: " +
                        mf.getOriginalFilename() + " - " + e.getMessage());
            }
        }

        loggingUtil.logSuccess(Action.CREATE, "[Files] saved count=" + saved.size());
        return saved;
    }

    @Override
    public void deleteFile(Long parentId, String filename) throws Exception {
        loggingUtil.logAttempt(Action.DELETE, "[Files] delete parentId=" + parentId + ", filename=" + filename);
        String safe = sanitizeFilename(filename);
        Path dir = resolveParentDir(parentId);
        Path target = dir.resolve(safe).normalize();

        if (!target.startsWith(dir)) {
            loggingUtil.logFail(Action.DELETE, "[Files] path traversal detected: " + filename);
            throw processException("허용되지 않은 경로입니다.");
        }

        try {
            if (!Files.exists(target) || !Files.isRegularFile(target)) {
                throw processException("파일이 존재하지 않습니다: " + safe);
            }
            Files.delete(target);
            loggingUtil.logSuccess(Action.DELETE, "[Files] deleted: " + safe);
        } catch (Exception e) {
            loggingUtil.logFail(Action.DELETE, "[Files] delete error: " + e.getMessage());
            throw processException("파일 삭제 실패", e);
        }
    }

    /* ===================== internal helpers ===================== */

    private Path resolveParentDir(Long parentId) {
        if (parentId == null || parentId <= 0) {
            throw new IllegalArgumentException("parentId는 1 이상의 값이어야 합니다.");
        }
        return Paths.get(baseDir, String.valueOf(parentId));
    }

    private UploadedFileResponse saveOne(Long parentId, Path dir, MultipartFile mf) throws Exception {
        String original = Optional.ofNullable(mf.getOriginalFilename()).orElse("noname");
        String safeName = sanitizeFilename(original);

        String ext = getExtLower(safeName);
        if (!ALLOWED_EXT.contains(ext)) {
            throw processException("허용되지 않은 파일 형식입니다: " + ext);
        }
        if (mf.getSize() > MAX_BYTES) {
            throw processException("파일 용량이 초과되었습니다(최대 25MB).");
        }

        String finalName = uniqueFilename(dir, safeName);
        Path target = dir.resolve(finalName).normalize();

        if (!target.startsWith(dir)) {
            throw processException("허용되지 않은 경로입니다.");
        }

        try {
            mf.transferTo(target.toFile());

            return toResponse(parentId, target);
        } catch (Exception e) {
            // (선택) 예외 클래스도 로그에 남기면 디버깅 편함
            loggingUtil.logFail(Action.CREATE,
                    "[Files] transferTo failed: " + safeName
                            + " - ex=" + e.getClass().getName()
                            + ", msg=" + String.valueOf(e.getMessage()));
            throw processException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    /** Path → 응답 DTO 변환 */
    private UploadedFileResponse toResponse(Long parentId, Path file) throws Exception {
        String name = file.getFileName().toString();
        long size = Files.size(file);
        FileTime ft = Files.getLastModifiedTime(file);
        String uploadedAt = formatIso(ft.toMillis());
        String url = publicUrl(parentId, name);

        return UploadedFileResponse.builder()
                .filename(name)
                .size(size)
                .uploadedAt(uploadedAt)
                .url(url)
                .build();
    }

    private String publicUrl(Long parentId, String filename) {
        return String.format("%s/%d/%s", trimTrailingSlash(publicUrlPrefix), parentId, filename);
    }

    private static String trimTrailingSlash(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

    private static String sanitizeFilename(String name) {
        String n = name.replace("\\", "_")
                .replace("/", "_")
                .replaceAll("[\\r\\n\\t]", "_")
                .replaceAll("[:*?\"<>|]+", "_");
        int max = 150;
        if (n.length() > max) {
            int dot = n.lastIndexOf('.');
            if (dot > 0 && dot < n.length() - 1) {
                String base = n.substring(0, dot);
                String ext = n.substring(dot);
                if (base.length() > 120) base = base.substring(0, 120);
                n = base + ext;
            } else {
                n = n.substring(0, max);
            }
        }
        return n;
    }

    private static String getExtLower(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot >= 0 && dot < filename.length() - 1)
                ? filename.substring(dot + 1).toLowerCase(Locale.ROOT)
                : "";
    }

    private static String uniqueFilename(Path dir, String filename) {
        String base;
        String extWithDot;
        int dot = filename.lastIndexOf('.');
        if (dot >= 0) {
            base = filename.substring(0, dot);
            extWithDot = filename.substring(dot);
        } else {
            base = filename;
            extWithDot = "";
        }

        Path candidate = dir.resolve(filename);
        int i = 1;
        while (Files.exists(candidate)) {
            String next = base + "_" + i + extWithDot;
            candidate = dir.resolve(next);
            i++;
        }
        return candidate.getFileName().toString();
    }

    private static String formatIso(long epochMillis) {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
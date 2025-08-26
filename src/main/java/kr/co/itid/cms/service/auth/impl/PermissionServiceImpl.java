package kr.co.itid.cms.service.auth.impl;

import kr.co.itid.cms.config.security.JwtTokenProvider;
import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.dto.auth.UserPermissionResponse;
import kr.co.itid.cms.dto.auth.permission.PermissionEntryDto;
import kr.co.itid.cms.dto.auth.permission.PermissionSubjectDto;
import kr.co.itid.cms.dto.auth.permission.SubjectType;
import kr.co.itid.cms.dto.auth.permission.response.PermissionChainResponse;
import kr.co.itid.cms.entity.cms.core.menu.Menu;
import kr.co.itid.cms.entity.cms.core.permission.Permission;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.repository.cms.core.menu.MenuRepository;
import kr.co.itid.cms.repository.cms.core.permission.PermissionRepository;
import kr.co.itid.cms.service.auth.PermissionResolverService;
import kr.co.itid.cms.service.auth.PermissionService;
import kr.co.itid.cms.service.auth.model.MenuPermissionData;
import kr.co.itid.cms.service.auth.model.PermissionEntry;
import kr.co.itid.cms.service.cms.core.board.DynamicBoardService;
import kr.co.itid.cms.service.cms.core.member.MemberService;
import kr.co.itid.cms.util.LoggingUtil;
import kr.co.itid.cms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static kr.co.itid.cms.constanrt.LevelConstants.LEVEL_NAME_MAP;
import static kr.co.itid.cms.constanrt.PermissionConstants.*;
import static kr.co.itid.cms.constanrt.RedisConstants.PERMISSION_KEY_PREFIX;
import static kr.co.itid.cms.util.PermissionKeyUtil.getBool;
import static kr.co.itid.cms.util.PermissionKeyUtil.normalizePermissions;
import static kr.co.itid.cms.util.PermissionKeyUtil.putYN;
import static kr.co.itid.cms.util.PermissionKeyUtil.yn;

@Service("permService")
@RequiredArgsConstructor
public class PermissionServiceImpl extends EgovAbstractServiceImpl implements PermissionService {

    private final LoggingUtil loggingUtil;
    private final JwtTokenProvider jwtTokenProvider;
    private final PermissionResolverService permissionResolverService;
    private final MemberService memberService;
    private final DynamicBoardService dynamicBoardService;

    private final MenuRepository menuRepository;
    private final PermissionRepository permissionRepository;

    // Resolver와 동일한 캐시 스키마 사용
    private final RedisTemplate<String, MenuPermissionData> redisTemplate;

    /* ================= 런타임 권한 ================= */

    // 추가: 오버로드 (글 단위 검사)
    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public boolean hasAccess(String permission, Long postId) throws Exception {
        return hasAccessInternal(permission, postId);
    }

    // 기존 메서드는 내부 공통 로직 호출로 변경(동작 동일)
    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public boolean hasAccess(String permission) throws Exception {
        return hasAccessInternal(permission, null);
    }

    // 내부 공통 로직
    private boolean hasAccessInternal(String permission, Long postId) throws Exception {
        try {
            JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
            jwtTokenProvider.refreshIfNeeded(user);
            user = SecurityUtil.getCurrentUser();

            loggingUtil.logAttempt(Action.RETRIEVE,
                    "Check access: user=" + user.userId()
                            + ", menuId=" + user.menuId()
                            + ", perm=" + permission
                            + (postId != null ? (", postId=" + postId) : ""));

            // 1) 관리자면 바로 허용
            if (user.isAdmin()) {
                loggingUtil.logSuccess(Action.RETRIEVE, "Admin override");
                return true;
            }

            // 2) 메뉴 권한(체인) 충족 여부
            boolean granted = permissionResolverService.hasPermission(user, permission);
            if (!granted) {
                loggingUtil.logFail(Action.RETRIEVE, "Denied (menu permission)");
                return false;
            }

            // 3) 글 단위 권한: MODIFY/REMOVE는 본인 글만 허용 (postId가 있을 때만)
            if (postId != null && ("MODIFY".equalsIgnoreCase(permission) || "REMOVE".equalsIgnoreCase(permission))) {
                boolean own = isOwner(user, postId);
                if (!own) {
                    loggingUtil.logFail(Action.RETRIEVE, "Denied (not owner)");
                    return false;
                }
                loggingUtil.logSuccess(Action.RETRIEVE, "Granted (owner)");
                return true;
            }

            loggingUtil.logSuccess(Action.RETRIEVE, "Granted");
            return true;

        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Access check error: " + e.getMessage());
            throw processException("권한 확인 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 소유자 확인 훅 메서드.
     */
    protected boolean isOwner(JwtAuthenticatedUser user, Long postId) {
        try {
            String regId = dynamicBoardService.getRegIdByBoard(postId);
            if (regId == null) return false;

            return !regId.isEmpty()
                    ? regId.equals(user.userId())
                    : false;

        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Owner check failed: " + e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public UserPermissionResponse getPermissionByMenu(JwtAuthenticatedUser user) throws Exception {
        final Long menuId = user.menuId();
        loggingUtil.logAttempt(Action.RETRIEVE, "Resolve permissions: user=" + user.userId() + ", menuId=" + menuId);
        try {
            if (user.userLevel() == 1) {
                loggingUtil.logSuccess(Action.RETRIEVE, "Admin full permission");
                return UserPermissionResponse.builder()
                        .view(true).write(true).modify(true).remove(true)
                        .manage(true).access(true).reply(true)
                        .build();
            }

            PermissionEntry entry = permissionResolverService.resolvePermissions(user);
            loggingUtil.logSuccess(Action.RETRIEVE, "Resolved");

            return UserPermissionResponse.builder()
                    .access(entry.getPermissions().contains(ACCESS))
                    .manage(entry.getPermissions().contains(MANAGE))
                    .view  (entry.getPermissions().contains(VIEW))
                    .write (entry.getPermissions().contains(WRITE))
                    .modify(entry.getPermissions().contains(MODIFY))
                    .remove(entry.getPermissions().contains(REMOVE))
                    .reply (entry.getPermissions().contains(REPLY))
                    .build();
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Resolve failed: " + e.getMessage());
            throw processException("권한 해석 중 오류가 발생했습니다.", e);
        }
    }

    /* ============== 관리: 체인 조회/업서트 ============== */

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public PermissionChainResponse getPermissionChain(Long menuId, String pathId) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Load chain (single query): menuId=" + menuId + ", pathId=" + pathId);
        try {
            // 1) 전체 경로(현재 포함) 확보
            final List<Long> fullPath = getFullPathIds(menuId, pathId);
            if (fullPath.isEmpty()) {
                throw new IllegalStateException("메뉴 경로가 비어있습니다: menuId=" + menuId);
            }

            // 2) 가까운 상위 우선의 조상 목록(현재 제외 후 reverse)
            final List<Long> ancestors = new ArrayList<>(fullPath);
            if (!ancestors.isEmpty() && Objects.equals(ancestors.get(ancestors.size() - 1), menuId)) {
                ancestors.remove(ancestors.size() - 1);
            }
            Collections.reverse(ancestors); // 가까운 상위 먼저

            // 3) 한 번만 조회
            final List<Permission> all = permissionRepository.findAllByMenuIds(fullPath);

            // ID → 이름 맵을 한 번만 생성
            final Map<String, String> idNameMap = buildIdNameMap(all);

            // 4) 현재/상속 분리
            final List<Permission> current = all.stream()
                    .filter(p -> Objects.equals(p.getMenuId(), menuId))
                    .sorted(Comparator.comparing(p -> Optional.ofNullable(p.getSort()).orElse(0)))
                    .collect(toList());

            final List<Permission> inherited = all.stream()
                    .filter(p -> !Objects.equals(p.getMenuId(), menuId))
                    .collect(toList());

            // 5) 매핑 + 정규화
            final List<PermissionEntryDto> currentDtos = current.stream()
                    .map(e -> toDto(e, idNameMap))
                    .map(this::normalizeEntry) // 반환값 사용
                    .collect(toList());

            // 가까운 상위 우선으로 dedupe(동일 subject 한 번만)
            final List<PermissionEntryDto> inheritedDtos =
                    mergeNearestAncestorFirst(inherited, ancestors, idNameMap);

            loggingUtil.logSuccess(Action.RETRIEVE,
                    "Chain loaded: current=" + currentDtos.size() + ", inherited=" + inheritedDtos.size());
            return PermissionChainResponse.builder()
                    .current(currentDtos)
                    .inherited(inheritedDtos)
                    .build();

        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "DB error: " + e.getMessage());
            throw processException("권한 체인 조회 중 DB 오류가 발생했습니다.", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error: " + e.getMessage());
            throw processException("권한 체인 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void upsertPermissions(Long menuId, List<PermissionEntryDto> entries) throws Exception {
        loggingUtil.logAttempt(Action.UPDATE, "Upsert permissions: menuId=" + menuId);
        try {
            // 1) 입력 리스트를 final로 고정
            final List<PermissionEntryDto> src = (entries == null) ? Collections.emptyList() : entries;

            // 2) 정규화 + sort 보정 → normalized 사용
            final List<PermissionEntryDto> normalized = IntStream.range(0, src.size())
                    .mapToObj(i -> withFixedSort(normalizeEntry(src.get(i)), i))
                    .collect(Collectors.toList());

            // 기존 엔트리 로드
            List<Permission> existing = permissionRepository.findByMenuId(menuId);
            Map<String, Permission> existingByPair = existing.stream()
                    .collect(Collectors.toMap(this::pairKey, p -> p, (a, b) -> a, LinkedHashMap::new));

            // 생존 페어 키 집합
            Set<String> alivePairs = new LinkedHashSet<>();

            // 업서트 (normalized 사용)
            for (PermissionEntryDto dto : normalized) {
                String type  = toEntityTypeOrInfer(dto);
                String value = String.valueOf(dto.getSubject().getValue());
                String pair  = pairKey(menuId, type, value);
                alivePairs.add(pair);

                Permission entity = existingByPair.get(pair);
                if (entity == null) {
                    entity = new Permission();
                    entity.setMenuId(menuId);
                    entity.setType(type);
                    entity.setValue(value);
                }
                fillEntityFromDto(entity, dto);
                permissionRepository.save(entity);
            }

            // 삭제(요청에 없는 기존 엔트리 제거)
            List<Permission> toDelete = existing.stream()
                    .filter(p -> !alivePairs.contains(pairKey(p)))
                    .collect(toList());
            if (!toDelete.isEmpty()) {
                permissionRepository.deleteAllInBatch(toDelete);
            }

            // 캐시 무효화 (하위 메뉴 전파 무효화)
            invalidateMenuPermission(menuId);

            loggingUtil.logSuccess(Action.UPDATE,
                    "Upsert success: menuId=" + menuId + ", saved=" + normalized.size() + ", deleted=" + toDelete.size());
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.UPDATE, "DB error during upsert: " + e.getMessage());
            throw processException("권한 저장 중 DB 오류가 발생했습니다.", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "Unexpected error during upsert: " + e.getMessage());
            throw processException("권한 저장 중 오류가 발생했습니다.", e);
        }
    }

    /* ================== 매핑/유틸 ================== */

    /** menuId, pathId를 이용해 전체 pathId 리스트([1,3,8]) 생성 */
    private List<Long> getFullPathIds(Long menuId, String pathId) {
        if (pathId != null && !pathId.isBlank()) {
            return parsePathAll(pathId);
        }
        // DB에서 Menu.pathId 조회
        String dbPath = menuRepository.findById(menuId)
                .map(Menu::getPathId)
                .orElseThrow(() -> new NoSuchElementException("Menu not found: " + menuId));
        return parsePathAll(dbPath);
    }

    /** "1.3.8" → [1,3,8] */
    private List<Long> parsePathAll(String pathId) {
        if (pathId == null || pathId.isBlank()) return List.of();
        return Arrays.stream(pathId.split("\\."))
                .filter(s -> s != null && !s.isBlank())
                .map(s -> {
                    try { return Long.parseLong(s.trim()); }
                    catch (NumberFormatException e) { return null; }
                })
                .filter(Objects::nonNull)
                .collect(toList());
    }

    /** Entity → DTO (value 사용, name 채우기) */
    private PermissionEntryDto toDto(Permission e, Map<String, String> idNameMap) {
        SubjectType type = "id".equalsIgnoreCase(e.getType()) ? SubjectType.ID : SubjectType.LEVEL;

        final String value = String.valueOf(e.getValue());
        final String name  = resolveSubjectName(type, value, idNameMap);

        PermissionSubjectDto subject = PermissionSubjectDto.builder()
                .type(type)
                .value(value)
                .name(name)
                .build();

        Map<String, Boolean> perms = new LinkedHashMap<>();
        putYN(perms, VIEW,   e.getView());
        putYN(perms, WRITE,  e.getWrite());
        putYN(perms, MODIFY, e.getModify());
        putYN(perms, REMOVE, e.getRemove());
        putYN(perms, MANAGE, e.getManage());
        putYN(perms, ACCESS, e.getAccess());
        putYN(perms, REPLY,  e.getReply());
        putYN(perms, ADMIN,  e.getAdmin());

        return PermissionEntryDto.builder()
                .subject(subject)
                .sort(e.getSort() != null ? e.getSort() : 0)
                .permissions(perms)
                .build();
    }

    private String resolveSubjectName(SubjectType type, String value, java.util.Map<String, String> idNameMap) {
        if (type == SubjectType.ID) {
            return idNameMap != null ? idNameMap.getOrDefault(value, "unknown") : "unknown";
        }
        // LEVEL: 매핑 없으면 "레벨 {value}"로 표시
        String named = LEVEL_NAME_MAP.get(value);
        return (named != null) ? named : ("레벨 " + value);
    }

    /** Permission 리스트에서 ID 타입만 모아 회원 이름을 배치로 조회 */
    private Map<String, String> buildIdNameMap(Collection<Permission> permissions) {
        if (permissions == null || permissions.isEmpty()) return Map.of();

        // type = "id" 인 엔트리의 value 수집
        Set<String> ids = permissions.stream()
                .filter(p -> p.getType() != null && p.getValue() != null)
                .filter(p -> "id".equalsIgnoreCase(p.getType()))
                .map(Permission::getValue)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (ids.isEmpty()) return Map.of();

        Map<String, String> result = new LinkedHashMap<>();
        try {
            // 키: 회원ID(String), 값: 표시명(String)
            Map<String, String> fetched = memberService.getDisplayNamesByIds(ids);
            if (fetched != null) result.putAll(fetched);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Failed to fetch usernames (ids=" + ids.size() + "): " + e.getMessage());
        } finally {
            // 누락/실패 모두 기본값으로 보정
            for (String id : ids) {
                result.putIfAbsent(id, "unknown");
            }
        }
        return result;
    }

    private void fillEntityFromDto(Permission entity, PermissionEntryDto dto) {
        entity.setSort(dto.getSort());

        Map<String, Boolean> p = Optional.ofNullable(dto.getPermissions()).orElse(Map.of());
        entity.setView  (yn(getBool(p, VIEW)));
        entity.setWrite (yn(getBool(p, WRITE)));
        entity.setModify(yn(getBool(p, MODIFY)));
        entity.setRemove(yn(getBool(p, REMOVE)));
        entity.setManage(yn(getBool(p, MANAGE))); // MANAGE or ADMIN
        entity.setAccess(yn(getBool(p, ACCESS)));
        entity.setReply (yn(getBool(p, REPLY)));
        entity.setAdmin (yn(getBool(p, ADMIN)));
    }

    /** dto 정규화: 동의어 → 정규키, 허용 키만 유지 (key 필드 의존 제거) */
    private PermissionEntryDto normalizeEntry(PermissionEntryDto dto) {
        if (dto == null) return null;
        if (dto.getSubject() == null
                || dto.getSubject().getType() == null
                || dto.getSubject().getValue() == null)
        {
            throw new IllegalArgumentException("subject.type 및 subject.value 는 필수입니다.");
        }

        Map<String, Boolean> out = normalizePermissions(dto.getPermissions(), true); // ASSIGNEE 허용
        return PermissionEntryDto.builder()
                .subject(dto.getSubject())
                .sort(dto.getSort())
                .permissions(out)
                .build();
    }

    /** sort 보정 (음수/누락 시 인덱스로 대체) */
    private PermissionEntryDto withFixedSort(PermissionEntryDto dto, int index) {
        int sort = (dto.getSort() < 0) ? index : dto.getSort();
        if (sort == dto.getSort()) return dto;
        return PermissionEntryDto.builder()
                .subject(dto.getSubject())
                .sort(sort)
                .permissions(dto.getPermissions())
                .build();
    }

    private String toEntityType(SubjectType t) {
        return (t == SubjectType.ID) ? "id" : "level";
    }

    /** type 누락 시 value로 유추 (key 의존 제거) */
    private String toEntityTypeOrInfer(PermissionEntryDto dto) {
        SubjectType t = (dto.getSubject() != null) ? dto.getSubject().getType() : null;
        if (t != null) return toEntityType(t);

        Object v = (dto.getSubject() != null) ? dto.getSubject().getValue() : null;
        if (v != null && String.valueOf(v).matches("\\d+")) return "id";
        return "level";
    }

    private String pairKey(Permission p) {
        return pairKey(p.getMenuId(), p.getType(), p.getValue());
    }
    private String pairKey(Long menuId, String type, String value) {
        return menuId + "|" + (type == null ? "" : type.toLowerCase(Locale.ROOT)) + "|" + String.valueOf(value);
    }

    /** 조상 엔트리를 "가까운 상위 우선"으로 dedupe해서 DTO로 변환 */
    private List<PermissionEntryDto> mergeNearestAncestorFirst(
            List<Permission> list,
            List<Long> orderedAncestors,
            Map<String, String> idNameMap
    ) {
        Map<Long, Integer> rank = new HashMap<>();
        for (int i = 0; i < orderedAncestors.size(); i++) rank.put(orderedAncestors.get(i), i);

        list.sort(Comparator
                .comparing((Permission e) -> rank.getOrDefault(e.getMenuId(), Integer.MAX_VALUE))
                .thenComparing(p -> Optional.ofNullable(p.getSort()).orElse(0)));

        Map<String, PermissionEntryDto> picked = new LinkedHashMap<>();
        for (Permission e : list) {
            String subjectPair = e.getType() + ":" + e.getValue(); // 동일 subject dedupe
            if (!picked.containsKey(subjectPair)) {
                PermissionEntryDto dto = normalizeEntry(toDto(e, idNameMap));
                picked.put(subjectPair, dto);
            }
        }
        return new ArrayList<>(picked.values());
    }

    /* === 캐시 무효화(Resolver와 동일 키 규칙) === */
    private void invalidateMenuPermission(Long menuId) {
        try {
            // 1) 자기 자신 키 삭제
            String selfKey = PERMISSION_KEY_PREFIX + menuId;
            redisTemplate.delete(selfKey);

            // 2) 자신의 path_id 조회
            String selfPath = menuRepository.findPathIdById(menuId);
            if (selfPath == null || selfPath.isBlank()) {
                loggingUtil.logFail(Action.UPDATE, "Cache invalidate: pathId not found for menuId=" + menuId);
                return;
            }
            String prefix = selfPath + ".";
            // 4) 후손 menuId들 조회
            List<Long> descendants = menuRepository.findDescendantIdsByPathPrefix(prefix);
            if (descendants.isEmpty()) {
                loggingUtil.logSuccess(Action.UPDATE, "Cache invalidate: no descendants for menuId=" + menuId);
                return;
            }
            // 5) 후손 키들 일괄 삭제
            List<String> keys = descendants.stream()
                    .map(id -> PERMISSION_KEY_PREFIX + id)
                    .collect(Collectors.toList());
            redisTemplate.delete(keys);

            loggingUtil.logSuccess(
                    Action.UPDATE,
                    "Cache invalidate success: menuId=" + menuId +
                            ", descendants=" + descendants.size() +
                            ", deletedKeys=" + (keys.size() + 1)
            );
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "Cache invalidate failed: " + e.getMessage());
        }
    }
}
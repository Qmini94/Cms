package kr.co.itid.cms.service.cms.core.menu.impl;

import kr.co.itid.cms.dto.cms.core.menu.request.MenuRequest;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuResponse;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuTreeLiteResponse;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuTreeResponse;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuTypeValueResponse;
import kr.co.itid.cms.entity.cms.core.menu.Menu;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.mapper.cms.core.menu.MenuMapper;
import kr.co.itid.cms.repository.cms.core.menu.MenuRepository;
import kr.co.itid.cms.service.cms.core.board.BoardMasterService;
import kr.co.itid.cms.service.cms.core.content.ContentService;
import kr.co.itid.cms.service.cms.core.menu.MenuService;
import kr.co.itid.cms.util.JsonFileWriterUtil;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service("menuService")
@RequiredArgsConstructor
public class MenuServiceImpl extends EgovAbstractServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;
    private final LoggingUtil loggingUtil;
    private final JsonFileWriterUtil jsonFileWriterUtil;
    private final BoardMasterService boardMasterService;
    private final ContentService contentService;

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public MenuResponse getMenuById(Long id) throws Exception {
        Menu menu = getMenuEntityById(id);
        return menuMapper.toResponse(menu);
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public MenuTypeValueResponse getMenuRenderById(Long id) throws Exception {
        Menu menu = getMenuEntityById(id);
        return menuMapper.toTypeValueResponse(menu);
    }

    private Menu getMenuEntityById(Long id) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get menu by id: " + id);
        try {
            Menu menu = menuRepository.findById(id)
                    .orElseThrow(() -> processException("Menu not found: " + id));
            loggingUtil.logSuccess(Action.RETRIEVE, "Got menu by id: " + id);
            return menu;
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "Database error while getting menu");
            throw processException("Cannot access database", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unexpected error while getting menu");
            throw processException("Unexpected error", e);
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public Optional<Menu> getMenuByTypeAndName(String type, String name) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get menu by type and name: type=" + type + ", name=" + name);
        try {
            Optional<Menu> result = menuRepository.findByTypeAndName(type, name);
            if (result.isPresent()) {
                loggingUtil.logSuccess(Action.RETRIEVE, "Got menu by type and name: " + name);
            } else {
                loggingUtil.logSuccess(Action.RETRIEVE, "Menu not found for type and name: " + name);
            }
            return result;
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "Database error while getting menu by type and name");
            throw processException("Cannot access database", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unexpected error while getting menu by type and name");
            throw processException("Unexpected error", e);
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public List<MenuResponse> getRootMenus() throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get all drives");
        try {
            List<Menu> menus = menuRepository.findByParentIdIsNull();
            loggingUtil.logSuccess(Action.RETRIEVE, "Got all drives");
            return menus.stream().map(menuMapper::toResponse).toList();
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "Database error while getting drives");
            throw processException("Cannot access database", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error while getting drives");
            throw processException("Unexpected error", e);
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public List<MenuTreeLiteResponse> getMenuTreeLiteByName(String name) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get menu tree (lite) for: " + name);
        try {
            Menu rootMenu = menuRepository.findByNameAndType(name, "drive")
                    .orElseThrow(() -> processException("Drive not found: " + name));

            // 한 번의 쿼리로 후손 노드 전체 로드
            List<Menu> allDescendants = menuRepository.findAllDescendantsByPathId(rootMenu.getPathId());

            // 메모리에서 트리 구성 (루트 제외, 루트의 자식부터 시작)
            List<MenuTreeLiteResponse> result = buildMenuTreeLiteInMemory(allDescendants, rootMenu.getId());

            loggingUtil.logSuccess(Action.RETRIEVE, "Got lite tree for: " + name);
            return result;
        } catch (NoSuchElementException e) {
            throw e;
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "Database error while getting tree: " + name);
            throw processException("Cannot access database", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error while getting tree: " + name);
            throw processException("Unexpected error", e);
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public List<MenuTreeResponse> getMenuTreeByName(String name) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get menu tree for: " + name);
        try {
            Menu rootMenu = menuRepository.findByNameAndType(name, "drive")
                    .orElseThrow(() -> processException("Drive not found: " + name));

            List<Menu> allDescendants = menuRepository.findAllDescendantsByPathId(rootMenu.getPathId());
            if (allDescendants == null || allDescendants.isEmpty()) {
                loggingUtil.logSuccess(Action.RETRIEVE, "Drive has no children: " + name);
                return Collections.emptyList();
            }

            // 루트 + 후손을 모두 전달하여 메모리에서 트리 구성
            List<Menu> allNodes = new ArrayList<>(1 + allDescendants.size());
            allNodes.add(rootMenu);
            allNodes.addAll(allDescendants);

            List<MenuTreeResponse> result = menuMapper.toTree(allNodes);
            loggingUtil.logSuccess(Action.RETRIEVE, "Got full tree for: " + name);
            return result;
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "Database error while getting tree: " + name);
            throw processException("Cannot access database", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error while getting tree: " + name);
            throw processException("Unexpected error", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void syncMenuTree(String driveName, List<MenuRequest> newTree) throws Exception {
        loggingUtil.logAttempt(Action.UPDATE, "Try to sync menu tree for drive: " + driveName);
        try {
            // 1) 루트 조회
            Menu rootMenu = menuRepository.findByNameAndType(driveName, "drive")
                    .orElseThrow(() -> processException("Drive not found: " + driveName));
            final Long rootId = rootMenu.getId();
            final String rootPathId = rootMenu.getPathId();

            // 2) 기존 서브트리 1회 로드 (루트 하위)
            List<Menu> existingMenus = menuRepository.findAllDescendantsByPathId(rootPathId);
            Map<Long, Menu> existingById = existingMenus.stream()
                    .collect(Collectors.toMap(Menu::getId, m -> m));

            // 3) 삭제 판별 및 참조 수집용
            Set<Long> processedIds = new HashSet<>();
            Set<String> usedBoardIds = new LinkedHashSet<>();
            Set<String> usedContentIds = new LinkedHashSet<>();

            // 4) BFS 큐(레벨 단위)
            record LevelItem(MenuRequest dto, Long parentId) {}
            List<LevelItem> current = new ArrayList<>();
            if (newTree != null) {
                for (MenuRequest child : newTree) {
                    current.add(new LevelItem(child, rootId));
                }
            }

            // 부모 pathId 캐시
            Map<Long, String> pathCache = new HashMap<>();
            pathCache.put(rootId, rootPathId);

            // 5) 레벨 순회
            while (!current.isEmpty()) {
                // 5-1) 이번 레벨 엔티티 생성/수정 수집
                List<Menu> toPersist = new ArrayList<>(current.size());
                for (LevelItem item : current) {
                    MenuRequest dto = item.dto();
                    Long parentId = item.parentId();

                    boolean isExisting = dto.getId() != null && dto.getId() > 0 && existingById.containsKey(dto.getId());
                    Menu entity;

                    if (isExisting) {
                        entity = existingById.get(dto.getId());
                        // 중복검사 (대량이면 UNIQUE 제약 권장)
                        if (menuRepository.existsByTypeAndNameAndIdNot(dto.getType(), dto.getName(), dto.getId())) {
                            throw new RuntimeException("이미 존재하는 type + name 조합입니다: " + dto.getType() + " / " + dto.getName());
                        }
                        menuMapper.updateEntity(dto, entity);
                        entity.setParentId(parentId);
                        entity.setPosition(dto.getPosition());
                    } else {
                        if (menuRepository.existsByTypeAndName(dto.getType(), dto.getName())) {
                            throw new RuntimeException("이미 존재하는 type + name 조합입니다: " + dto.getType() + " / " + dto.getName());
                        }
                        dto.setId(null);
                        entity = menuMapper.toEntity(dto);
                        entity.setParentId(parentId);
                        entity.setPosition(dto.getPosition());
                    }

                    // 참조 수집
                    collectReferences(dto, usedBoardIds, usedContentIds);

                    toPersist.add(entity);
                }

                // 5-2) 배치 저장 (ID 확정)
                menuRepository.saveAll(toPersist);

                // 5-3) 부모별로 묶어 자식 pathId 일괄 갱신
                Map<Long, List<Menu>> byParent = toPersist.stream()
                        .collect(Collectors.groupingBy(Menu::getParentId, LinkedHashMap::new, Collectors.toList()));

                for (Map.Entry<Long, List<Menu>> e : byParent.entrySet()) {
                    Long parentId = e.getKey();
                    String parentPathId = pathCache.get(parentId);
                    if (parentPathId == null) {
                        // 이 경우는 로직 오류에 가까움: 상위 레벨에서 이미 채워져 있어야 함
                        throw new IllegalStateException("Parent pathId not found for parentId=" + parentId);
                    }
                    // 부모의 모든 자식 pathId를 한 번에: CONCAT(parentPathId, '.', child.id)
                    menuRepository.updateChildrenPathIds(parentId, parentPathId);

                    // 캐시/처리 집합 갱신
                    for (Menu child : e.getValue()) {
                        String childPath = parentPathId + "." + child.getId();
                        pathCache.put(child.getId(), childPath);
                        processedIds.add(child.getId());
                    }
                }

                // 5-4) 다음 레벨 큐 구성
                List<LevelItem> next = new ArrayList<>();
                int i = 0;
                for (LevelItem item : current) {
                    Menu saved = toPersist.get(i++);
                    List<MenuRequest> children = item.dto().getChildren();
                    if (children != null && !children.isEmpty()) {
                        for (MenuRequest ch : children) {
                            next.add(new LevelItem(ch, saved.getId()));
                        }
                    }
                }
                current = next;
            }

            // 6) 삭제 (요청 트리에 없는 기존 노드)
            List<Long> toDeleteIds = existingMenus.stream()
                    .map(Menu::getId)
                    .filter(id -> !processedIds.contains(id))
                    .toList();
            if (!toDeleteIds.isEmpty()) {
                menuRepository.deleteAllByIdInBatch(toDeleteIds);
            }

            // 7) 후처리 (참조 플래그)
            boardMasterService.syncUsageFlagsByBoardIds(usedBoardIds);
            contentService.syncUsageFlagsByContentIds(usedContentIds);

            loggingUtil.logSuccess(Action.UPDATE, "Synced menu tree for drive: " + driveName);
        } catch (DataIntegrityViolationException e) {
            loggingUtil.logFail(Action.UPDATE, "Constraint violation during sync for drive: " + driveName);
            throw processException("Duplicate menu name or type detected.", e);
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.UPDATE, "DB error during sync for drive: " + driveName);
            throw processException("Database error", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "Unexpected error during sync for drive: " + driveName);
            throw processException("Unexpected sync error", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void saveDriveMenu(Long id, MenuRequest request) throws Exception {
        boolean isNew = (id == null);
        Action action = isNew ? Action.CREATE : Action.UPDATE;

        loggingUtil.logAttempt(action, "Try to " + (isNew ? "create" : "update") + " menu: " + request.getName());
        try {
            Menu menu;
            if (isNew) {
                if (menuRepository.findByTypeAndName(request.getType(), request.getName()).isPresent()) {
                    throw processException("이미 동일한 이름의 메뉴가 존재합니다: " + request.getName());
                }
                menu = menuMapper.toEntity(request);
                menuRepository.save(menu);
                loggingUtil.logSuccess(action, "Menu created: " + request.getName());
            } else {
                menu = menuRepository.findById(id)
                        .orElseThrow(() -> processException("해당 ID의 메뉴가 존재하지 않습니다: " + id));

                menu.setName(request.getName());
                menu.setTitle(request.getTitle());
                menu.setType(request.getType());
                menu.setValue(request.getValue());
                menu.setPathUrl(request.getPathUrl());
                menu.setPathId(request.getPathId());
                menu.setPathString(request.getPathString());
                menu.setPosition(request.getPosition());
                menu.setLevel(request.getLevel().longValue());
                menu.setIsShow(request.getIsShow());

                menuRepository.save(menu);
                loggingUtil.logSuccess(action, "Menu updated: " + request.getName());
            }

            // 루트/단독 저장의 경우 pathId = id 로 초기화
            menuRepository.updatePathIdById(menu.getId(), String.valueOf(menu.getId()));

            if ("drive".equals(request.getType())) {
                List<Menu> latestMenus = menuRepository.findAllDescendantsByPathId(String.valueOf(menu.getId()));
                List<MenuTreeResponse> tree = menuMapper.toTree(latestMenus);
                jsonFileWriterUtil.writeJsonFile("menu", "menu_" + request.getName(), tree, true);
                loggingUtil.logSuccess(action, "Menu json file created: " + request.getName());
            }
        } catch (Exception e) {
            loggingUtil.logFail(action, "Failed to " + (isNew ? "create" : "update") + " menu: " + e.getMessage());
            throw processException("메뉴 " + (isNew ? "등록" : "수정") + " 실패", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void deleteDriveAndAllChildren(String driveName) throws Exception {
        loggingUtil.logAttempt(Action.DELETE, "Try to delete drive and all children: " + driveName);
        try {
            Menu rootDrive = menuRepository.findByTypeAndName("drive", driveName)
                    .orElseThrow(() -> processException("해당 드라이브 메뉴가 존재하지 않습니다: " + driveName));

            String rootPathId = rootDrive.getPathId();
            List<Menu> allDescendants = menuRepository.findAllDescendantsByPathIdWithDot(rootPathId + ".");

            if (!allDescendants.isEmpty()) {
                menuRepository.deleteAllInBatch(allDescendants);
            }
            menuRepository.delete(rootDrive);

            loggingUtil.logSuccess(Action.DELETE, "Deleted drive and all children: " + driveName);
        } catch (Exception e) {
            loggingUtil.logFail(Action.DELETE, "Failed to delete drive and children: " + e.getMessage());
            throw processException("드라이브 및 하위 메뉴 삭제 실패", e);
        }
    }

    // ===== 내부 유틸 =====

    /** 메모리에서 MenuTreeLiteResponse 트리 구성 (루트 제외, 루트의 자식부터 시작) */
    private List<MenuTreeLiteResponse> buildMenuTreeLiteInMemory(List<Menu> allNodes, Long rootId) {
        Map<Long, List<Menu>> childrenMap = allNodes.stream()
                .collect(Collectors.groupingBy(Menu::getParentId));
        return buildMenuTreeLiteRecursive(rootId, childrenMap);
    }

    private List<MenuTreeLiteResponse> buildMenuTreeLiteRecursive(Long parentId, Map<Long, List<Menu>> childrenMap) {
        List<Menu> children = childrenMap.getOrDefault(parentId, Collections.emptyList());
        return children.stream()
                .sorted(Comparator.comparingInt(Menu::getPosition))
                .map(menu -> menuMapper.toLiteTreeResponse(menu,
                        buildMenuTreeLiteRecursive(menu.getId(), childrenMap)))
                .toList();
    }

    private void collectReferences(MenuRequest dto, Set<String> usedBoardIds, Set<String> usedContentIds) {
        if (dto.getType() != null && dto.getValue() != null && !dto.getValue().isBlank()) {
            String type = dto.getType().trim().toLowerCase();
            String value = dto.getValue().trim();
            if ("board".equals(type)) usedBoardIds.add(value);
            else if ("content".equals(type)) usedContentIds.add(value);
        }
    }

    // ===== Permission 서비스 연동 메서드 =====

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public String getPathIdById(Long menuId) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Get pathId by menuId: " + menuId);
        try {
            String pathId = menuRepository.findById(menuId)
                    .map(Menu::getPathId)
                    .orElseThrow(() -> processException("Menu not found: " + menuId));
            loggingUtil.logSuccess(Action.RETRIEVE, "Got pathId: " + pathId);
            return pathId;
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "Database error while getting pathId");
            throw processException("Cannot access database", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unexpected error while getting pathId");
            throw processException("Unexpected error", e);
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public List<Long> getDescendantIdsByPathPrefix(String pathPrefix) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Get descendant IDs by pathPrefix: " + pathPrefix);
        try {
            List<Long> descendants = menuRepository.findDescendantIdsByPathPrefix(pathPrefix);
            loggingUtil.logSuccess(Action.RETRIEVE, "Got " + descendants.size() + " descendants");
            return descendants;
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "Database error while getting descendants");
            throw processException("Cannot access database", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unexpected error while getting descendants");
            throw processException("Unexpected error", e);
        }
    }
}
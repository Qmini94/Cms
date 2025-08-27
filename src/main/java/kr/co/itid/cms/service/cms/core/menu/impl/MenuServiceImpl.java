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

            return menus.stream()
                    .map(menuMapper::toResponse)
                    .toList();
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
            Menu rootMenu = menuRepository.findByNameOrderByPositionAsc(name)
                    .orElseThrow(() -> processException("Drive not found: " + name));

            // 최적화: 한 번의 쿼리로 모든 하위 노드 조회 (루트 제외)
            List<Menu> allDescendants = menuRepository.findAllDescendantsByPathId(rootMenu.getPathId());

            // 루트 자체는 제외하고 직접 자식들부터 시작하는 트리 구성
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
            Menu rootMenu = menuRepository.findByNameOrderByPositionAsc(name)
                    .orElseThrow(() -> processException("Drive not found: " + name));

            // 최적화: 한 번의 쿼리로 모든 하위 노드 조회
            List<Menu> allDescendants = menuRepository.findAllDescendantsByPathId(rootMenu.getPathId());

            // 전체 노드 리스트에 루트도 포함 (mapper.toTree가 루트를 찾아서 처리)
            List<Menu> allNodes = new ArrayList<>();
            allNodes.add(rootMenu);
            allNodes.addAll(allDescendants);

            // 기존 매퍼 로직 활용 (메모리에서 트리 구성)
            List<MenuTreeResponse> result = menuMapper.toTree(allNodes);

            loggingUtil.logSuccess(Action.RETRIEVE, "Got full tree for: " + name);
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
    @Transactional(rollbackFor = EgovBizException.class)
    public void syncMenuTree(String driveName, List<MenuRequest> newTree) throws Exception {
        loggingUtil.logAttempt(Action.UPDATE, "Try to sync menu tree for drive: " + driveName);

        try {
            Menu rootMenu = menuRepository.findByNameOrderByPositionAsc(driveName)
                    .orElseThrow(() -> processException("Drive not found: " + driveName));
            String rootPathId = rootMenu.getPathId();

            // 최적화: 기존 메뉴 한 번에 조회
            List<Menu> existingMenus = menuRepository.findAllDescendantsByPathId(rootPathId);
            Map<Long, Menu> existingMenuMap = existingMenus.stream()
                    .collect(Collectors.toMap(Menu::getId, m -> m));

            // 변경사항을 메모리에서 계산
            List<Menu> toSave = new ArrayList<>();
            List<Menu> toUpdate = new ArrayList<>();
            Set<Long> processedIds = new HashSet<>();
            Set<String> usedBoardIds = new LinkedHashSet<>();
            Set<String> usedContentIds = new LinkedHashSet<>();

            // 재귀적으로 변경사항 처리
            for (MenuRequest child : newTree) {
                processMenuChanges(child, rootMenu.getId(), rootPathId,
                        existingMenuMap, toSave, toUpdate, processedIds,
                        usedBoardIds, usedContentIds);
            }

            // 배치 처리로 성능 최적화
            if (!toSave.isEmpty()) {
                menuRepository.saveAll(toSave);
                // pathId 일괄 업데이트
                updatePathIdsForNewMenus(toSave, rootPathId);
            }
            if (!toUpdate.isEmpty()) {
                menuRepository.saveAll(toUpdate);
            }

            // 사용되지 않는 메뉴 삭제
            List<Long> deleteIds = existingMenus.stream()
                    .map(Menu::getId)
                    .filter(id -> !processedIds.contains(id))
                    .toList();
            if (!deleteIds.isEmpty()) {
                menuRepository.deleteAllByIdInBatch(deleteIds);
            }

            // 후처리
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

            menuRepository.updatePathIdById(menu.getId(), String.valueOf(menu.getId()));

            if ("drive".equals(request.getType())) {
                // 최적화된 조회로 JSON 파일 생성
                List<Menu> latestMenus = menuRepository.findAllDescendantsByPathId(String.valueOf(menu.getId()));
                List<MenuTreeResponse> tree = menuMapper.toTree(latestMenus);
                jsonFileWriterUtil.writeJsonFile("menu", "menu_" + request.getName(), tree, true);
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

            // 최적화: 한 번에 모든 하위 노드 조회
            List<Menu> allDescendants = menuRepository.findAllDescendantsByPathIdWithDot(rootPathId + ".");

            // 배치 삭제
            if (!allDescendants.isEmpty()) {
                menuRepository.deleteAllInBatch(allDescendants);
            }
            menuRepository.delete(rootDrive);

            jsonFileWriterUtil.writeJsonFile("menu", "menu_" + driveName, Collections.emptyList(), true);

            loggingUtil.logSuccess(Action.DELETE, "Deleted drive and all children: " + driveName);
        } catch (Exception e) {
            loggingUtil.logFail(Action.DELETE, "Failed to delete drive and children: " + e.getMessage());
            throw processException("드라이브 및 하위 메뉴 삭제 실패", e);
        }
    }

    // === 최적화된 내부 메서드들 ===

    /**
     * 메모리에서 MenuTreeLiteResponse 트리 구성 (DB 호출 없음)
     * 루트 자체는 포함하지 않고, 루트의 직접 자식들을 최상위로 하는 트리를 구성
     */
    private List<MenuTreeLiteResponse> buildMenuTreeLiteInMemory(List<Menu> allNodes, Long rootId) {
        // parentId로 그룹핑
        Map<Long, List<Menu>> childrenMap = allNodes.stream()
                .collect(Collectors.groupingBy(Menu::getParentId));

        // rootId의 직접 자식들부터 시작 (루트 자체는 제외)
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

    /**
     * 메뉴 변경사항을 메모리에서 처리 (배치 저장을 위한 준비)
     */
    private void processMenuChanges(
            MenuRequest dto,
            Long parentId,
            String parentPathId,
            Map<Long, Menu> existingMenuMap,
            List<Menu> toSave,
            List<Menu> toUpdate,
            Set<Long> processedIds,
            Set<String> usedBoardIds,
            Set<String> usedContentIds) {

        Menu entity;
        boolean isExisting = dto.getId() != null && dto.getId() > 0 && existingMenuMap.containsKey(dto.getId());

        if (isExisting) {
            entity = existingMenuMap.get(dto.getId());

            if (menuRepository.existsByTypeAndNameAndIdNot(dto.getType(), dto.getName(), dto.getId())) {
                throw new RuntimeException("이미 존재하는 type + name 조합입니다: " + dto.getType() + " / " + dto.getName());
            }

            // 기존 엔티티 업데이트
            menuMapper.updateEntity(dto, entity);
            entity.setParentId(parentId);
            entity.setPosition(dto.getPosition());
            toUpdate.add(entity);
        } else {
            if (menuRepository.existsByTypeAndName(dto.getType(), dto.getName())) {
                throw new RuntimeException("이미 존재하는 type + name 조합입니다: " + dto.getType() + " / " + dto.getName());
            }

            // 새 엔티티 생성
            dto.setId(null);
            entity = menuMapper.toEntity(dto);
            entity.setParentId(parentId);
            entity.setPosition(dto.getPosition());
            toSave.add(entity);
        }

        if (entity.getId() != null) {
            processedIds.add(entity.getId());
        }

        // 참조 수집
        collectReferences(dto, usedBoardIds, usedContentIds);

        // 자식 재귀 처리
        if (dto.getChildren() != null && !dto.getChildren().isEmpty()) {
            String newPathId = parentPathId + "." + (entity.getId() != null ? entity.getId() : "NEW");

            for (MenuRequest child : dto.getChildren()) {
                processMenuChanges(child, entity.getId(), newPathId,
                        existingMenuMap, toSave, toUpdate, processedIds,
                        usedBoardIds, usedContentIds);
            }
        }
    }

    /**
     * 새로 저장된 메뉴들의 pathId를 일괄 업데이트
     */
    private void updatePathIdsForNewMenus(List<Menu> newMenus, String rootPathId) {
        // 실제 구현에서는 저장 후 ID가 생성된 상태이므로 배치 업데이트 가능
        List<Long> newIds = newMenus.stream()
                .map(Menu::getId)
                .filter(Objects::nonNull)
                .toList();

        if (!newIds.isEmpty()) {
            // 루트 메뉴의 ID를 찾아서 배치 업데이트
            Long rootId = newMenus.stream()
                    .filter(m -> m.getParentId() == null)
                    .map(Menu::getId)
                    .findFirst()
                    .orElse(null);

            if (rootId != null) {
                menuRepository.batchUpdatePathIds(rootId, rootPathId, newIds);
            }
        }
    }

    private void collectReferences(MenuRequest dto, Set<String> usedBoardIds, Set<String> usedContentIds) {
        if (dto.getType() != null && dto.getValue() != null && !dto.getValue().isBlank()) {
            String type = dto.getType().trim().toLowerCase();
            String value = dto.getValue().trim();

            if ("board".equals(type)) {
                usedBoardIds.add(value);
            } else if ("content".equals(type)) {
                usedContentIds.add(value);
            }
        }
    }

    // === Permission 서비스에서 사용하는 메서드들 ===

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
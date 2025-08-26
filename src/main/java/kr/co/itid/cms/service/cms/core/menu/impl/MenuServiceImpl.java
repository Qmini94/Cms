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

            loggingUtil.logSuccess(Action.RETRIEVE, "Got lite tree for: " + name);
            return buildMenuTreeLite(rootMenu.getId());
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
            // 1. 드라이브 루트 조회
            Menu rootMenu = menuRepository.findByNameOrderByPositionAsc(name)
                    .orElseThrow(() -> processException("Drive not found: " + name));

            // 2. path_id 기준 하위 전체 조회
            List<Menu> flatList = menuRepository.findAllDescendantsByPathId(rootMenu.getPathId());

            // 3. 트리 재구성 (자식 정렬 포함)
            List<MenuTreeResponse> children = menuMapper.toTree(flatList);

            // 4. 루트 포함한 트리 생성
            MenuTreeResponse rootDto = menuMapper.toFullResponse(rootMenu, children);

            loggingUtil.logSuccess(Action.RETRIEVE, "Got full tree using path_id for: " + name);

            return List.of(rootDto);
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
            // 1. 루트 메뉴 조회
            Menu rootMenu = menuRepository.findByNameOrderByPositionAsc(driveName)
                    .orElseThrow(() -> processException("Drive not found: " + driveName));
            String rootPathId = rootMenu.getPathId();

            // 2. 기존 메뉴 조회 및 매핑
            List<Menu> existingMenus = menuRepository.findAllDescendantsByPathId(rootPathId);
            Map<Long, Menu> existingMenuMap = existingMenus.stream()
                    .collect(Collectors.toMap(Menu::getId, m -> m));

            // 3. 트리 재귀 동기화 및 갱신 ID 추적
            Set<Long> updatedIds = new HashSet<>();
            Set<String> usedBoardIds = new LinkedHashSet<>();
            Set<String> usedContentIds = new LinkedHashSet<>();
            for (MenuRequest child : newTree) {
                syncRecursive(child, rootMenu.getId(), rootPathId,
                        existingMenuMap, updatedIds,
                        usedBoardIds, usedContentIds);
            }

            // 4. 사용되지 않는 메뉴 삭제
            List<Long> deleteIds = existingMenus.stream()
                    .map(Menu::getId)
                    .filter(id -> !updatedIds.contains(id))
                    .toList();
            menuRepository.deleteAllByIdInBatch(deleteIds);

            // 후처리: 참조된 것만 is_use=1, 나머지 0
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
                // 중복 체크
                if (menuRepository.findByTypeAndName(request.getType(), request.getName()).isPresent()) {
                    throw processException("이미 동일한 이름의 메뉴가 존재합니다: " + request.getName());
                }

                menu = menuMapper.toEntity(request);
                menuRepository.save(menu);

                loggingUtil.logSuccess(action, "Menu created: " + request.getName());

            } else {
                menu = menuRepository.findById(id)
                        .orElseThrow(() -> processException("해당 ID의 메뉴가 존재하지 않습니다: " + id));

                // 수정 가능한 필드 반영
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
            // 1. 드라이브 조회
            Menu rootDrive = menuRepository.findByTypeAndName("drive", driveName)
                    .orElseThrow(() -> processException("해당 드라이브 메뉴가 존재하지 않습니다: " + driveName));

            String rootPathId = rootDrive.getPathId(); // 예: "2"

            // 2. 하위 노드 조회 (dot 붙여서 정확히 하위만)
            List<Menu> allDescendants = menuRepository.findAllDescendantsByPathIdWithDot(rootPathId + ".");

            // 3. 하위 노드 먼저 삭제
            if (!allDescendants.isEmpty()) {
                menuRepository.deleteAllInBatch(allDescendants);
            }

            // 4. 루트 드라이브 삭제
            menuRepository.delete(rootDrive);

            jsonFileWriterUtil.writeJsonFile("menu", "menu_" + driveName, Collections.emptyList(), true);

            loggingUtil.logSuccess(Action.DELETE, "Deleted drive and all children: " + driveName);
        } catch (Exception e) {
            loggingUtil.logFail(Action.DELETE, "Failed to delete drive and children: " + e.getMessage());
            throw processException("드라이브 및 하위 메뉴 삭제 실패", e);
        }
    }

    private void syncRecursive(
            MenuRequest dto,
            Long parentId,
            String parentPathId,
            Map<Long, Menu> existingMenuMap,
            Set<Long> updatedIds,
            Set<String> usedBoardIds,
            Set<String> usedContentIds
    ) {
        Menu entity;
        boolean isRealEntity = dto.getId() != null && dto.getId() > 0 && existingMenuMap.containsKey(dto.getId());

        if (isRealEntity) {
            entity = existingMenuMap.get(dto.getId());

            if (menuRepository.existsByTypeAndNameAndIdNot(dto.getType(), dto.getName(), dto.getId())) {
                throw new RuntimeException("이미 존재하는 type + name 조합입니다: " + dto.getType() + " / " + dto.getName());
            }

            menuMapper.updateEntity(dto, entity); // id는 건드리지 않는 매퍼여야 함
            entity.setParentId(parentId);
            entity.setPosition(dto.getPosition());

            menuRepository.save(entity);
        } else {
            if (menuRepository.existsByTypeAndName(dto.getType(), dto.getName())) {
                throw new RuntimeException("이미 존재하는 type + name 조합입니다: " + dto.getType() + " / " + dto.getName());
            }

            dto.setId(null); // 신규 생성 보장
            entity = menuMapper.toEntity(dto);
            entity.setParentId(parentId);
            entity.setPosition(dto.getPosition());

            menuRepository.save(entity); // PK 생성
        }

        updatedIds.add(entity.getId());

        // pathId 갱신 (엔티티에도 세팅해두면 자식 재귀에서 값 사용시 안전)
        String newPathId = parentPathId + "." + entity.getId();
        menuRepository.updatePathIdById(entity.getId(), newPathId);
        entity.setPathId(newPathId);

        // 참조 수집
        if (dto.getType() != null && dto.getValue() != null && !dto.getValue().isBlank()) {
            String t = dto.getType().trim().toLowerCase();
            String v = dto.getValue().trim();
            if ("board".equals(t)) usedBoardIds.add(v);
            else if ("content".equals(t)) usedContentIds.add(v);
        }

        // 자식 재귀
        List<MenuRequest> children = dto.getChildren();
        if (children != null && !children.isEmpty()) {
            for (MenuRequest child : children) {
                syncRecursive(
                        child,
                        entity.getId(),
                        newPathId,
                        existingMenuMap,
                        updatedIds,
                        usedBoardIds,
                        usedContentIds
                );
            }
        }
    }

    private List<MenuTreeLiteResponse> buildMenuTreeLite(Long parentId) {
        return getChildren(parentId).stream()
                .map(menu -> menuMapper.toLiteTreeResponse(menu, buildMenuTreeLite(menu.getId())))
                .toList();
    }

    private List<Menu> getChildren(Long parentId) {
        return menuRepository.findByParentIdOrderByPositionAsc(parentId);
    }
}
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
            // 1~5. 트리 동기화 처리
            Menu rootMenu = menuRepository.findByNameOrderByPositionAsc(driveName)
                    .orElseThrow(() -> processException("Drive not found: " + driveName));
            String rootPathId = rootMenu.getPathId();

            List<Menu> existingMenus = menuRepository.findAllDescendantsByPathId(rootPathId);
            Map<Long, Menu> existingMenuMap = existingMenus.stream()
                    .collect(Collectors.toMap(Menu::getId, m -> m));

            Set<Long> updatedIds = new HashSet<>();
            int position = 0;
            for (MenuRequest child : newTree) {
                syncRecursive(child, rootMenu.getId(), rootPathId, position++, existingMenuMap, updatedIds);
            }

            List<Long> deleteIds = existingMenus.stream()
                    .map(Menu::getId)
                    .filter(id -> !updatedIds.contains(id))
                    .toList();
            menuRepository.deleteAllByIdInBatch(deleteIds);

            // 6. JSON 파일 생성
            List<Menu> latestMenus = menuRepository.findAllDescendantsByPathId(rootPathId);
            List<MenuTreeResponse> tree = menuMapper.toTree(latestMenus);

            jsonFileWriterUtil.writeJsonFile("menu", "menu_" + driveName, tree, true);

            loggingUtil.logSuccess(Action.UPDATE, "Synced menu tree for drive: " + driveName);

        } catch (DataIntegrityViolationException e) {
            loggingUtil.logFail(Action.UPDATE, "Constraint violation during sync for drive: " + driveName);
            throw processException("Duplicate menu name or type detected.", e);

        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.UPDATE, "DB error during sync for drive: " + driveName);
            throw processException("Database error", e);

        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("JSON")) {
                loggingUtil.logFail(Action.UPDATE, "JSON 파일 저장 실패 during sync for drive: " + driveName);
                throw processException("메뉴 JSON 파일 저장 실패" + e.getMessage(), e);
            } else {
                loggingUtil.logFail(Action.UPDATE, "Runtime error during sync for drive: " + driveName);
                throw processException("Unexpected runtime error", e);
            }

        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "Unknown error during sync for drive: " + driveName);
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
                menu.setIsUseSearch(request.getIsUseSearch());
                menu.setIsUseCount(request.getIsUseCount());

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
            int position,
            Map<Long, Menu> existingMenuMap,
            Set<Long> updatedIds
    ) {
        Menu entity;

        boolean isRealEntity = dto.getId() != null && dto.getId() > 0 && existingMenuMap.containsKey(dto.getId());
        if (isRealEntity) {
            // 수정
            entity = existingMenuMap.get(dto.getId());

            // 자기 자신을 제외하고 같은 type + name 조합이 있는지 확인
            if (menuRepository.existsByTypeAndNameAndIdNot(dto.getType(), dto.getName(), dto.getId())) {
                throw new RuntimeException("이미 존재하는 type + name 조합입니다: " + dto.getType() + " / " + dto.getName());
            }

            menuMapper.updateEntity(dto, entity);
            entity.setParentId(parentId);
            entity.setPosition(position);
        } else {
            // 신규
            // 같은 type + name 조합이 이미 존재하는지 확인
            if (menuRepository.existsByTypeAndName(dto.getType(), dto.getName())) {
                throw new RuntimeException("이미 존재하는 type + name 조합입니다: " + dto.getType() + " / " + dto.getName());
            }
            dto.setId(null);
            entity = menuMapper.toEntity(dto);
            entity.setParentId(parentId);
            entity.setPosition(position);
            menuRepository.save(entity); // 여기서 ID 생성됨
        }

        updatedIds.add(entity.getId());

        // pathId 직접 갱신 (flush나 트랜잭션 커밋 시 자동 반영 안될 수 있음)
        String newPathId = parentPathId + "." + entity.getId();
        menuRepository.updatePathIdById(entity.getId(), newPathId);

        // 자식들 재귀 처리
        List<MenuRequest> children = dto.getChildren();
        if (children != null && !children.isEmpty()) {
            int childPos = 0;
            for (MenuRequest child : children) {
                syncRecursive(child, entity.getId(), newPathId, childPos++, existingMenuMap, updatedIds);
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
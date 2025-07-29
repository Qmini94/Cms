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
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service("menuService")
@RequiredArgsConstructor
public class MenuServiceImpl extends EgovAbstractServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final LoggingUtil loggingUtil;
    private final MenuMapper menuMapper;

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
            // 1. 루트 메뉴 조회
            Menu rootMenu = menuRepository.findByNameOrderByPositionAsc(name)
                    .orElseThrow(() -> processException("Drive not found: " + name));

            // 2. path_id 기반으로 하위 메뉴 한 번에 조회
            List<Menu> flatList = menuRepository.findAllDescendantsByPathId(rootMenu.getPathId());

            // 3. 트리 구조 재조립
            List<MenuTreeLiteResponse> children = buildLiteTree(flatList);

            loggingUtil.logSuccess(Action.RETRIEVE, "Got lite tree for: " + name);
            return children;
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
            // 1. 드라이브 루트 조회
            Menu rootMenu = menuRepository.findByNameOrderByPositionAsc(driveName)
                    .orElseThrow(() -> processException("Drive not found: " + driveName));

            String rootPathId = rootMenu.getPathId(); // 예: 11774

            // 2. path_id 기반으로 하위 트리 조회
            List<Menu> existingMenus = menuRepository.findAllDescendantsByPathId(rootPathId);

            // 3. ID → Menu 맵 구성
            Map<Long, Menu> existingMenuMap = existingMenus.stream()
                    .collect(Collectors.toMap(Menu::getId, m -> m));

            // 4. 재귀적으로 트리 동기화 (insert/update)
            Set<Long> updatedIds = new HashSet<>();
            int position = 0;
            for (MenuRequest child : newTree) {
                syncRecursive(child, rootMenu.getId(), rootPathId, position++, existingMenuMap, updatedIds);
            }

            // 5. 삭제 처리: 전달되지 않은 기존 메뉴 제거
            for (Menu menu : existingMenus) {
                if (!updatedIds.contains(menu.getId())) {
                    menuRepository.delete(menu);
                }
            }

            loggingUtil.logSuccess(Action.UPDATE, "Synced menu tree for drive: " + driveName);

        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.UPDATE, "DB error during sync for drive: " + driveName);
            throw processException("Database error", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "Unknown error during sync for drive: " + driveName);
            throw processException("Unexpected sync error", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void saveMenu(Long id, MenuRequest request) throws Exception {
        boolean isNew = (id == null);
        Action action = isNew ? Action.CREATE : Action.UPDATE;

        loggingUtil.logAttempt(action, "Try to " + (isNew ? "create" : "update") + " menu: " + request.getName());

        try {
            if (isNew) {
                // 중복 체크
                if (menuRepository.findByTypeAndName(request.getType(), request.getName()).isPresent()) {
                    throw processException("이미 동일한 이름의 메뉴가 존재합니다: " + request.getName());
                }

                Menu menu = menuMapper.toEntity(request);
                menuRepository.save(menu);

                loggingUtil.logSuccess(action, "Menu created: " + request.getName());

            } else {
                Menu menu = menuRepository.findById(id)
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

        } catch (Exception e) {
            loggingUtil.logFail(action, "Failed to " + (isNew ? "create" : "update") + " menu: " + e.getMessage());
            throw processException("메뉴 " + (isNew ? "등록" : "수정") + " 실패", e);
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

        if (dto.getId() != null && existingMenuMap.containsKey(dto.getId())) {
            // 수정
            entity = existingMenuMap.get(dto.getId());
            entity.setTitle(dto.getTitle());
            entity.setType(dto.getType());
            entity.setValue(dto.getValue());
            entity.setIsShow(dto.getIsShow());
            entity.setParentId(parentId);
            entity.setPosition(position);
        } else {
            // 신규
            entity = new Menu();
            entity.setTitle(dto.getTitle());
            entity.setType(dto.getType());
            entity.setValue(dto.getValue());
            entity.setIsShow(dto.getIsShow());
            entity.setParentId(parentId);
            entity.setPosition(position);
        }

        menuRepository.save(entity); // 저장 후 ID 부여됨
        updatedIds.add(entity.getId());

        // path_id 갱신: 부모 path_id + "." + 현재 ID
        entity.setPathId(parentPathId + "." + entity.getId());
        menuRepository.save(entity); // path_id 갱신 후 다시 저장

        // 자식 처리
        List<MenuRequest> children = dto.getChildren();
        if (children != null && !children.isEmpty()) {
            int childPos = 0;
            for (MenuRequest child : children) {
                syncRecursive(child, entity.getId(), entity.getPathId(), childPos++, existingMenuMap, updatedIds);
            }
        }
    }

    private List<MenuTreeLiteResponse> buildLiteTree(List<Menu> flatList) {
        Map<Long, MenuTreeLiteResponse> map = new LinkedHashMap<>();
        List<MenuTreeLiteResponse> roots = new ArrayList<>();

        for (Menu menu : flatList) {
            map.put(menu.getId(), menuMapper.toLiteTreeResponse(menu, new ArrayList<>()));
        }

        for (Menu menu : flatList) {
            Long parentId = menu.getParentId();
            MenuTreeLiteResponse current = map.get(menu.getId());

            if (parentId != null && map.containsKey(parentId)) {
                map.get(parentId).getChildren().add(current);
            } else {
                roots.add(current);
            }
        }

        return roots;
    }
}
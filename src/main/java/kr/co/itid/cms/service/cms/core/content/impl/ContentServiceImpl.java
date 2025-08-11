package kr.co.itid.cms.service.cms.core.content.impl;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.dto.cms.core.content.request.ContentRequest;
import kr.co.itid.cms.dto.cms.core.content.response.ContentResponse;
import kr.co.itid.cms.entity.cms.core.content.Content;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.mapper.cms.core.content.ContentMapper;
import kr.co.itid.cms.repository.cms.core.content.ContentRepository;
import kr.co.itid.cms.service.cms.core.content.ContentService;
import kr.co.itid.cms.util.LoggingUtil;
import kr.co.itid.cms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service("contentService")
@RequiredArgsConstructor
public class ContentServiceImpl extends EgovAbstractServiceImpl implements ContentService {

    private final ContentRepository contentRepository;
    private final ContentMapper contentMapper;
    private final LoggingUtil loggingUtil;

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public Page<ContentResponse> searchContents(SearchOption option, Pageable pageable) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to search content list");

        try {
            // 1. 검색 및 페이징 처리
            Page<Content> resultPage = contentRepository.searchByCondition(option, pageable);

            loggingUtil.logSuccess(Action.RETRIEVE, "Content list retrieved successfully (total=" + resultPage.getTotalElements() + ")");

            // 2. 매핑
            return resultPage.map(contentMapper::toResponse);

        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "DB error: " + e.getMessage());
            throw processException("DB 오류가 발생했습니다.", e);

        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error: " + e.getMessage());
            throw processException("콘텐츠 목록 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public List<ContentResponse> getContentsByParentId(Long parentId) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get group contents by parentId=" + parentId);

        try {
            List<Content> list = contentRepository.findByParentIdOrderBySortAsc(parentId);
            loggingUtil.logSuccess(Action.RETRIEVE, "Group contents loaded: parentId=" + parentId);
            return contentMapper.toResponseList(list);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, e.getMessage());
            throw processException("Failed to load contents", e);
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public ContentResponse getContentByParentId(Long parentId) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get content by parentId=" + parentId);

        try {
            Content content = contentRepository.findFirstByParentIdAndIsUseTrue(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Content not found for parentId=" + parentId));

            loggingUtil.logSuccess(Action.RETRIEVE, "Content loaded: idx=" + content.getIdx());
            return contentMapper.toResponse(content);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, e.getMessage());
            throw processException("Failed to get content", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void createRootContent(ContentRequest request) throws Exception {
        loggingUtil.logAttempt(Action.CREATE, "Try to create root content");

        try {
            JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();

            // Step 1. toEntity + 강제 설정
            Content root = contentMapper.toEntity(request);
            root.setHostname(user.hostname());
            root.setCreatedBy(user.userId());
            root.setSort(0);
            root.setIsUse(true);

            // Step 2. 저장 → parentId를 자기 자신으로 설정 후 재저장
            Content saved = contentRepository.save(root);
            saved.setParentId(saved.getIdx());
            contentRepository.save(saved);

            loggingUtil.logSuccess(Action.CREATE, "Root content created: idx=" + saved.getIdx());
        } catch (IllegalArgumentException e) {
            loggingUtil.logFail(Action.CREATE, "입력값 오류: " + e.getMessage());
            throw processException("Invalid input detected", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.CREATE, e.getMessage());
            throw processException("Failed to create root content", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void createChildContent(Long parentId, ContentRequest request) throws Exception {
        loggingUtil.logAttempt(Action.CREATE, "Try to create child content: parentId=" + parentId);

        try {
            JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
            // Step 1. max(sort) + 1 계산
            int nextSort = Optional.ofNullable(contentRepository.findMaxSortByParentId(parentId)).orElse(0) + 1;

            // Step 2. entity 생성
            Content child = contentMapper.toEntity(request);
            child.setHostname(user.hostname());
            child.setCreatedBy(user.userId());
            child.setParentId(parentId);
            child.setSort(nextSort);
            child.setIsUse(true);

            contentRepository.updateIsUseFalseByParentId(parentId);
            contentRepository.save(child);
            loggingUtil.logSuccess(Action.CREATE, "Child content created: parentId=" + parentId);
        } catch (IllegalArgumentException e) {
            loggingUtil.logFail(Action.CREATE, "입력값 오류: " + e.getMessage());
            throw processException("Invalid input detected", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.CREATE, e.getMessage());
            throw processException("Failed to create child content", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void updateContent(Long idx, ContentRequest request) throws Exception {
        loggingUtil.logAttempt(Action.UPDATE, "Try to update content: idx=" + idx);

        try {
            JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();

            Content content = contentRepository.findById(idx)
                    .orElseThrow(() -> new IllegalArgumentException("Content not found: " + idx));

            contentMapper.updateEntity(content, request);

            // 서버에서만 채워야 하는 정보
            content.setUpdatedBy(user.userId());
            content.setHostname(user.hostname());
            if (Boolean.TRUE.equals(request.getIsUse())) {
                contentRepository.updateIsUseFalseByParentId(content.getParentId());
            }

            contentRepository.save(content);

            loggingUtil.logSuccess(Action.UPDATE, "Content updated: idx=" + idx);
        } catch (IllegalArgumentException e) {
            loggingUtil.logFail(Action.CREATE, "입력값 오류: " + e.getMessage());
            throw processException("Invalid input detected", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, e.getMessage());
            throw processException("Failed to update content", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void syncUsageFlagsByContentIds(Set<String> inUseContentIds) throws Exception {
        loggingUtil.logAttempt(Action.UPDATE, "Sync content is_use flags by contentIds");

        try {
            // null/공백/빈 문자열 정리
            Set<String> ids = (inUseContentIds == null) ? Set.of()
                    : inUseContentIds.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

            if (ids.isEmpty()) {
                loggingUtil.logSuccess(Action.UPDATE, "No in-use content IDs found. Skipping is_use sync.");
                return; // 아무 것도 하지 않음
            }

            // IN 집합 ON
            int on = contentRepository.updateIsUseTrueByContentIdIn(ids);
            // NOT IN 집합 OFF
            int off = contentRepository.updateIsUseFalseByContentIdNotIn(ids);

            loggingUtil.logSuccess(Action.UPDATE,
                    "Content is_use synced. on=" + on + ", off=" + off + ", inUseSize=" + ids.size());
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.UPDATE, "DB error during content is_use sync");
            throw processException("Database error while syncing content usage flags", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "Unexpected error during content is_use sync: " + e.getMessage());
            throw processException("Failed to sync content usage flags", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void deleteContentByIdx(Long idx) throws Exception {
        loggingUtil.logAttempt(Action.DELETE, "Try to delete content: idx=" + idx);

        try {
            contentRepository.deleteById(idx);
            loggingUtil.logSuccess(Action.DELETE, "Content deleted: idx=" + idx);
        } catch (IllegalArgumentException e) {
            loggingUtil.logFail(Action.CREATE, "입력값 오류: " + e.getMessage());
            throw processException("Invalid input detected", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.DELETE, e.getMessage());
            throw processException("Failed to delete content", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void deleteContentByParentId(Long parentId) throws Exception {
        loggingUtil.logAttempt(Action.DELETE, "Try to delete group contents: parentId=" + parentId);

        try {
            contentRepository.deleteAllByParentIdOrIdx(parentId, parentId);
            loggingUtil.logSuccess(Action.DELETE, "Group contents deleted: parentId=" + parentId);
        } catch (IllegalArgumentException e) {
            loggingUtil.logFail(Action.CREATE, "입력값 오류: " + e.getMessage());
            throw processException("Invalid input detected", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.DELETE, e.getMessage());
            throw processException("Failed to delete group contents", e);
        }
    }
}
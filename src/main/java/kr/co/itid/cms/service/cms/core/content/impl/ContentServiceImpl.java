package kr.co.itid.cms.service.cms.core.content.impl;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.dto.cms.core.content.request.ChildContentRequest;
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

import java.util.*;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;

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
    public List<ContentResponse> getContentsByIdx(Long idx) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get group contents by idx=" + idx);

        try {
            Content content = contentRepository.findById(idx)
                    .orElseThrow(() -> new IllegalArgumentException("Content not found: " + idx));

            List<Content> list = contentRepository.findByParentIdOrderByCreatedDateDesc(content.getParentId());
            loggingUtil.logSuccess(Action.RETRIEVE, "Group contents loaded: idx=" + idx);
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
            Content content = contentRepository.findFirstByParentIdAndIsMainTrue(parentId)
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
            root.setCreatedBy(user.userId());
            root.setIsMain(true);

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
    public void createChildContent(Long idx, ContentRequest request) throws Exception {
        loggingUtil.logAttempt(Action.CREATE, "Try to create child content: idx=" + idx);
        try {
            JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();

            Content content = contentRepository.findById(idx)
                    .orElseThrow(() -> new IllegalArgumentException("Content not found: " + idx));
            // Step 1. entity 생성
            Content child = contentMapper.toEntity(request);
            child.setCreatedBy(user.userId());
            child.setParentId(content.getParentId());
            child.setIsUse(content.getIsUse());

            contentRepository.save(child);
            loggingUtil.logSuccess(Action.CREATE, "Child content created: parentId=" + content.getParentId());
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
    public void activeContent(Long idx) throws Exception {
        loggingUtil.logAttempt(Action.UPDATE, "Try to active content: idx=" + idx);
        try {
            // 1) 대상 로우 조회 (parentId 얻기)
            Content target = contentRepository.findById(idx)
                    .orElseThrow(() -> new IllegalArgumentException("Content not found: " + idx));

            Long parentId = target.getParentId() != null ? target.getParentId() : target.getIdx();

            // 2) 해당 그룹 전체 is_main=false
            contentRepository.updateIsMainFalseByParentId(parentId);

            // 3) 현재 idx만 is_main=true
            contentRepository.updateIsMainTrueByIdx(idx);

            loggingUtil.logSuccess(Action.UPDATE, "Content activated: idx=" + idx + ", parentId=" + parentId);
        } catch (org.springframework.dao.DataAccessException e) {
            loggingUtil.logFail(Action.UPDATE, "DB error: " + e.getMessage());
            throw processException("Database error during content activation", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "Unexpected error: " + e.getMessage());
            throw processException("Failed to activate content", e);
        }
    }


    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void syncUsageFlagsByContentIds(Set<String> inUseContentIds) throws Exception {
        loggingUtil.logAttempt(Action.UPDATE, "Sync content is_use flags by contentIds");
        try {
            Set<Long> ids = (inUseContentIds == null) ? Set.of()
                    : inUseContentIds.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> s.matches("\\d+"))
                    .map(Long::parseLong)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (ids.isEmpty()) {
                loggingUtil.logSuccess(Action.UPDATE, "No numeric content IDs. Skipping is_use sync.");
                return;
            }

            List<Content> selected = contentRepository.findByIdxIn(ids);
            if (selected.isEmpty()) {
                loggingUtil.logSuccess(Action.UPDATE, "No contents found for given IDs. Skipping.");
                return;
            }

            Map<String, Set<Long>> hostToParentIds = selected.stream()
                    .filter(c -> c.getParentId() != null)
                    .collect(Collectors.groupingBy(
                            Content::getHostname,
                            Collectors.mapping(Content::getParentId, Collectors.toCollection(LinkedHashSet::new))
                    ));

            int onTotal = 0, offTotal = 0;
            for (Map.Entry<String, Set<Long>> e : hostToParentIds.entrySet()) {
                String hostname = e.getKey();
                Set<Long> parentIds = e.getValue();
                if (parentIds.isEmpty()) continue;

                int on  = contentRepository.updateIsUseTrueByHostnameAndParentIdIn(hostname, parentIds);
                int off = contentRepository.updateIsUseFalseByHostnameAndParentIdNotIn(hostname, parentIds);

                onTotal  += on;
                offTotal += off;
            }

            loggingUtil.logSuccess(
                    Action.UPDATE,
                    "is_use synced per hostname. on=" + onTotal + ", off=" + offTotal +
                            ", idsSize=" + ids.size() + ", hostSize=" + hostToParentIds.size()
            );
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.UPDATE, "DB error during is_use sync");
            throw processException("Database error while syncing content usage flags", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "Unexpected error during is_use sync: " + e.getMessage());
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
    public void deleteContentByParentId(Long idx) throws Exception {
        loggingUtil.logAttempt(Action.DELETE, "Try to delete group contents: idx=" + idx);

        try {
            // 1) 대상 로우 조회 (parentId 얻기)
            Content target = contentRepository.findById(idx)
                    .orElseThrow(() -> new IllegalArgumentException("Content not found: " + idx));
            contentRepository.deleteAllByParentId(target.getParentId());
            loggingUtil.logSuccess(Action.DELETE, "Group contents deleted: parentId=" + target.getParentId());
        } catch (IllegalArgumentException e) {
            loggingUtil.logFail(Action.CREATE, "입력값 오류: " + e.getMessage());
            throw processException("Invalid input detected", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.DELETE, e.getMessage());
            throw processException("Failed to delete group contents", e);
        }
    }
}
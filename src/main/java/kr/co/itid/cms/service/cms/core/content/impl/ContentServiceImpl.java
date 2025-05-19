package kr.co.itid.cms.service.cms.core.content.impl;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.dto.cms.core.content.ContentRequest;
import kr.co.itid.cms.dto.cms.core.content.ContentResponse;
import kr.co.itid.cms.entity.cms.core.content.Content;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.mapper.cms.core.content.ContentMapper;
import kr.co.itid.cms.repository.cms.core.content.ContentRepository;
import kr.co.itid.cms.service.cms.core.content.ContentService;
import kr.co.itid.cms.util.LoggingUtil;
import kr.co.itid.cms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("contentService")
@RequiredArgsConstructor
public class ContentServiceImpl extends EgovAbstractServiceImpl implements ContentService {

    private final ContentRepository contentRepository;
    private final ContentMapper contentMapper;
    private final LoggingUtil loggingUtil;

    @Override
    @Transactional(readOnly = true)
    public List<ContentResponse> getTopSortedContents() throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get top-sorted contents");

        try {
            List<Content> list = contentRepository.findBySortAndIsUseTrue(0);
            loggingUtil.logSuccess(Action.RETRIEVE, "Top-sorted contents loaded");
            return contentMapper.toResponseList(list);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, e.getMessage());
            throw processException("Failed to load top-sorted contents", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public ContentResponse getContentByIdx(Long idx) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get content by idx=" + idx);

        try {
            Content content = contentRepository.findById(idx)
                    .orElseThrow(() -> new IllegalArgumentException("Content not found: " + idx));
            loggingUtil.logSuccess(Action.RETRIEVE, "Content loaded: idx=" + idx);
            return contentMapper.toResponse(content);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, e.getMessage());
            throw processException("Failed to get content", e);
        }
    }

    @Override
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
    @Transactional
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
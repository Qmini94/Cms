package kr.co.itid.cms.service.cms.core.content.impl;

import kr.co.itid.cms.dto.cms.core.content.ContentRequest;
import kr.co.itid.cms.dto.cms.core.content.ContentResponse;
import kr.co.itid.cms.entity.cms.core.content.Content;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.mapper.cms.core.content.ContentMapper;
import kr.co.itid.cms.repository.cms.core.content.ContentRepository;
import kr.co.itid.cms.service.cms.core.content.ContentService;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service("contentService")
@RequiredArgsConstructor
public class ContentServiceImpl extends EgovAbstractServiceImpl implements ContentService {

    private final ContentRepository contentRepository;
    private final ContentMapper contentMapper;
    private final LoggingUtil loggingUtil;

    @Override
    @Transactional(readOnly = true)
    public List<ContentResponse> getLatestContentsPerMenu() throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get latest content per menu");

        try {
            List<Content> contents = contentRepository.findLatestContentPerMenu(); // 구현 필요
            loggingUtil.logSuccess(Action.RETRIEVE, "Latest content list loaded per menu");
            return contents.stream().map(contentMapper::toResponse).collect(Collectors.toList());
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "DB error: " + e.getMessage());
            throw processException("DB error. " + e.getMessage(), e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error: " + e.getMessage());
            throw processException("Failed to get latest content. " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContentResponse> getContentsByMenuIdx(Integer menuIdx) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get contents by menuIdx: " + menuIdx);

        try {
            List<Content> contents = contentRepository.findByMenuIdxOrderByCreatedDateDesc(menuIdx);
            loggingUtil.logSuccess(Action.RETRIEVE, "Contents loaded: menuIdx=" + menuIdx);
            return contents.stream().map(contentMapper::toResponse).collect(Collectors.toList());
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "DB error: " + e.getMessage());
            throw processException("DB error. " + e.getMessage(), e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error: " + e.getMessage());
            throw processException("Failed to get contents. " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ContentResponse getByIdx(Integer idx) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get content by idx: " + idx);

        try {
            Content content = contentRepository.findById(idx)
                    .orElseThrow(() -> new IllegalArgumentException("Content not found: " + idx));
            loggingUtil.logSuccess(Action.RETRIEVE, "Content loaded: idx=" + idx);
            return contentMapper.toResponse(content);
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "DB error: " + e.getMessage());
            throw processException("DB error. " + e.getMessage(), e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error: " + e.getMessage());
            throw processException("Failed to get content. " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void create(ContentRequest request) throws Exception {
        loggingUtil.logAttempt(Action.CREATE, "Try to create content for menuIdx=" + request.getMenuIdx());

        try {
            contentRepository.updateIsUseFalseByMenuIdx(request.getMenuIdx()); // 기존 데이터 비활성화

            Content content = contentMapper.toEntity(request, null);
            Content saved = contentRepository.save(content);

            loggingUtil.logSuccess(Action.CREATE, "Content created: idx=" + saved.getIdx());
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.CREATE, "DB error: " + e.getMessage());
            throw processException("DB error. " + e.getMessage(), e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.CREATE, "Failed to create content: " + e.getMessage());
            throw processException("Create failed. " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void update(Integer idx, ContentRequest request) throws Exception {
        loggingUtil.logAttempt(Action.UPDATE, "Try to update content: idx=" + idx);

        try {
            Content content = contentRepository.findById(idx)
                    .orElseThrow(() -> new IllegalArgumentException("Content not found: " + idx));

            contentMapper.updateEntity(content, request);
            contentRepository.save(content);

            loggingUtil.logSuccess(Action.UPDATE, "Content updated: idx=" + idx);
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.UPDATE, "DB error: " + e.getMessage());
            throw processException("DB error. " + e.getMessage(), e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "Failed to update content: " + e.getMessage());
            throw processException("Update failed. " + e.getMessage(), e);
        }
    }
}
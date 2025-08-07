package kr.co.itid.cms.service.cms.core.board.impl;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterListResponse;
import kr.co.itid.cms.dto.cms.core.board.request.BoardMasterRequest;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterResponse;
import kr.co.itid.cms.entity.cms.core.board.BoardMaster;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.mapper.cms.core.board.BoardMasterMapper;
import kr.co.itid.cms.repository.cms.core.board.BoardMasterDao;
import kr.co.itid.cms.repository.cms.core.board.BoardMasterRepository;
import kr.co.itid.cms.service.cms.core.board.BoardMasterService;
import kr.co.itid.cms.util.LoggingUtil;
import kr.co.itid.cms.util.SecurityUtil;
import kr.co.itid.cms.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("boardMasterService")
@RequiredArgsConstructor
public class BoardMasterServiceImpl extends EgovAbstractServiceImpl implements BoardMasterService {

    private final BoardMasterRepository boardMasterRepository;
    private final BoardMasterMapper boardMapper;
    private final BoardMasterDao boardMasterDao;
    private final LoggingUtil loggingUtil;
    private final ValidationUtil validationUtil;

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public Page<BoardMasterListResponse> searchBoardMasters(SearchOption option, Pageable pageable) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to search board master list");

        try {
            // 1. 검색 및 페이징 처리
            Page<BoardMaster> resultPage = boardMasterRepository.searchByCondition(option, pageable);

            loggingUtil.logSuccess(Action.RETRIEVE, "Board master list retrieved successfully (total=" + resultPage.getTotalElements() + ")");

            // 2. 매핑
            return resultPage.map(boardMapper::toListResponse);

        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "DB error: " + e.getMessage());
            throw processException("DB 오류가 발생했습니다.", e);

        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error: " + e.getMessage());
            throw processException("게시판 마스터 목록 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public BoardMasterResponse getBoardByIdx(Long idx) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "게시판 단건 조회 시도: idx=" + idx);

        try {
            BoardMasterResponse response = boardMasterDao.findBoardMasterByIdx(idx);
            if (response == null) {
                throw new IllegalArgumentException("게시판이 존재하지 않습니다: idx=" + idx);
            }

            loggingUtil.logSuccess(Action.RETRIEVE, "게시판 조회 성공: " + response.getBoardId());
            return response;
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "게시판 조회 실패: " + e.getMessage());
            throw processException("게시판 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void createBoard(BoardMasterRequest request) throws Exception {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        validationUtil.validateBadWords(request, user);

        loggingUtil.logAttempt(Action.CREATE, "게시판 생성 시도: board_id=" + request.getBoardId());

        try {
            // 1. 필드 정의 먼저 등록
            boardMasterDao.insertBoardFieldDefinitions(request);

            // 2. 게시판 메타 등록
            boardMasterDao.insertBoardMaster(request);

            // 3. 게시판 실제 테이블 생성
            boardMasterDao.createBoardTable(request.getBoardId());

            loggingUtil.logSuccess(Action.CREATE, "게시판 생성 완료: board_id=" + request.getBoardId());

        } catch (Exception e) {
            loggingUtil.logFail(Action.CREATE, "게시판 생성 실패: " + e.getMessage());
            throw processException("게시판 생성 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void updateBoard(Long idx, BoardMasterRequest request) throws Exception {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        validationUtil.validateBadWords(request, user);

        loggingUtil.logAttempt(Action.UPDATE, "게시판 수정 시도: idx=" + idx);

        try {
            // 1. 필드 정의 갱신
            boardMasterDao.updateBoardFieldDefinitions(idx, request);

            // 2. 게시판 메타 수정
            boardMasterDao.updateBoardMaster(idx, request);

            // 3. 필요 시 테이블 구조 반영 (현재 생략)
            loggingUtil.logSuccess(Action.UPDATE, "게시판 수정 완료: idx=" + idx);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "게시판 수정 실패: " + e.getMessage());
            throw processException("게시판 수정 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void deleteBoard(Long idx) throws Exception {
        loggingUtil.logAttempt(Action.DELETE, "게시판 삭제 시도: idx=" + idx);

        try {
            String boardId = boardMasterDao.findBoardIdByIdx(idx);
            if (boardId == null) {
                throw new IllegalArgumentException("해당 게시판 ID를 찾을 수 없습니다.");
            }

            // 1. 필드 정의 삭제
            boardMasterDao.deleteBoardFieldDefinitions(idx);

            // 2. 게시판 메타 삭제
            boardMasterDao.deleteBoardMaster(idx);

            // 3. 게시판 테이블 삭제
            boardMasterDao.dropBoardTable(boardId);

            loggingUtil.logSuccess(Action.DELETE, "게시판 삭제 완료: board_id=" + boardId);

        } catch (Exception e) {
            loggingUtil.logFail(Action.DELETE, "게시판 삭제 실패: " + e.getMessage());
            throw processException("게시판 삭제 중 오류가 발생했습니다.", e);
        }
    }
}
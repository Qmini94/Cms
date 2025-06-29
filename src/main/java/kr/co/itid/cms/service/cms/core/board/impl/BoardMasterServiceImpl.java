package kr.co.itid.cms.service.cms.core.board.impl;

import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterListResponse;
import kr.co.itid.cms.dto.cms.core.board.request.BoardMasterRequest;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterResponse;
import kr.co.itid.cms.entity.cms.core.board.BoardMaster;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.mapper.cms.core.board.BoardMasterMapper;
import kr.co.itid.cms.repository.cms.core.board.BoardMasterRepository;
import kr.co.itid.cms.service.cms.core.board.BoardMasterService;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("boardMasterService")
@RequiredArgsConstructor
public class BoardMasterServiceImpl extends EgovAbstractServiceImpl implements BoardMasterService {

    private final BoardMasterRepository boardMasterRepository;
    private final BoardMasterMapper boardMapper;
    private final LoggingUtil loggingUtil;

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public List<BoardMasterListResponse> getAllBoards() throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get board list");

        try {
            List<BoardMaster> list = boardMasterRepository.findAll();
            loggingUtil.logSuccess(Action.RETRIEVE, "Board list loaded");
            return boardMapper.toResponseList(list);
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "DB error: " + e.getMessage());
            throw processException("DB error. " + e.getMessage(), e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error: " + e.getMessage());
            throw processException("Something went wrong. " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public BoardMasterResponse getBoardByBoardId(String boardId) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get board by boardId: " + boardId);

        try {
            BoardMaster entity = boardMasterRepository.findByBoardId(boardId)
                    .orElseThrow(() -> new IllegalArgumentException("게시판 모듈이 존재하지 않습니다: " + boardId));

            return boardMapper.toResponse(entity);
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "DB error: " + e.getMessage());
            throw processException("DB error. " + e.getMessage(), e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error: " + e.getMessage());
            throw processException("Something went wrong. " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void saveBoard(Long idx, BoardMasterRequest request) throws Exception {
        BoardMaster boardMaster = boardMapper.toEntity(request, idx); // 주입된 매퍼 사용
        boolean isNew = (idx == null);
        Action action = isNew ? Action.CREATE : Action.UPDATE;

        loggingUtil.logAttempt(action, "Try to " + action.getValue() + " board: " + boardMaster.getBoardId());

        try {
            BoardMaster saved = boardMasterRepository.save(boardMaster);
            loggingUtil.logSuccess(action, "Board " + action.getValue() + "d: idx=" + saved.getIdx());
        } catch (IllegalArgumentException e) {
            loggingUtil.logFail(Action.CREATE, "입력값 오류: " + e.getMessage());
            throw processException("Invalid input detected", e);
        } catch (DataAccessException e) {
            loggingUtil.logFail(action, "DB error: " + e.getMessage());
            throw processException("DB error. " + e.getMessage(), e);
        } catch (Exception e) {
            loggingUtil.logFail(action, "Failed to " + action.getValue() + " board: " + e.getMessage());
            throw processException("Save failed. " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void deleteBoard(Long idx) throws Exception {
        loggingUtil.logAttempt(Action.DELETE, "Try to delete board: idx=" + idx);

        try {
            boardMasterRepository.deleteById(idx);
            loggingUtil.logSuccess(Action.DELETE, "Board deleted: idx=" + idx);
        } catch (IllegalArgumentException e) {
            loggingUtil.logFail(Action.CREATE, "입력값 오류: " + e.getMessage());
            throw processException("Invalid input detected", e);
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.DELETE, "DB error: " + e.getMessage());
            throw processException("DB error. " + e.getMessage(), e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.DELETE, "Delete failed: " + e.getMessage());
            throw processException("Delete failed. " + e.getMessage(), e);
        }
    }
}
package kr.co.itid.cms.service.cms.core.impl;

import kr.co.itid.cms.entity.cms.core.BoardMaster;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.repository.cms.core.BoardMasterRepository;
import kr.co.itid.cms.service.cms.core.BoardMasterService;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("boardMasterService")
@RequiredArgsConstructor
public class BoardMasterServiceImpl extends EgovAbstractServiceImpl implements BoardMasterService {

    private final BoardMasterRepository boardMasterRepository;
    private final LoggingUtil loggingUtil;

    @Override
    public List<BoardMaster> getAllBoards() throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get board list");

        try {
            List<BoardMaster> list = boardMasterRepository.findAll();
            loggingUtil.logSuccess(Action.RETRIEVE, "Board list loaded");
            return list;
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "DB error: " + e.getMessage());
            throw processException("DB error. " + e.getMessage(), e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error: " + e.getMessage());
            throw processException("Something went wrong. " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<BoardMaster> getBoardById(Long id) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get board by ID: " + id);

        try {
            Optional<BoardMaster> board = boardMasterRepository.findById(id);
            loggingUtil.logSuccess(Action.RETRIEVE, "Board loaded: id=" + id);
            return board;
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "DB error: " + e.getMessage());
            throw processException("DB error. " + e.getMessage(), e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error: " + e.getMessage());
            throw processException("Something went wrong. " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<BoardMaster> getBoardByBoardId(String boardId) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get board by boardId: " + boardId);

        try {
            Optional<BoardMaster> board = boardMasterRepository.findByBoardId(boardId);
            loggingUtil.logSuccess(Action.RETRIEVE, "Board loaded: boardId=" + boardId);
            return board;
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "DB error: " + e.getMessage());
            throw processException("DB error. " + e.getMessage(), e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error: " + e.getMessage());
            throw processException("Something went wrong. " + e.getMessage(), e);
        }
    }

    @Override
    public BoardMaster save(BoardMaster boardMaster) throws Exception {
        boolean isNew = (boardMaster.getId() == null);
        Action action = isNew ? Action.CREATE : Action.UPDATE;

        loggingUtil.logAttempt(action, "Try to " + action.getValue() + " board: " + boardMaster.getBoardId());

        try {
            BoardMaster saved = boardMasterRepository.save(boardMaster);
            loggingUtil.logSuccess(action, "Board " + action.getValue() + "d: id=" + saved.getId());
            return saved;
        } catch (DataAccessException e) {
            loggingUtil.logFail(action, "DB error: " + e.getMessage());
            throw processException("DB error. " + e.getMessage(), e);
        } catch (Exception e) {
            loggingUtil.logFail(action, "Failed to " + action.getValue() + " board: " + e.getMessage());
            throw processException("Save failed. " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Long id) throws Exception {
        loggingUtil.logAttempt(Action.DELETE, "Try to delete board: id=" + id);

        try {
            boardMasterRepository.deleteById(id);
            loggingUtil.logSuccess(Action.DELETE, "Board deleted: id=" + id);
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.DELETE, "DB error: " + e.getMessage());
            throw processException("DB error. " + e.getMessage(), e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.DELETE, "Delete failed: " + e.getMessage());
            throw processException("Delete failed. " + e.getMessage(), e);
        }
    }
}
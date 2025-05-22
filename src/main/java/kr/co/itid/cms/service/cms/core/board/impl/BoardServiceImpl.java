package kr.co.itid.cms.service.cms.core.board.impl;

import kr.co.itid.cms.dto.cms.core.board.BoardSearchOption;
import kr.co.itid.cms.dto.cms.core.board.request.BoardRequest;
import kr.co.itid.cms.dto.cms.core.board.response.BoardResponse;
import kr.co.itid.cms.entity.cms.core.board.Board;
import kr.co.itid.cms.entity.cms.core.board.BoardMaster;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.mapper.cms.core.board.BoardMapper;
import kr.co.itid.cms.repository.cms.core.board.BoardMasterRepository;
import kr.co.itid.cms.repository.cms.core.board.BoardRepository;
import kr.co.itid.cms.service.cms.core.board.BoardService;
import kr.co.itid.cms.util.LoggingUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service("boardService")
@RequiredArgsConstructor
public class BoardServiceImpl extends EgovAbstractServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final BoardMasterRepository boardMasterRepository;
    private final BoardMapper boardMapper;
    private final LoggingUtil loggingUtil;

    private static final String TARGET_NAME = "게시글";

    @Override
    @Transactional(readOnly = true)
    public Page<BoardResponse> searchBoardList(BoardSearchOption option) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Attempting to search board list (boardId=" + option.getBoardId() + ")");

        try {
            Pageable pageable = option.getPageable();
            if (pageable == null) {
                BoardMaster master = boardMasterRepository.findByBoardId(option.getBoardId())
                        .orElseThrow(() -> new IllegalArgumentException("Board does not exist: boardId=" + option.getBoardId()));
                pageable = PageRequest.of(0, master.getListCount(), Sort.by(Sort.Direction.DESC, "createdDate"));
            }

            Page<Board> resultPage = boardRepository.searchByCondition(option, pageable);
            loggingUtil.logSuccess(Action.RETRIEVE, "Board list retrieved successfully (total=" + resultPage.getTotalElements() + ")");
            return resultPage.map(boardMapper::toResponse);

        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Failed to retrieve board list: " + e.getMessage());
            throw processException("Failed to retrieve board list", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BoardResponse getBoard(Long idx) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get board: idx=" + idx);

        try {
            Board board = boardRepository.findById(idx)
                    .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다: idx=" + idx));

            loggingUtil.logSuccess(Action.RETRIEVE, "Board loaded: idx=" + idx);
            return boardMapper.toResponse(board);
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "DB error: " + e.getMessage());
            throw processException("DB error. " + e.getMessage(), e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Failed to get board: " + e.getMessage());
            throw processException("Retrieve failed. " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void saveBoard(Long idx, BoardRequest request) throws Exception {
        boolean isNew = (idx == null);
        Action action = isNew ? Action.CREATE : Action.UPDATE;

        loggingUtil.logAttempt(action, "Try to " + action.getValue() + " board");

        try {
            if (isNew) {
                Board entity = boardMapper.toEntity(request);
                boardRepository.save(entity);
                loggingUtil.logSuccess(action, "Board created: boardId=" + entity.getBoardId());
            } else {
                Board board = boardRepository.findById(idx)
                        .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다: idx=" + idx));

                board.setTitle(request.getTitle());
                board.setContents(request.getContents());
                board.setUpdatedDate(LocalDateTime.now());
                // TODO: 필요한 필드 추가 반영
                loggingUtil.logSuccess(action, "Board updated: idx=" + idx);
            }
        } catch (IllegalArgumentException e) {
            loggingUtil.logFail(action, "입력값 오류: " + e.getMessage());
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
    @Transactional
    public void deleteBoard(Long idx) throws Exception {
        loggingUtil.logAttempt(Action.DELETE, "Try to delete board: idx=" + idx);

        try {
            Board board = boardRepository.findById(idx)
                    .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다: idx=" + idx));

            board.setIsDeleted(true);
            loggingUtil.logSuccess(Action.DELETE, "Board deleted (soft): idx=" + idx);
        } catch (IllegalArgumentException e) {
            loggingUtil.logFail(Action.DELETE, "입력값 오류: " + e.getMessage());
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
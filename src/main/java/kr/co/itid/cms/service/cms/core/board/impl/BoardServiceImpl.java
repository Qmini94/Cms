package kr.co.itid.cms.service.cms.core.board.impl;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
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
import kr.co.itid.cms.service.cms.core.menu.MenuService;
import kr.co.itid.cms.util.LoggingUtil;
import kr.co.itid.cms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
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

    private final MenuService menuService;
    private final BoardRepository boardRepository;
    private final BoardMasterRepository boardMasterRepository;
    private final BoardMapper boardMapper;
    private final LoggingUtil loggingUtil;

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public Page<BoardResponse> searchBoardList(BoardSearchOption option, Pageable pageable) throws Exception {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        Long menuId = user.menuId();

        loggingUtil.logAttempt(Action.RETRIEVE, "Try to search board list: user=" + user.userId() + ", menuId=" + menuId);

        try {
            // 1. menuId → boardId
            String boardId = menuService.getMenuRenderById(menuId).getValue();

            if (boardId == null) {
                loggingUtil.logFail(Action.RETRIEVE, "메뉴에 연결된 게시판 ID가 없습니다. menuId=" + menuId);
                throw processException("메뉴에 연결된 게시판이 존재하지 않습니다.");
            }

            // 2. boardId → BoardMaster (게시판 기본 설정)
            BoardMaster master = boardMasterRepository.findByBoardId(boardId)
                    .orElseThrow(() -> {
                        loggingUtil.logFail(Action.RETRIEVE, "BoardMaster not found: boardId=" + boardId);
                        return processException("게시판 설정이 존재하지 않습니다.");
                    });

            // 3. Pageable 보정
            if (pageable == null) {
                pageable = PageRequest.of(0, master.getListCount(), Sort.by(Sort.Direction.DESC, "createdDate"));
            }

            // 4. boardId 포함된 실제 조회용 Option 재생성
            BoardSearchOption actualOption = new BoardSearchOption();
            actualOption.setSearchKeys(option.getSearchKeys());
            actualOption.setKeyword(option.getKeyword());
            actualOption.setStartDate(option.getStartDate());
            actualOption.setEndDate(option.getEndDate());

            // 5. 조회
            Page<Board> resultPage = boardRepository.searchByCondition(boardId, actualOption, pageable);
            loggingUtil.logSuccess(Action.RETRIEVE, "Board list retrieved successfully (total=" + resultPage.getTotalElements() + ")");

            return resultPage.map(boardMapper::toResponse);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Board list search failed: " + e.getMessage());
            throw processException("게시판 목록 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class) // readOnly 제거
    public BoardResponse getBoard(Long idx) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to get board: idx=" + idx);

        try {
            Board board = boardRepository.findById(idx)
                    .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다: idx=" + idx));

            validateBoardOwnership(board);
            board.increaseViewCount();

            loggingUtil.logSuccess(Action.RETRIEVE, "Board loaded: idx=" + idx);
            return boardMapper.toResponse(board);

        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Failed to get board: " + e.getMessage());
            throw processException("게시글 조회 실패", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void saveBoard(Long idx, BoardRequest request) throws Exception {
        boolean isNew = (idx == null);
        Action action = isNew ? Action.CREATE : Action.UPDATE;

        loggingUtil.logAttempt(action, "Try to " + action.getValue() + " board");

        try {
            if (isNew) {
                Board entity = boardMapper.toEntity(request);
                validateBoardOwnership(entity);
                boardRepository.save(entity);
                loggingUtil.logSuccess(action, "Board created: boardId=" + entity.getBoardId());
            } else {
                Board board = boardRepository.findById(idx)
                        .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다: idx=" + idx));
                validateBoardOwnership(board);
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
    @Transactional(rollbackFor = EgovBizException.class)
    public void deleteBoard(Long idx) throws Exception {
        loggingUtil.logAttempt(Action.DELETE, "Try to delete board: idx=" + idx);

        try {
            Board board = boardRepository.findById(idx)
                    .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다: idx=" + idx));
            validateBoardOwnership(board);
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

    private void validateBoardOwnership(Board board) throws Exception {
        if (board == null || board.getBoardId() == null) {
            loggingUtil.logFail(Action.VALIDATE, "게시판 정보가 null이거나 boardId가 없습니다.");
            throw processException("게시판 정보가 올바르지 않습니다.");
        }

        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        String expectedBoardId = menuService.getMenuRenderById(user.menuId()).getValue();
        String actualBoardId = board.getBoardId();

        if (!actualBoardId.equals(expectedBoardId)) {
            loggingUtil.logFail(
                    Action.VALIDATE,
                    "게시판 권한 불일치: user=" + user.userId() +
                            ", menuId=" + user.menuId() +
                            ", expectedBoardId=" + expectedBoardId +
                            ", actualBoardId=" + actualBoardId
            );
            throw processException("게시판 권한이 없습니다.");
        }
    }

}
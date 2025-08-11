package kr.co.itid.cms.service.cms.core.board.impl;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.dto.cms.core.board.request.*;
import kr.co.itid.cms.dto.cms.core.board.response.BoardFieldDefinitionResponse;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterListResponse;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterResponse;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
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

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service("boardMasterService")
@RequiredArgsConstructor
public class BoardMasterServiceImpl extends EgovAbstractServiceImpl implements BoardMasterService {

    private final BoardMasterRepository boardMasterRepository;
    private final BoardMasterMapper boardMapper;
    private final BoardMasterDao boardMasterDao;
    private final LoggingUtil loggingUtil;
    private final ValidationUtil validationUtil;

    /** 목록 */
    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public Page<BoardMasterListResponse> searchBoardMasters(SearchOption option, Pageable pageable) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "Try to search board master list");
        try {
            Page<BoardMaster> resultPage = boardMasterRepository.searchByCondition(option, pageable);
            loggingUtil.logSuccess(Action.RETRIEVE, "Board master list retrieved successfully (total=" + resultPage.getTotalElements() + ")");
            return resultPage.map(boardMapper::toListResponse);
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "DB error: " + e.getMessage());
            throw processException("DB 오류가 발생했습니다.", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unknown error: " + e.getMessage());
            throw processException("게시판 마스터 목록 조회 중 오류가 발생했습니다.", e);
        }
    }

    /** 단건 */
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

    /**
     * 생성: master insert → field insert → CREATE TABLE
     * DDL 실패 시 master/fields 보상 삭제 + safeDrop
     */
    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void createBoard(BoardCreateRequest request) throws Exception {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        validationUtil.validateBadWords(request.getMaster(), user);

        final String boardId = request.getMaster().getBoardId();
        loggingUtil.logAttempt(Action.CREATE, "게시판 생성 시도: board_id=" + boardId);

        Long masterIdx = null;
        try {
            // 1) master insert → idx 반환
            masterIdx = boardMasterDao.insertBoardMasterReturningIdx(request.getMaster());

            // 2) field definitions insert
            boardMasterDao.insertBoardFieldDefinitions(masterIdx, request.getFields());

            // 3) physical table create (DDL; MySQL/Maria는 암묵커밋)
            boardMasterDao.createBoardTable(boardId);

            loggingUtil.logSuccess(Action.CREATE, "게시판 생성 완료: board_id=" + boardId + ", masterIdx=" + masterIdx);
        } catch (Exception e) {
            loggingUtil.logFail(Action.CREATE, "게시판 생성 실패: " + e.getMessage());
            // === 보상 롤백(DDL 실패 시 논리 데이터 복구) ===
            try {
                if (masterIdx != null) {
                    boardMasterDao.deleteBoardFieldDefinitions(masterIdx);
                    boardMasterDao.deleteBoardMaster(masterIdx);
                }
            } catch (Exception comp) {
                loggingUtil.logFail(Action.CREATE, "보상(논리 데이터 삭제) 실패: " + comp.getMessage());
            }
            try {
                boardMasterDao.safeDropBoardTable(boardId); // 존재하면 드롭, 없으면 무시
            } catch (Exception comp) {
                loggingUtil.logFail(Action.CREATE, "보상(테이블 드롭) 실패: " + comp.getMessage());
            }
            throw processException("게시판 생성 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 수정: master update → fields replace → sync(DDL)
     * DDL 실패 시 스냅샷으로 복원 + 재동기화
     */
    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void updateBoard(BoardUpdateRequest request) throws Exception {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        validationUtil.validateBadWords(request.getMaster(), user);

        final Long idx = request.getIdx();
        loggingUtil.logAttempt(Action.UPDATE, "게시판 수정 시도: idx=" + idx);

        // === 스냅샷 확보(보상 복원용) ===
        final BoardMasterRequest oldMaster = boardMasterDao.getMasterSnapshot(idx);   // 필요 시 DAO에 구현
        final List<BoardFieldDefinitionRequest> oldFields = boardMasterDao.getFieldSnapshot(idx);

        try {
            // 1) master update
            boardMasterDao.updateBoardMaster(idx, request.getMaster());

            // 2) field definitions replace (delete → insert)
            boardMasterDao.replaceBoardFieldDefinitions(idx, request.getFields());

            // 3) sync physical table with definitions (DDL; 실패 가능)
            boardMasterDao.syncPhysicalTableWithDefinitions(idx);

            loggingUtil.logSuccess(Action.UPDATE, "게시판 수정 및 스키마 동기화 완료: idx=" + idx);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "게시판 수정 실패: " + e.getMessage());

            // === 보상 롤백(이전 상태로 복원 후 재동기화) ===
            try {
                if (oldMaster != null) {
                    boardMasterDao.updateBoardMaster(idx, oldMaster);
                }
                if (oldFields != null) {
                    boardMasterDao.replaceBoardFieldDefinitions(idx, oldFields);
                }
                // 이전 정의 기준으로 다시 sync 시도 (대부분 no-op이거나 역 ALTER)
                boardMasterDao.syncPhysicalTableWithDefinitions(idx);
                loggingUtil.logSuccess(Action.UPDATE, "보상 복원 완료: idx=" + idx);
            } catch (Exception comp) {
                loggingUtil.logFail(Action.UPDATE, "보상 복원 실패(운영 확인 필요): " + comp.getMessage());
            }
            throw processException("게시판 수정 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 삭제: defs → master → DROP TABLE
     * DROP 실패 시 이전 데이터 복원 시도(선택)
     */
    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void deleteBoard(Long idx) throws Exception {
        loggingUtil.logAttempt(Action.DELETE, "게시판 삭제 시도: idx=" + idx);
        // === 스냅샷(복원 대비) ===
        final String boardId = boardMasterDao.findBoardIdByIdx(idx);
        if (boardId == null) throw new IllegalArgumentException("해당 게시판 ID를 찾을 수 없습니다.");
        final BoardMasterRequest oldMaster = boardMasterDao.getMasterSnapshot(idx);
        final List<BoardFieldDefinitionRequest> oldFields = boardMasterDao.getFieldSnapshot(idx);

        try {
            // 1) field definitions delete
            boardMasterDao.deleteBoardFieldDefinitions(idx);

            // 2) master delete
            boardMasterDao.deleteBoardMaster(idx);

            // 3) drop physical table (DDL)
            boardMasterDao.dropBoardTable(boardId);

            loggingUtil.logSuccess(Action.DELETE, "게시판 삭제 완료: board_id=" + boardId);
        } catch (Exception e) {
            loggingUtil.logFail(Action.DELETE, "게시판 삭제 실패: " + e.getMessage());
            // === 보상: 테이블이 남았는데 메타만 지워졌다면 복구 시도 ===
            try {
                if (oldMaster != null) {
                    Long restoredIdx = boardMasterDao.insertBoardMasterReturningIdx(oldMaster);
                    if (oldFields != null) {
                        boardMasterDao.insertBoardFieldDefinitions(restoredIdx, oldFields);
                    }
                    // 정의와 물리 재동기화 (테이블 존재 시 no-op)
                    boardMasterDao.syncPhysicalTableWithDefinitions(restoredIdx);
                }
            } catch (Exception comp) {
                loggingUtil.logFail(Action.DELETE, "삭제 보상(복구) 실패: " + comp.getMessage());
            }
            throw processException("게시판 삭제 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void syncUsageFlagsByBoardIds(Set<String> inUseBoardIds) throws Exception {
        loggingUtil.logAttempt(Action.UPDATE, "Sync board is_use flags by boardIds");
        try {
            // null/공백/빈 문자열 정리
            Set<String> ids = (inUseBoardIds == null) ? Set.of()
                    : inUseBoardIds.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

            if (ids.isEmpty()) {
                // 전역 OFF 금지: 다른 사이트/드라이브 오염 방지 위해 스킵
                loggingUtil.logSuccess(Action.UPDATE, "No in-use board IDs found. Skipping is_use sync.");
                return;
            }

            int on  = boardMasterRepository.updateIsUseTrueByBoardIdIn(ids);
            int off = boardMasterRepository.updateIsUseFalseByBoardIdNotIn(ids);

            loggingUtil.logSuccess(
                    Action.UPDATE,
                    "Board is_use synced. on=" + on + ", off=" + off + ", inUseSize=" + ids.size()
            );
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.UPDATE, "DB error during board is_use sync");
            throw processException("Database error while syncing board usage flags", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "Unexpected error during board is_use sync: " + e.getMessage());
            throw processException("Failed to sync board usage flags", e);
        }
    }

    /** 필드 정의 조회 */
    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public List<BoardFieldDefinitionResponse> getFieldDefinitions(Long boardMasterIdx) throws Exception {
        loggingUtil.logAttempt(Action.RETRIEVE, "필드 정의 조회 시도: masterIdx=" + boardMasterIdx);
        try {
            List<BoardFieldDefinitionResponse> list = boardMasterDao.selectFieldDefinitions(boardMasterIdx);
            loggingUtil.logSuccess(Action.RETRIEVE, "필드 정의 조회 성공: count=" + (list != null ? list.size() : 0));
            return list;
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "필드 정의 조회 실패: " + e.getMessage());
            throw processException("필드 정의 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 필드 전용 업서트 + 동기화
     * DDL 실패 시 이전 필드 정의로 복원 + 재동기화
     */
    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void upsertFieldDefinitionsAndSync(BoardFieldDefinitionsUpsertRequest request) throws Exception {
        final Long masterIdx = request.getBoardMasterIdx();
        loggingUtil.logAttempt(Action.UPDATE, "필드 정의 업서트 + 동기화 시도: masterIdx=" + masterIdx);

        // === 스냅샷 ===
        final List<BoardFieldDefinitionRequest> oldFields = boardMasterDao.getFieldSnapshot(masterIdx);

        try {
            // 1) replace definitions
            boardMasterDao.replaceBoardFieldDefinitions(masterIdx, request.getFields());

            // 2) sync physical table (DDL)
            boardMasterDao.syncPhysicalTableWithDefinitions(masterIdx);

            loggingUtil.logSuccess(Action.UPDATE, "필드 정의 업서트 + 동기화 완료: masterIdx=" + masterIdx);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "필드 정의 업서트/동기화 실패: " + e.getMessage());
            // === 보상: 이전 필드로 복원 + 재동기화 ===
            try {
                if (oldFields != null) {
                    boardMasterDao.replaceBoardFieldDefinitions(masterIdx, oldFields);
                    boardMasterDao.syncPhysicalTableWithDefinitions(masterIdx);
                }
            } catch (Exception comp) {
                loggingUtil.logFail(Action.UPDATE, "필드 보상 복구 실패: " + comp.getMessage());
            }
            throw processException("필드 정의 업서트/동기화 중 오류가 발생했습니다.", e);
        }
    }

    /** 강제 동기화 */
    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void syncPhysicalTableWithDefinitions(Long boardMasterIdx) throws Exception {
        loggingUtil.logAttempt(Action.UPDATE, "스키마 동기화 시도: masterIdx=" + boardMasterIdx);
        try {
            boardMasterDao.syncPhysicalTableWithDefinitions(boardMasterIdx);
            loggingUtil.logSuccess(Action.UPDATE, "스키마 동기화 완료: masterIdx=" + boardMasterIdx);
        } catch (Exception e) {
            loggingUtil.logFail(Action.UPDATE, "스키마 동기화 실패: " + e.getMessage());
            throw processException("스키마 동기화 중 오류가 발생했습니다.", e);
        }
    }
}
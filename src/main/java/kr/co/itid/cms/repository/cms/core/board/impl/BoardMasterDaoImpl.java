package kr.co.itid.cms.repository.cms.core.board.impl;

import kr.co.itid.cms.dto.cms.core.board.request.BoardMasterRequest;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterResponse;
import kr.co.itid.cms.repository.cms.core.board.BoardMasterDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository("boardMasterDao")
@RequiredArgsConstructor
public class BoardMasterDaoImpl implements BoardMasterDao {
    @Override
    public BoardMasterResponse findBoardMasterByIdx(Long idx) {
        return null;
    }

    @Override
    public void insertBoardMaster(BoardMasterRequest request) {

    }

    @Override
    public void insertBoardFieldDefinitions(BoardMasterRequest request) {

    }

    @Override
    public void createBoardTable(String boardId) {

    }

    @Override
    public void updateBoardMaster(Long idx, BoardMasterRequest request) {

    }

    @Override
    public void updateBoardFieldDefinitions(Long boardMasterIdx, BoardMasterRequest request) {

    }

    @Override
    public void deleteBoardMaster(Long idx) {

    }

    @Override
    public void deleteBoardFieldDefinitions(Long boardMasterIdx) {

    }

    @Override
    public void dropBoardTable(String boardId) {

    }

    @Override
    public String findBoardIdByIdx(Long idx) {
        return "";
    }
}

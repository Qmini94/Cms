package kr.co.itid.cms.service.cms.core.board;

import kr.co.itid.cms.dto.cms.core.board.response.BoardFieldDefinitionResponse;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterListResponse;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterResponse;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.entity.cms.core.board.BoardMaster;
import kr.co.itid.cms.mapper.cms.core.board.BoardMasterMapper;
import kr.co.itid.cms.repository.cms.core.board.BoardMasterDao;
import kr.co.itid.cms.repository.cms.core.board.BoardMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 게시판 마스터 캐시 관리 서비스
 */
@Service
@RequiredArgsConstructor
public class BoardMasterCacheService {

    private final BoardMasterRepository boardMasterRepository;
    private final BoardMasterDao boardMasterDao;
    private final BoardMasterMapper boardMasterMapper;

    /**
     * 게시판 목록 조회 (캐시 적용)
     */
    @Cacheable(value = "boardMasters", key = "#option.toString() + '_' + #pageable.toString()")
    public Page<BoardMasterListResponse> searchBoardMasters(SearchOption option, Pageable pageable) {
        Page<BoardMaster> resultPage = boardMasterRepository.searchByCondition(option, pageable);
        return resultPage.map(boardMasterMapper::toListResponse);
    }

    /**
     * 게시판 단일 조회 (캐시 적용)
     */
    @Cacheable(value = "boardMaster", key = "#idx")
    public BoardMasterResponse getBoardByIdx(Long idx) {
        try {
            return boardMasterDao.findBoardMasterByIdx(idx);
        } catch (Exception e) {
            throw new RuntimeException("게시판 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 게시판 필드 정의 조회 (캐시 적용)
     */
    @Cacheable(value = "boardFieldDefinitions", key = "#boardMasterIdx")
    public List<BoardFieldDefinitionResponse> getFieldDefinitions(Long boardMasterIdx) {
        try {
            return boardMasterDao.selectFieldDefinitions(boardMasterIdx);
        } catch (Exception e) {
            throw new RuntimeException("필드 정의 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 활성 게시판 목록 조회 (캐시 적용)
     */
    @Cacheable(value = "activeBoardMasters", key = "'active'")
    public List<BoardMasterListResponse> getActiveBoardMasters() {
        // 활성 게시판만 조회하는 로직 (isUse = true)
        SearchOption activeOption = new SearchOption();
        // TODO: SearchOption에 isUse 필터 추가 필요시 구현
        Page<BoardMaster> resultPage = boardMasterRepository.searchByCondition(activeOption, Pageable.unpaged());
        return resultPage.getContent().stream()
                .filter(board -> Boolean.TRUE.equals(board.getIsUse()))
                .map(boardMasterMapper::toListResponse)
                .toList();
    }

    /**
     * 게시판 마스터 캐시 무효화 (전체)
     */
    @CacheEvict(value = {"boardMasters", "boardMaster", "boardFieldDefinitions", "activeBoardMasters"}, allEntries = true)
    public void evictAllBoardMasterCache() {
        // 캐시 무효화만 수행
    }

    /**
     * 필드 정의 캐시 무효화
     */
    @CacheEvict(value = "boardFieldDefinitions", key = "#boardMasterIdx")
    public void evictFieldDefinitionsCache(Long boardMasterIdx) {
        // 필드 정의 캐시 무효화
    }

    /**
     * 검색 결과 캐시 무효화
     */
    @CacheEvict(value = "boardMasters", allEntries = true)
    public void evictSearchCache() {
        // 검색 결과 캐시 무효화
    }

    /**
     * 활성 게시판 캐시 무효화
     */
    @CacheEvict(value = "activeBoardMasters", allEntries = true)
    public void evictActiveBoardMastersCache() {
        // 활성 게시판 캐시 무효화
    }
}

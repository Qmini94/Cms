package kr.co.itid.cms.service.cms.core.board.impl;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.dto.cms.core.board.response.FieldDefinitionResponse;
import kr.co.itid.cms.dto.cms.core.common.PaginationOption;
import kr.co.itid.cms.dto.cms.core.common.SearchOption;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.repository.cms.core.board.DynamicBoardDao;
import kr.co.itid.cms.service.cms.core.board.DynamicBoardService;
import kr.co.itid.cms.util.LoggingUtil;
import kr.co.itid.cms.util.MapKeyConverterUtil;
import kr.co.itid.cms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service("dynamicBoardService")
@RequiredArgsConstructor
public class DynamicBoardServiceImpl extends EgovAbstractServiceImpl implements DynamicBoardService {

    private final DynamicBoardDao dynamicBoardDao;
    private final LoggingUtil loggingUtil;

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public List<FieldDefinitionResponse> getFieldDefinitions() throws Exception {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        Long menuId = user.menuId();
        loggingUtil.logAttempt(Action.RETRIEVE, "[필드 정의 조회] menuId=" + menuId);
        try {
            List<FieldDefinitionResponse> fields = dynamicBoardDao.getFieldDefinitionsByMenuId(menuId);
            loggingUtil.logSuccess(Action.RETRIEVE, "[필드 정의 조회 성공] count=" + fields.size());
            return fields;
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "[필드 정의 조회 실패] menuId=" + menuId + " / " + e.getMessage());
            throw processException("게시판 필드 정의 조회 중 오류가 발생했습니다.");
        }
    }

    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public Page<Map<String, Object>> getList(SearchOption option, PaginationOption pagination) throws Exception {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        Long menuId = user.menuId();
        loggingUtil.logAttempt(Action.RETRIEVE, "[게시글 목록 조회] menuId=" + menuId);
        try {
            Page<Map<String, Object>> result = dynamicBoardDao.selectListByMenuId(menuId, option, pagination)
                    .map(MapKeyConverterUtil::convertKeysToCamelCase);

            loggingUtil.logSuccess(Action.RETRIEVE, "[게시글 목록 조회 성공] total=" + result.getTotalElements());
            return result;
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "[게시글 목록 조회 실패] menuId=" + menuId + " / " + e.getMessage());
            throw processException("게시글 목록 조회 중 오류가 발생했습니다.");
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class) // readOnly 제거 (UPDATE 필요)
    public Map<String, Object> getOne(Long idx) throws Exception {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        Long menuId = user.menuId();
        loggingUtil.logAttempt(Action.RETRIEVE, "[게시글 단건 조회] menuId=" + menuId + ", idx=" + idx);
        try {
            // 1) 조회수 +1
            dynamicBoardDao.increaseViewCountByMenuId(menuId, idx);

            // 2) 데이터 조회
            Map<String, Object> raw = dynamicBoardDao.selectOneByMenuId(menuId, idx);
            Map<String, Object> result = MapKeyConverterUtil.convertKeysToCamelCase(raw);

            loggingUtil.logSuccess(Action.RETRIEVE, "[게시글 조회 성공] idx=" + idx);
            return result;
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "[게시글 조회 실패] idx=" + idx + " / " + e.getMessage());
            throw processException("게시글 조회 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void save(Long idx, Map<String, Object> data) throws Exception {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        Long menuId = user.menuId();
        boolean isNew = (idx == null);
        Action action = isNew ? Action.CREATE : Action.UPDATE;

        loggingUtil.logAttempt(action, "[게시글 저장 시도] menuId=" + menuId + ", idx=" + idx + ", user=" + user.userId());

        try {
            if (isNew) {
                data.put("reg_id", user.userId());
                data.put("reg_name", user.userName());
                dynamicBoardDao.insertByMenuId(menuId, data);
                loggingUtil.logSuccess(action, "[게시글 등록 성공]");
            } else {
                data.put("mod_id", user.userId());
                data.put("mod_name", user.userName());
                dynamicBoardDao.updateByMenuId(menuId, idx, data);
                loggingUtil.logSuccess(action, "[게시글 수정 성공]");
            }
        } catch (Exception e) {
            loggingUtil.logFail(action, "[게시글 저장 실패] " + e.getMessage());
            throw processException("게시글 저장 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    @Transactional(rollbackFor = EgovBizException.class)
    public void delete(Long idx) throws Exception {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        Long menuId = user.menuId();
        loggingUtil.logAttempt(Action.DELETE, "[게시글 삭제 시도] menuId=" + menuId + ", idx=" + idx);
        try {
            dynamicBoardDao.deleteByMenuId(menuId, idx);
            loggingUtil.logSuccess(Action.DELETE, "[게시글 삭제 성공] idx=" + idx);
        } catch (Exception e) {
            loggingUtil.logFail(Action.DELETE, "[게시글 삭제 실패] idx=" + idx + " / " + e.getMessage());
            throw processException("게시글 삭제 중 오류가 발생했습니다.", e);
        }
    }
}
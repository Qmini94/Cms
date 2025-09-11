package kr.co.itid.cms.service.cms.core.render.impl;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.dto.auth.UserPermissionResponse;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterResponse;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuTypeValueResponse;
import kr.co.itid.cms.dto.cms.core.render.response.RenderResponse;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.service.auth.PermissionService;
import kr.co.itid.cms.service.cms.core.board.BoardMasterService;
import kr.co.itid.cms.service.cms.core.menu.MenuService;
import kr.co.itid.cms.service.cms.core.render.RenderService;
import kr.co.itid.cms.util.LoggingUtil;
import kr.co.itid.cms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.egovframe.rte.fdl.cmmn.exception.EgovBizException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RenderServiceImpl extends EgovAbstractServiceImpl implements RenderService {

    private final MenuService menuService;
    private final PermissionService permissionService;
    private final BoardMasterService boardMasterService;
    private final LoggingUtil loggingUtil;

    /* =========================================================
       권한/옵션 포함 렌더 데이터
     ========================================================= */
    @Override
    @Transactional(readOnly = true, rollbackFor = EgovBizException.class)
    public RenderResponse getRenderData() throws Exception {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        Long menuId = user.menuId();

        loggingUtil.logAttempt(Action.RETRIEVE, "render.data attempt: menuId=" + menuId);
        try {
            MenuTypeValueResponse menu = menuService.getMenuRenderById(menuId);
            if (menu.getType() == null || menu.getValue() == null) {
                throw processException("render.data.menu.invalid");
            }

            Object option;
            String value = menu.getValue();
            UserPermissionResponse perm = permissionService.getPermissionByMenu(user);

            switch (menu.getType()) {
                case "board": {
                    BoardMasterResponse board = boardMasterService.getBoardByIdx(Long.valueOf(value));
                    option = board;
                    break;
                }
                case "content":
                case "script":
                case "drive":
                case "folder":
                case "link":
                    option = null;
                    break;
                default:
                    throw processException("render.data.type.unsupported");
            }

            loggingUtil.logSuccess(Action.RETRIEVE, "render.data ok: type=" + menu.getType());
            return RenderResponse.builder()
                    .type(menu.getType())
                    .value(value)
                    .option(option)
                    .permission(perm)
                    .build();

        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "render.data db error: menuId=" + menuId);
            throw processException("render.data.db.error", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "render.data unexpected");
            throw processException("render.data.unexpected", e);
        }
    }
}
package kr.co.itid.cms.service.cms.core.render.impl;

import kr.co.itid.cms.config.security.model.JwtAuthenticatedUser;
import kr.co.itid.cms.dto.cms.core.board.response.BoardMasterResponse;
import kr.co.itid.cms.dto.cms.core.menu.response.MenuTypeValueResponse;
import kr.co.itid.cms.dto.cms.core.render.response.RenderResponse;
import kr.co.itid.cms.enums.Action;
import kr.co.itid.cms.service.cms.core.board.BoardMasterService;
import kr.co.itid.cms.service.cms.core.content.ContentService;
import kr.co.itid.cms.service.cms.core.menu.MenuService;
import kr.co.itid.cms.service.cms.core.render.RenderService;
import kr.co.itid.cms.util.LoggingUtil;
import kr.co.itid.cms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.egovframe.rte.fdl.cmmn.EgovAbstractServiceImpl;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RenderServiceImpl extends EgovAbstractServiceImpl implements RenderService {

    private final MenuService menuService;
    private final BoardMasterService boardMasterService;
    private final ContentService contentService;
    private final LoggingUtil loggingUtil;

    @Override
    @Transactional(readOnly = true)
    public RenderResponse getRenderData() throws Exception {
        JwtAuthenticatedUser user = SecurityUtil.getCurrentUser();
        Long menuId = user.menuId();

        loggingUtil.logAttempt(Action.RETRIEVE, "Try to render content for menuId: " + menuId);
        try {
            MenuTypeValueResponse menu = menuService.getMenuRenderById(menuId);

            if (menu.getType() == null || menu.getValue() == null) {
                throw processException("Menu type/value is not defined");
            }

            Object data;
            switch (menu.getType()) {
                case "module":
                    BoardMasterResponse boardMaster = boardMasterService.getBoardByBoardId(menu.getValue());
                    //TODO: 옵션처리에 맞게 DB에서 board_id? name에 해당하는 데이터 가져오기. 엔티티부터 컨트롤러까지 개발진행.
                    data = boardMaster;
                    break;
                case "content":
                    data = contentService.getContentByParentId(Long.parseLong(menu.getValue()));
                    break;
                default:
                    throw processException("Unsupported render type: " + menu.getType());
            }

            loggingUtil.logSuccess(Action.RETRIEVE, "Rendered content for type: " + menu.getType());
            return RenderResponse.builder()
                    .type(menu.getType())
                    .data(data)
                    .build();
        } catch (DataAccessException e) {
            loggingUtil.logFail(Action.RETRIEVE, "DB error during render for menuId: " + menuId);
            throw processException("Database error", e);
        } catch (Exception e) {
            loggingUtil.logFail(Action.RETRIEVE, "Unexpected error during render");
            throw processException("Unexpected error", e);
        }
    }
}
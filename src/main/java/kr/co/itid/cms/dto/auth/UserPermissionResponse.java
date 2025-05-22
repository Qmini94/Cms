package kr.co.itid.cms.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserPermissionResponse {
    private boolean view;
    private boolean write;
    private boolean modify;
    private boolean remove;
    private boolean manage;
    private boolean access;
    private boolean reply;
}

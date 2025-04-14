package kr.co.itid.cms.service.auth;

public interface PermissionResolverService {
    boolean resolvePermission(String userId, int menuId, String permission) throws Exception;
}

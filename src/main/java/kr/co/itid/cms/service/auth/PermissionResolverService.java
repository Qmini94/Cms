package kr.co.itid.cms.service.auth;

public interface PermissionResolverService {
    boolean resolvePermission(String userId, long menuId, String permission) throws Exception;
}

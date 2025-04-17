package kr.co.itid.cms.repository.cms.core;

import kr.co.itid.cms.entity.cms.core.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    @Query("SELECT p FROM Permission p WHERE p.menuId IN :menuIds")
    List<Permission> findAllByMenuIds(List<Integer> menuIds);
}

package kr.co.itid.cms.config.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class UserWithAdditionalInfo extends User {
    private String name;
    private String tel;
    private String deptId;

    public UserWithAdditionalInfo(String username, String password, boolean enabled,
                                  Collection<? extends GrantedAuthority> authorities,
                                  String name, String tel, String deptId) {
        super(username, password, enabled, true, true, true, authorities);
        this.name = name;
        this.tel = tel;
        this.deptId = deptId;
    }
}


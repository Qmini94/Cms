package kr.co.itid.cms.config.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class UserWithAdditionalInfo extends User {
    private String email;
    private String address;
    private String departmentId;

    public UserWithAdditionalInfo(String username, String password, boolean enabled,
                                  Collection<? extends GrantedAuthority> authorities,
                                  String email, String address, String departmentId) {
        super(username, password, enabled, true, true, true, authorities);
        this.email = email;
        this.address = address;
        this.departmentId = departmentId;
    }
}


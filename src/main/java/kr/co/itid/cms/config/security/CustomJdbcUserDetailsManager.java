package kr.co.itid.cms.config.security;

import org.egovframe.rte.fdl.security.config.SecurityConfig;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class CustomJdbcUserDetailsManager extends JdbcUserDetailsManager {

    private final SecurityConfig securityConfig;

    public CustomJdbcUserDetailsManager(DataSource dataSource, SecurityConfig securityConfig) {
        super(dataSource);
        this.securityConfig = securityConfig;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // SecurityConfig에서 쿼리문을 가져와 사용
        String jdbcUsersByUsernameQuery = securityConfig.getJdbcUsersByUsernameQuery();
        String jdbcAuthoritiesByUsernameQuery = securityConfig.getJdbcAuthoritiesByUsernameQuery();

        // 사용자 정보 로드 (커스텀 쿼리 사용)
        UserDetails userDetails = getJdbcTemplate().queryForObject(jdbcUsersByUsernameQuery, new Object[]{username},
                (rs, rowNum) -> new UserWithAdditionalInfo(
                        rs.getString("user_id"),
                        rs.getString("password"),
                        rs.getString("enabled").equals("Y"),
                        new ArrayList<>(), // 권한은 나중에 설정
                        rs.getString("user_name"),
                        rs.getString("tel"),
                        rs.getString("dept_id")
                )
        );

        int level = 9;

        try {
            level = getJdbcTemplate().queryForObject(jdbcAuthoritiesByUsernameQuery, Integer.class, username);
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found", e);
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // 권한 설정 (int 값을 기준으로)
        if (level == 1) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else if (level == 9) {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        } else if (level == 6) {
            authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
        }

        // 기존 userDetails에서 가져온 정보와 새로 설정한 권한을 이용하여 반환
        return new UserWithAdditionalInfo(
                userDetails.getUsername(),
                userDetails.getPassword(),
                userDetails.isEnabled(),
                authorities,
                ((UserWithAdditionalInfo) userDetails).getName(),
                ((UserWithAdditionalInfo) userDetails).getTel(),
                ((UserWithAdditionalInfo) userDetails).getDeptId()
        );
    }
}



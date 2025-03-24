package kr.co.itid.cms.config.security;

import org.egovframe.rte.fdl.security.config.SecurityConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

@Configuration
public class SpringSecurityConfig {

    private final SecurityConfig securityConfig;

    public SpringSecurityConfig(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .antMatchers("/login", "/logout").permitAll()  // 로그인, 로그아웃은 인증 없이 허용
                        .anyRequest().authenticated()  // 그 외 모든 요청은 인증 필요
                )
                .formLogin(login -> login
                        .loginPage(securityConfig.getLoginUrl())  // 로그인 페이지
                        .loginProcessingUrl("/login")  // 로그인 요청 URL
                        .defaultSuccessUrl(securityConfig.getDefaultTargetUrl(), true)  // 로그인 성공 후 이동할 페이지
                        .failureUrl(securityConfig.getLoginFailureUrl())  // 로그인 실패 시 이동할 페이지
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl(securityConfig.getLogoutSuccessUrl())  // 로그아웃 성공 시 이동할 페이지
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage(securityConfig.getAccessDeniedUrl())  // 권한 거부 시 이동할 페이지
                )
                .sessionManagement(session -> session
                        .maximumSessions(securityConfig.getConcurrentMaxSessons()) // 최대 세션 수
                        .expiredUrl(securityConfig.getConcurrentExpiredUrl()) // 세션 만료 시 이동할 URL
                );

        return http.build();
    }

    @Bean
    public CustomJdbcUserDetailsManager customJdbcUserDetailsManager(DataSource dataSource, SecurityConfig securityConfig) {
        return new CustomJdbcUserDetailsManager(dataSource, securityConfig);
    }

    // 평문 비밀번호 사용 (개발 환경에서만 사용)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();  // 평문 비밀번호 비교
    }

    // 실제 배포 환경에서는 BCryptPasswordEncoder 사용 권장
    // @Bean
    // public PasswordEncoder passwordEncoder() {
    //     return new BCryptPasswordEncoder(); // 안전한 비밀번호 해싱
    // }
}

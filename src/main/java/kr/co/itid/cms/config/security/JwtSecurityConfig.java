package kr.co.itid.cms.config.security;

import kr.co.itid.cms.config.security.port.SiteAccessChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
@RequiredArgsConstructor
public class JwtSecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final SiteAccessChecker siteAccessChecker;
    private final XssProtectionFilter xssProtectionFilter; // XSS 방어 필터 추가

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, siteAccessChecker);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // CSRF 보호 활성화 - API 기반이므로 토큰 방식 사용
                .csrf(csrf -> csrf
                        .csrfTokenRepository(org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(request -> 
                            request.getRequestURI().equals("/back-api/auth/login")) // 로그인은 CSRF 예외
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 보안 헤더 설정 추가
                .headers(headers -> headers
                        .frameOptions().deny() // X-Frame-Options: DENY
                        .contentTypeOptions().and() // X-Content-Type-Options: nosniff
                        .addHeaderWriter((request, response) -> {
                            // XSS 보호 헤더 추가
                            response.setHeader("X-XSS-Protection", "1; mode=block");
                            // HSTS 헤더 추가
                            response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                            // CSP 헤더 추가 (기본적인 정책)
                            response.setHeader("Content-Security-Policy", 
                                "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:;");
                        })
                )
                // 인증/인가 permitAll로 넘긴 후 filter와 interceptor에서 처리.
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                // XSS 방어 필터를 JWT 인증 필터 앞에 추가
                .addFilterBefore(xssProtectionFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",     // 개발용 프론트 주소
                "https://localhost:3000",     // 개발용 프론트 주소
                "https://cms.itid.co.kr"     // 운영용 프론트 주소
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

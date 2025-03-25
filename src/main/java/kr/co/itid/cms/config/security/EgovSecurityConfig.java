package kr.co.itid.cms.config.security;

import org.egovframe.rte.fdl.security.config.SecurityConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class EgovSecurityConfig {
    // 전자정부 시큐리티 설정
    @Bean
    public SecurityConfig securityConfig(DataSource dataSource) {
        SecurityConfig securityConfig = new SecurityConfig();

        // 필수 URL 설정
        securityConfig.setLoginUrl("/login");  // 로그인 페이지
        securityConfig.setLogoutSuccessUrl("/www/index");  // 로그아웃 성공 시 URL
        securityConfig.setLoginFailureUrl("/login?fail=true");  // 로그인 실패 시 URL
        securityConfig.setAccessDeniedUrl("/denied");  // 권한 거부 시 URL

        // 로그인 성공 후 이동할 URL 설정 (미지정시 기본 값은 처음 접속하고자 했던 페이지)
        securityConfig.setDefaultTargetUrl("/www/index");

        // DBMS 설정
        securityConfig.setDataSource(dataSource);

        // 인증에 사용되는 쿼리 (사용자 정보)
        securityConfig.setJdbcUsersByUsernameQuery(
                "SELECT user_id, user_password AS password, 'Y' AS enabled, user_name, tel, dept_id FROM egov_member WHERE user_id = ?"
        );

        // 인증된 사용자의 권한 조회 쿼리
        securityConfig.setJdbcAuthoritiesByUsernameQuery(
                "SELECT user_level FROM egov_member WHERE user_id = ? "
        );

        // 사용자 정보 mapping 처리 class 설정 (선택 사항)
        securityConfig.setJdbcMapClass("egovframework.rte.fdl.security.userdetails.DefaultMapUserDetailsMapping");

        // 요청 매칭 방식 (패턴 매칭 방식 선택)
        securityConfig.setRequestMatcherType("regex");

        // 비밀번호 해싱 방식 설정 (예: sha-256)
        securityConfig.setHash("plaintext");

        // 해시값 base64 인코딩 여부 (기본값: true)
        securityConfig.setHashBase64(true);

        // 동시 접속 최대 세션 수 설정 (기본값: 999)
        securityConfig.setConcurrentMaxSessons(999);

        // 동시 접속 제한 시 Expired URL 설정
        securityConfig.setConcurrentExpiredUrl("/logout");

        // 중복 로그인 방지 옵션 (기본값: false)
        securityConfig.setErrorIfMaximumExceeded(false);

        // 로그인 이후 설정된 페이지로 이동할지 여부
        securityConfig.setAlwaysUseDefaultTargetUrl(true);

        // MIME 가로채기 방지 여부
        securityConfig.setSniff(true);

        // X-Frame-Options 설정 (DENY 또는 SAMEORIGIN)
        securityConfig.setXframeOptions("SAMEORIGIN");

        // XSS Protection 활성화 (기본값: true)
        securityConfig.setXssProtection(true);

        // 캐시 비활성화 여부 (기본값: false)
        securityConfig.setCacheControl(false);

        // CSRF 기능 사용 여부 (기본값: false)
        securityConfig.setCsrf(false);

        // CSRF 실패 시 호출되는 URL 설정
        securityConfig.setCsrfAccessDeniedUrl("/login?csrfError=true");

        // Spring 표현 언어(SpEL) 설정 여부
        securityConfig.setUseExpressions(false);

        return securityConfig;
    }
}

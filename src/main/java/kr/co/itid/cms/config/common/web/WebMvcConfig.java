package kr.co.itid.cms.config.common.web;

import kr.co.itid.cms.config.common.interceptor.AuthInterceptor;
import kr.co.itid.cms.config.common.interceptor.VisitorInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final VisitorInterceptor visitorInterceptor;
    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(visitorInterceptor)
                .addPathPatterns("/api/auth/me");

        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/auth/me");
    }
}

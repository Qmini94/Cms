package kr.co.itid.cms.config.common.swagger;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.service.*;
import springfox.documentation.spi.service.contexts.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())  // 모든 패키지 스캔
                .paths(PathSelectors.any())           // 모든 경로 스캔
                .build()
                .securityContexts(List.of(securityContext()))  // 보안 설정 추가
                .securitySchemes(List.of(apiKey()));           // API 키 설정 추가
    }

    // API 키를 통해 JWT를 설정하는 부분
    private ApiKey apiKey() {
        return new ApiKey("JWT", "Authorization", "header");
    }

    // 보안 컨텍스트 설정
    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .build();
    }

    // 기본 권한 설정
    private List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[]{authorizationScope};
        return List.of(new SecurityReference("JWT", authorizationScopes));
    }
}

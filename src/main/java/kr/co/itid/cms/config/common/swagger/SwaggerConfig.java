package kr.co.itid.cms.config.common.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String X_SITE_HOSTNAME = "X-Site-Hostname";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API 문서")
                        .version("1.0.0")
                        .description("X-Site-Hostname 헤더 포함 설정"))
                .addSecurityItem(new SecurityRequirement().addList(X_SITE_HOSTNAME))
                .components(new Components()
                        .addSecuritySchemes(X_SITE_HOSTNAME,
                                new SecurityScheme()
                                        .name(X_SITE_HOSTNAME)
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .description("요청 영역 구분자 (예: www, admin 등)")));
    }
}

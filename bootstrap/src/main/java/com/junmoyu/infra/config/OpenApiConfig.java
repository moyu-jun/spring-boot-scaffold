package com.junmoyu.infra.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档配置
 */
@Configuration
public class OpenApiConfig {

    @Value("$spring.application.name:default")
    private String appName;

    @Value("$spring.application.version:1.0.0")
    private String appVersion;

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info().title(appName)
                        .description(appName + "的接口文档")
                        .version(appVersion))
                .externalDocs(new ExternalDocumentation()
                        .description("springdoc-openapi Documentation")
                        .url("https://springdoc.org"));
    }
}

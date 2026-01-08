package com.zzy.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API 文档配置
 */
@Configuration
public class Knife4jConfiguration {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("在线课堂系统API")
                        .version("1.0.0")
                        .description("在线课堂系统后端 API 接口文档"));
    }

    @Bean
    public GroupedOpenApi studentAPI() {
        return GroupedOpenApi.builder()
                .group("学生端")
                .pathsToMatch(
                        "/teacher/**",
                        "/appointment/**",
                        "/auth/**",
                        "/chat/**",
                        "/notification/**",
                        "/payment/**",
                        "/user/**"
                )
                .build();
    }

    @Bean
    public GroupedOpenApi commonAPI() {
        return GroupedOpenApi.builder()
                .group("通用接口")
                .pathsToMatch("/common/**")
                .build();
    }

    @Bean
    public GroupedOpenApi teacherAPI() {
        return GroupedOpenApi.builder()
                .group("教师端")
                .pathsToMatch("/teacher-portal/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminAPI() {
        return GroupedOpenApi.builder()
                .group("管理端")
                .pathsToMatch("/admin/**")
                .build();
    }
}

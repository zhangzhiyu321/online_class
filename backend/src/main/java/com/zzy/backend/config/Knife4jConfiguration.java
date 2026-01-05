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
        return new OpenAPI().info(
                new Info()
                        .title("在线课堂系统API")
                        .version("1.0")
                        .description("在线课堂系统后端 API 接口文档"));
    }

    // ==================== 学生端功能 ====================
    
    @Bean
    public GroupedOpenApi studentAPI() {
        return GroupedOpenApi.builder().group("学生端").
                pathsToMatch(
                        "/teacher/**",              // 教师管理
                        "/student/appointment/**",  // 预约管理
                        "/student/auth/**",         // 认证登录
                        "/student/call/**",         // 视频通话
                        "/student/chat/**",         // 聊天消息
                        "/student/notification/**",  // 通知公告
                        "/student/payment/**",       // 支付管理
                        "/student/profile/**",       // 个人中心
                        "/student/refund/**",        // 退款管理
                        "/student/review/**"         // 评价管理
                ).
                build();
    }

    // ==================== 教师端功能 ====================
    
    @Bean
    public GroupedOpenApi teacherAPI() {
        return GroupedOpenApi.builder().group("教师端").
                pathsToMatch(
                        "/teacher-portal/**"
                ).
                build();
    }

    // ==================== 后台端功能 ====================
    
    @Bean
    public GroupedOpenApi adminAPI() {
        return GroupedOpenApi.builder().group("管理端").
                pathsToMatch(
                        "/admin/**"
                ).
                build();
    }
}


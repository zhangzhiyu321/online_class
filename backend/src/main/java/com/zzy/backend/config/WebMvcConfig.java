package com.zzy.backend.config;

import com.zzy.backend.interceptor.AuthInterceptor;
import com.zzy.backend.interceptor.LogInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;
    
    @Autowired
    private LogInterceptor logInterceptor;

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 日志拦截器 - 最先执行
        registry.addInterceptor(logInterceptor)
                .addPathPatterns("/**")
                .order(1);
        
        // 认证拦截器
        // 注意：由于设置了 context-path=/api，拦截器的路径匹配是相对于 context-path 的
        // 所以不需要在路径前加 /api 前缀
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/**",               // 认证相关接口（实际路径：/api/auth/**）
                        "/common/**",             // 公共接口（实际路径：/api/common/**）
                        "/doc.html",              // Knife4j 文档
                        "/webjars/**",            // Knife4j 静态资源
                        "/v3/api-docs/**",        // OpenAPI 文档
                        "/actuator/**",           // Actuator 监控
                        "/error",                 // 错误页面
                        "/favicon.ico"            // 图标
                )
                .order(2);
    }

    /**
     * 静态资源处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 文件上传路径映射
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");
    }
}


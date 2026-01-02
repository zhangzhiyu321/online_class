package com.zzy.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    @Value("${cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials}")
    private Boolean allowCredentials;

    @Value("${cors.max-age}")
    private Long maxAge;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的源
        String[] origins = allowedOrigins.split(",");
        for (String origin : origins) {
            config.addAllowedOriginPattern(origin.trim());
        }
        
        // 允许的请求方法
        String[] methods = allowedMethods.split(",");
        for (String method : methods) {
            config.addAllowedMethod(method.trim());
        }
        
        // 允许的请求头
        if ("*".equals(allowedHeaders)) {
            config.addAllowedHeader("*");
        } else {
            String[] headers = allowedHeaders.split(",");
            for (String header : headers) {
                config.addAllowedHeader(header.trim());
            }
        }
        
        // 允许携带凭证
        config.setAllowCredentials(allowCredentials);
        
        // 预检请求的有效期
        config.setMaxAge(maxAge);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}


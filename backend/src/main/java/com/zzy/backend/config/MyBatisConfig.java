package com.zzy.backend.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 配置类
 */
@Configuration
@MapperScan("com.zzy.backend.mapper")
public class MyBatisConfig {
    // MyBatis 配置已在 application.properties 中完成
}


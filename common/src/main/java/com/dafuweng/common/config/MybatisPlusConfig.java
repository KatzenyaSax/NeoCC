package com.dafuweng.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 全局配置
 *
 * 注册自动填充处理器，所有使用 MyBatis-Plus 的模块都会自动应用。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public AutoFillMetaObjectHandler autoFillMetaObjectHandler() {
        return new AutoFillMetaObjectHandler();
    }
}

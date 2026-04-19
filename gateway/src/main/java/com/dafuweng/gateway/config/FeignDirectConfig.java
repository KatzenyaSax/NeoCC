package com.dafuweng.gateway.config;

import feign.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 强制 Feign 使用直接 HTTP 调用，绕过 Spring Cloud LoadBalancer。
 * 否则 @FeignClient 的 url="http://localhost:8085" 仍被 LoadBalancer 拦截，
 * 导致 Feign 尝试从 Nacas 解析 "auth" 服务名而失败（503）。
 *
 * 原理：注入 @Primary 的 Client bean，覆盖 FeignAutoConfiguration 中的默认 LoadBalancerClient。
 */
@Configuration
public class FeignDirectConfig {

    @Bean
    @Primary
    public Client feignClient() {
        // 使用 JDK 内置 HttpURLConnection 的直接 HTTP 调用，无 LoadBalancer 拦截
        return new Client.Default(null, null);
    }
}

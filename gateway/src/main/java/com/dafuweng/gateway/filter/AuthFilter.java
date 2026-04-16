package com.dafuweng.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Gateway 全局过滤器
 * 
 * 功能：
 * - 传递 Authorization 头到下游服务
 * - 添加请求标识头
 * - JWT 验证由 Auth 服务负责
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final String AUTH_HEADER = "Authorization";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 添加请求ID用于追踪
        String requestId = java.util.UUID.randomUUID().toString();
        
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(REQUEST_ID_HEADER, requestId)
                .build();

        // 如果存在 Authorization 头，提取 userId 并传递给下游
        String authHeader = request.getHeaders().getFirst(AUTH_HEADER);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            // JWT Token 由 Auth 服务验证，这里只传递
            // 如果 token 是简单的 userId（兼容旧版），则添加到 header
            try {
                Long userId = Long.parseLong(token);
                mutatedRequest = mutatedRequest.mutate()
                        .header(USER_ID_HEADER, String.valueOf(userId))
                        .build();
            } catch (NumberFormatException e) {
                // JWT Token，由 Auth 服务验证
            }
        }

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -100;  // 优先执行
    }
}

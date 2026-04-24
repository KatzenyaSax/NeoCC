package com.dafuweng.gateway.filter;

import com.dafuweng.common.entity.Result;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthFilter implements GlobalFilter, Ordered {
    
    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);

    private static final String AUTH_SERVICE = "http://neocc-auth:8085";
    private static final String AUTH_HEADER = "Authorization";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLES_HEADER = "X-User-Roles";

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthFilter(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl(AUTH_SERVICE)
                .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String path = request.getURI().getPath();
        // 跳过登录/公开路径（doc04 修正后路径）
        if (path.equals("/auth/login") || path.startsWith("/auth/login") ||
            path.equals("/auth/getInfo") || path.startsWith("/auth/getInfo") ||
            path.equals("/auth/getRouters") || path.startsWith("/auth/getRouters") ||
            path.equals("/auth/logout") || path.startsWith("/auth/logout") ||
            path.contains("/auth/api/sysUser/login") || path.contains("/auth/api/sysUser/page")) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("AuthFilter: No auth header for path={}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        log.info("AuthFilter: path={}, token={}", path, token);

        Long userId;
        try {
            userId = Long.parseLong(token);
        } catch (NumberFormatException e) {
            log.warn("AuthFilter: Invalid token format: {}", token);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 调用 auth 服务验证用户（WebClient，直接 HTTP，无 LoadBalancer）
        log.info("AuthFilter: Calling auth service for userId={}", userId);
        return webClient.get()
                .uri("/api/sysUser/{id}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Result.class)
                .flatMap(userResult -> {
                    log.info("AuthFilter: auth service response: code={}, data={}", 
                        userResult != null ? userResult.getCode() : "null",
                        userResult != null && userResult.getData() != null ? "exists" : "null");
                    if (userResult == null || userResult.getCode() != 200 || userResult.getData() == null) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                    // 查询用户角色
                    return webClient.get()
                            .uri("/api/sysUser/{id}/roles", userId)
                            .accept(MediaType.APPLICATION_JSON)
                            .retrieve()
                            .bodyToMono(Result.class)
                            .flatMap(rolesResult -> {
                                List<Long> roleIds = null;
                                if (rolesResult != null && rolesResult.getCode() == 200 && rolesResult.getData() != null) {
                                    roleIds = objectMapper.convertValue(rolesResult.getData(), new TypeReference<List<Long>>() {});
                                }

                                final Long finalUserId = userId;
                                final List<Long> finalRoleIds = roleIds;
                                ServerHttpRequest mutatedRequest = request.mutate()
                                        .header(USER_ID_HEADER, String.valueOf(finalUserId))
                                        .build();

                                if (finalRoleIds != null && !finalRoleIds.isEmpty()) {
                                    mutatedRequest = mutatedRequest.mutate()
                                            .header(USER_ROLES_HEADER, String.join(",",
                                                    finalRoleIds.stream().map(String::valueOf).toArray(String[] ::new)))
                                            .build();
                                }

                                return chain.filter(exchange.mutate().request(mutatedRequest).build());
                            })
                            .onErrorResume(e -> {
                                // 角色查询失败，继续（角色非必须）
                                ServerHttpRequest mutatedRequest = request.mutate()
                                        .header(USER_ID_HEADER, String.valueOf(userId))
                                        .build();
                                return chain.filter(exchange.mutate().request(mutatedRequest).build());
                            });
                })
                .onErrorResume(WebClientResponseException.class, e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                    return exchange.getResponse().setComplete();
                })
                .onErrorResume(Exception.class, e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                    return exchange.getResponse().setComplete();
                });
    }

    @Override
    public int getOrder() {
        return -100;
    }
}

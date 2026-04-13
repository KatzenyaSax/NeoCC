package com.dafuweng.gateway.filter;

import com.dafuweng.common.entity.Result;
import com.dafuweng.gateway.feign.AuthFeignClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private ObjectProvider<AuthFeignClient> authFeignClientProvider;

    @Autowired
    @Lazy
    private AuthFeignClient authFeignClient;

    private static final String AUTH_HEADER = "Authorization";
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLES_HEADER = "X-User-Roles";

    private AuthFeignClient getAuthFeignClient() {
        try {
            return authFeignClientProvider.getObject();
        } catch (Exception e) {
            return authFeignClient;
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 跳过登录/公开路径
        String path = request.getURI().getPath();
        if (path.contains("/auth/api/sysUser/login") || path.contains("/auth/api/sysUser/page")) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);  // 去掉 "Bearer " 前缀

        // 当前阶段 token = userId 的字符串形式
        Long userId;
        try {
            userId = Long.parseLong(token);
        } catch (NumberFormatException e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 调用 auth 服务验证用户存在且有效
        try {
            Result<?> userResult = getAuthFeignClient().getUserById(userId);
            if (userResult == null || userResult.getCode() != 200 || userResult.getData() == null) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            return exchange.getResponse().setComplete();
        }

        // 查询用户角色
        List<Long> roleIds = null;
        try {
            Result<?> rolesResult = getAuthFeignClient().getRoleIds(userId);
            if (rolesResult != null && rolesResult.getCode() == 200 && rolesResult.getData() != null) {
                roleIds = new ObjectMapper().convertValue(rolesResult.getData(), new TypeReference<List<Long>>() {});
            }
        } catch (Exception ignored) {
        }

        // 将用户信息传递给下游服务
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
    }

    @Override
    public int getOrder() {
        return -100;  // 优先执行
    }
}

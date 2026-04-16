package com.dafuweng.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * XSS防护过滤器
 */
@Component
public class XssFilter implements GlobalFilter, Ordered {
    
    private static final Logger log = LoggerFactory.getLogger(XssFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 只处理JSON请求
        List<String> contentTypeHeaders = request.getHeaders().get(HttpHeaders.CONTENT_TYPE);
        if (contentTypeHeaders != null && contentTypeHeaders.stream()
                .anyMatch(ct -> ct.contains(MediaType.APPLICATION_JSON_VALUE))) {
            
            // 包装请求，过滤XSS
            ServerHttpRequestDecorator decoratedRequest = new ServerHttpRequestDecorator(request) {
                @Override
                public Flux<DataBuffer> getBody() {
                    return super.getBody()
                        .map(dataBuffer -> {
                            String body = dataBuffer.toString(StandardCharsets.UTF_8);
                            
                            // 检查是否包含XSS
                            if (XssUtils.containsXss(body)) {
                                log.warn("检测到XSS攻击尝试，IP: {}, URI: {}", 
                                    request.getRemoteAddress(), request.getURI());
                                // 清理XSS
                                body = XssUtils.cleanXss(body);
                            }
                            
                            byte[] cleanedBytes = body.getBytes(StandardCharsets.UTF_8);
                            return exchange.getResponse().bufferFactory().wrap(cleanedBytes);
                        });
                }
            };
            
            return chain.filter(exchange.mutate().request(decoratedRequest).build());
        }
        
        return chain.filter(exchange);
    }
    
    @Override
    public int getOrder() {
        return -100; // 在AuthFilter之前执行
    }
}

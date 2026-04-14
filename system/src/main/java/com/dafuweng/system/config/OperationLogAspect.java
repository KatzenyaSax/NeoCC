package com.dafuweng.system.config;

import com.dafuweng.system.entity.SysOperationLogEntity;
import com.dafuweng.system.service.SysOperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.concurrent.CompletableFuture;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class OperationLogAspect {

    @Autowired
    private SysOperationLogService sysOperationLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Pointcut("@annotation(operationLog)")
    public void operationLogPointcut(OperationLog operationLog) {}

    @Around("operationLogPointcut(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long cost = System.currentTimeMillis() - start;

        final String username = getCurrentUsername();
        final String module = operationLog.module();
        final String action = operationLog.action();
        final String methodName = joinPoint.getSignature().getName();
        final String params = getParamsJson(joinPoint);

        CompletableFuture.runAsync(() -> {
            SysOperationLogEntity logEntity = new SysOperationLogEntity();
            logEntity.setUsername(username);
            logEntity.setModule(module);
            logEntity.setAction(action);
            logEntity.setRequestMethod(methodName);
            logEntity.setRequestParams(params);
            logEntity.setCostTimeMs(cost);
            logEntity.setCreatedAt(new Date());
            sysOperationLogService.save(logEntity);
        });

        return result;
    }

    private String getCurrentUsername() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    return authHeader.substring(7);
                }
            }
        } catch (Exception ignored) {}
        return "unknown";
    }

    private String getParamsJson(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return "";
            }
            return objectMapper.writeValueAsString(args);
        } catch (Exception e) {
            return "[]";
        }
    }
}
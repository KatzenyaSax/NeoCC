package com.dafuweng.common.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据权限 AOP 切面
 *
 * 在每个 Controller 方法执行前，从 Spring Security SecurityContext 提取当前用户
 * 的数据权限信息，存入 DataScopeContext（ThreadLocal）。
 *
 * DAO 层 XML 中使用 ${_dataScope.toSqlCondition("alias")} 引用，
 * 由 MyBatis OGNL 调用 ThreadLocal 中的安全数据范围条件。
 *
 * 此切面使用反射提取 SysUserEntity 的字段，避免 common 对 auth 模块的直接依赖。
 */
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE)
public class DataScopeAspect {

    @Around("execution(* com.dafuweng..*Controller.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Map<String, Object> userData = extractUserData();
            DataScopeContext.set(userData);
            return joinPoint.proceed();
        } finally {
            DataScopeContext.clear();
        }
    }

    private Map<String, Object> extractUserData() {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", null);
        data.put("dataScope", (short) 4);
        data.put("deptId", null);
        data.put("zoneId", null);
        data.put("roleCodes", new ArrayList<>());

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return data;
            }

            Object principal = auth.getPrincipal();
            if (principal == null) {
                return data;
            }

            // 通过反射提取 SysUserEntity 字段，避免 common → auth 的直接依赖
            data.put("userId", getFieldValue(principal, "id"));
            data.put("dataScope", getFieldValue(principal, "dataScope"));
            data.put("deptId", getFieldValue(principal, "deptId"));
            data.put("zoneId", getFieldValue(principal, "zoneId"));

            // 提取角色编码列表（从 GrantedAuthority 中提取，去掉 "ROLE_" 前缀）
            List<String> roleCodes = new ArrayList<>();
            Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
            if (authorities != null) {
                for (GrantedAuthority authority : authorities) {
                    String authStr = authority.getAuthority();
                    if (authStr != null && authStr.startsWith("ROLE_")) {
                        // 去掉 "ROLE_" 前缀，得到原始角色编码，如 "SUPER_ADMIN"
                        roleCodes.add(authStr.substring(5));
                    }
                }
            }
            data.put("roleCodes", roleCodes);

        } catch (Exception ignored) {
            // 无用户上下文时返回默认值（dataScope=4，即全部）
        }
        return data;
    }

    private Object getFieldValue(Object target, String fieldName) {
        try {
            java.lang.reflect.Field field = findField(target.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(target);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }
}

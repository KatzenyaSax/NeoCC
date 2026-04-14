package com.dafuweng.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;

/**
 * 自动填充拦截器
 *
 * 在 insert 时自动填充: createdAt, updatedAt, createdBy, updatedBy
 * 在 update 时自动填充: updatedAt, updatedBy
 *
 * 使用 SecurityContextHolder 获取当前用户 ID（通过反射访问 SysUserEntity 字段），
 * 避免 common 模块对 auth.SysUserEntity 的直接依赖。
 *
 * 注意: getCurrentUserId() 返回 null 是安全的。
 * strictInsertFill / strictUpdateFill 在值为 null 时跳过填充，不会报错。
 * 这确保了定时任务、系统内部调用等无用户上下文的场景不会报错。
 */
public class AutoFillMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", Date.class, new Date());
        this.strictInsertFill(metaObject, "updatedAt", Date.class, new Date());

        Long userId = getCurrentUserId();
        if (userId != null) {
            this.strictInsertFill(metaObject, "createdBy", Long.class, userId);
            this.strictInsertFill(metaObject, "updatedBy", Long.class, userId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", Date.class, new Date());

        Long userId = getCurrentUserId();
        if (userId != null) {
            this.strictUpdateFill(metaObject, "updatedBy", Long.class, userId);
        }
    }

    /**
     * 从 Spring Security SecurityContext 获取当前用户 ID
     *
     * 通过反射访问 SysUserEntity.id 字段，避免直接依赖 auth 模块。
     * 无登录上下文（定时任务、测试、Redis 未命中等）时返回 null。
     */
    private Long getCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return null;
            }
            Object principal = auth.getPrincipal();
            if (principal == null) {
                return null;
            }
            // 通过反射获取 id 字段
            return (Long) getFieldValue(principal, "id");
        } catch (Exception e) {
            // 无登录上下文（如定时任务、测试环境、缓存未命中导致匿名用户）
            return null;
        }
    }

    private Object getFieldValue(Object target, String fieldName) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (IllegalAccessException e) {
                return null;
            }
        }
        return null;
    }
}

package com.dafuweng.common.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 数据权限上下文
 *
 * ThreadLocal 存储当前请求的数据权限范围，供 DAO 层 XML 中的 ${_dataScope} 引用。
 * 使用 ${_dataScope.toSqlCondition("alias")} 而非直接拼接 SQL，防止注入攻击。
 *
 * 使用 Map 存储用户属性，避免 common 模块对 auth.SysUserEntity 的直接依赖。
 */
public class DataScopeContext {

    private static final ThreadLocal<Map<String, Object>> CONTEXT = new ThreadLocal<>();

    public static void set(Map<String, Object> userData) {
        CONTEXT.set(userData);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * 从 Spring Security SecurityContext 提取当前用户数据权限信息
     *
     * 调用方（通常是 system 模块的 AOP 拦截器）负责将 SysUserEntity
     * 的相关字段提取为 Map 存入 ThreadLocal。
     * 此方法仅负责从 SecurityContextHolder 获取认证对象。
     */
    public static Authentication getAuthentication() {
        try {
            return SecurityContextHolder.getContext().getAuthentication();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从 ThreadLocal 获取当前用户的 userId
     */
    public static Long getUserId() {
        Map<String, Object> data = get();
        if (data == null) return null;
        Object userId = data.get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        return null;
    }

    /**
     * 从 ThreadLocal 获取当前用户的角色编码列表
     * @return 角色编码列表，如 ["SUPER_ADMIN", "ZONE_DIRECTOR"]
     */
    @SuppressWarnings("unchecked")
    public static List<String> getRoleCodes() {
        Map<String, Object> data = get();
        if (data == null) return Collections.emptyList();
        Object roleCodes = data.get("roleCodes");
        if (roleCodes instanceof List) {
            return (List<String>) roleCodes;
        }
        return Collections.emptyList();
    }

    /**
     * 判断当前用户是否拥有指定角色
     * @param roleCode 角色编码，如 "SUPER_ADMIN"
     */
    public static boolean hasRole(String roleCode) {
        return getRoleCodes().contains(roleCode);
    }

    /**
     * 从 ThreadLocal 获取当前用户的数据权限等级
     * 1=本人, 2=本部门, 3=本战区, 4=全部, null=全部（安全默认值）
     */
    public static Short getDataScopeLevel() {
        Map<String, Object> data = get();
        if (data == null) return 4;
        Object level = data.get("dataScope");
        if (level instanceof Number) {
            return ((Number) level).shortValue();
        }
        return 4;
    }

    /**
     * 从 ThreadLocal 获取当前用户的 deptId
     */
    public static Long getDeptId() {
        Map<String, Object> data = get();
        if (data == null) return null;
        Object deptId = data.get("deptId");
        if (deptId instanceof Number) {
            return ((Number) deptId).longValue();
        }
        return null;
    }

    /**
     * 从 ThreadLocal 获取当前用户的 zoneId
     */
    public static Long getZoneId() {
        Map<String, Object> data = get();
        if (data == null) return null;
        Object zoneId = data.get("zoneId");
        if (zoneId instanceof Number) {
            return ((Number) zoneId).longValue();
        }
        return null;
    }

    /**
     * 生成 MyBatis XML 中使用的 SQL 片段
     * 示例返回: "AND t.created_by = 123" 或 "AND t.dept_id = 456" 或 ""
     */
    public static String toSqlCondition(String tableAlias) {
        Short level = getDataScopeLevel();
        if (level == null || level == 4) {
            return "";  // 全部，不过滤
        }
        String alias = (tableAlias != null && !tableAlias.isEmpty()) ? tableAlias + "." : "";

        switch (level) {
            case 1: {
                Long userId = getUserId();
                if (userId != null) {
                    return " AND " + alias + "created_by = " + userId;
                }
                break;
            }
            case 2: {
                Long deptId = getDeptId();
                if (deptId != null) {
                    return " AND " + alias + "dept_id = " + deptId;
                }
                break;
            }
            case 3: {
                Long zoneId = getZoneId();
                if (zoneId != null) {
                    return " AND " + alias + "zone_id = " + zoneId;
                }
                break;
            }
            default:
                break;
        }
        return "";
    }
}

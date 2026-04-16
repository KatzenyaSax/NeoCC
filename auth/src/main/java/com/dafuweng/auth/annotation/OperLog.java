package com.dafuweng.auth.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 使用方式：@OperLog(title = "用户管理", businessType = BusinessType.INSERT)
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {
    
    /**
     * 模块标题
     */
    String title() default "";
    
    /**
     * 业务类型
     * 0其它 1新增 2修改 3删除 4查询 5导入 6导出
     */
    int businessType() default 0;
}

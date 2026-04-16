package com.dafuweng.auth.aspect;

import com.dafuweng.auth.annotation.OperLog;
import com.dafuweng.auth.entity.SysOperLogEntity;
import com.dafuweng.auth.mapper.SysOperLogMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志AOP切面
 */
@Aspect
@Component
public class OperLogAspect {
    
    private static final Logger log = LoggerFactory.getLogger(OperLogAspect.class);
    
    @Autowired
    private SysOperLogMapper operLogMapper;
    
    /**
     * 定义切点：拦截所有@OperLog注解的方法
     */
    @Pointcut("@annotation(com.dafuweng.auth.annotation.OperLog)")
    public void operLogPointcut() {
    }
    
    /**
     * 正常返回时记录日志
     */
    @AfterReturning(pointcut = "operLogPointcut()", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        saveLog(joinPoint, result, 0, null);
    }
    
    /**
     * 异常时记录日志
     */
    @AfterThrowing(pointcut = "operLogPointcut()", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Exception e) {
        saveLog(joinPoint, null, 1, e.getMessage());
    }
    
    /**
     * 保存日志
     */
    private void saveLog(JoinPoint joinPoint, Object result, int status, String errorMsg) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            OperLog operLog = method.getAnnotation(OperLog.class);
            
            SysOperLogEntity logEntity = new SysOperLogEntity();
            logEntity.setTitle(operLog.title());
            logEntity.setBusinessType(operLog.businessType());
            logEntity.setMethod(joinPoint.getTarget().getClass().getName() + "." + method.getName());
            logEntity.setRequestMethod(signature.getMethod().getAnnotation(org.springframework.web.bind.annotation.RequestMapping.class) != null ? "REQUEST" : "OTHER");
            logEntity.setOperName("admin"); // TODO: 从SecurityContext获取
            logEntity.setOperUrl(""); // TODO: 从Request获取
            logEntity.setOperIp("127.0.0.1"); // TODO: 从Request获取
            logEntity.setOperParam(argsToString(joinPoint.getArgs()));
            logEntity.setJsonResult(result != null ? result.toString() : "");
            logEntity.setStatus(status);
            logEntity.setErrorMsg(errorMsg);
            logEntity.setOperTime(LocalDateTime.now());
            logEntity.setCostTime(0L); // TODO: 计算方法耗时
            
            // 异步保存日志（避免影响主流程）
            new Thread(() -> {
                try {
                    operLogMapper.insert(logEntity);
                } catch (Exception e) {
                    log.error("保存操作日志失败", e);
                }
            }).start();
            
        } catch (Exception e) {
            log.error("操作日志记录失败", e);
        }
    }
    
    /**
     * 参数转字符串
     */
    private String argsToString(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            if (arg != null) {
                String str = arg.toString();
                if (str.length() > 500) {
                    str = str.substring(0, 500) + "...";
                }
                sb.append(str).append(",");
            }
        }
        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
    }
}

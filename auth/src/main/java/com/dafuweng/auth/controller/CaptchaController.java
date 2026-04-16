package com.dafuweng.auth.controller;

import com.dafuweng.common.entity.Result;
import com.wf.captcha.SpecCaptcha;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码控制器
 * 使用内存存储（ConcurrentHashMap）替代Redis
 */
@RestController
public class CaptchaController {
    
    // 内存存储验证码（key: uuid, value: code）
    private static final ConcurrentHashMap<String, String> CAPTCHA_CACHE = new ConcurrentHashMap<>();
    
    /**
     * 生成验证码
     */
    @GetMapping("/captchaImage")
    public Result<Map<String, Object>> captchaImage() {
        // 生成验证码
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        String code = captcha.text().toLowerCase();
        String uuid = UUID.randomUUID().toString();
        
        // 存储到内存（5分钟后自动清理）
        CAPTCHA_CACHE.put(uuid, code);
        
        // 定时清理过期验证码
        cleanExpiredCaptchas();
        
        // 返回Base64图片
        Map<String, Object> result = new HashMap<>();
        result.put("captchaEnabled", true);
        result.put("uuid", uuid);
        result.put("img", captcha.toBase64());
        
        return Result.success(result);
    }
    
    /**
     * 验证验证码
     */
    public static boolean verifyCaptcha(String uuid, String code) {
        if (uuid == null || code == null) {
            return false;
        }
        
        String cachedCode = CAPTCHA_CACHE.remove(uuid); // 验证后删除，防止重复使用
        return cachedCode != null && cachedCode.equals(code.toLowerCase());
    }
    
    /**
     * 清理过期验证码（超过5分钟）
     */
    private static void cleanExpiredCaptchas() {
        // 简单实现：如果超过1000个，清空所有（实际应使用时间戳）
        if (CAPTCHA_CACHE.size() > 1000) {
            CAPTCHA_CACHE.clear();
        }
    }
}

package com.dafuweng.auth.controller;

import com.dafuweng.auth.service.QrCodeLoginService;
import com.dafuweng.auth.utils.JwtUtil;
import com.dafuweng.common.entity.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OAuth2 二维码登录控制器
 * 提供扫码登录和管理后台接口
 */
@Slf4j
@Tag(name = "OAuth2二维码登录", description = "扫码登录相关接口")
@RestController
@RequestMapping("/api/oauth2/qrcode")
public class OAuth2QrCodeController {
    
    @Autowired
    private QrCodeLoginService qrCodeLoginService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 生成二维码
     */
    @Operation(summary = "生成二维码", description = "生成扫码登录二维码")
    @PostMapping("/generate")
    public Result<Map<String, Object>> generateQrCode(@RequestBody(required = false) Map<String, String> request) {
        String clientType = request != null ? request.getOrDefault("clientType", "web") : "web";
        String deviceName = request != null ? request.getOrDefault("deviceName", "Unknown") : "Unknown";
        
        try {
            Map<String, Object> result = qrCodeLoginService.generateSession(clientType, deviceName);
            return Result.success(result);
        } catch (Exception e) {
            log.error("生成二维码失败: {}", e.getMessage(), e);
            return Result.error(500, "生成二维码失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询二维码状态（Web 端轮询）
     */
    @Operation(summary = "查询二维码状态", description = "Web 端轮询扫码状态")
    @GetMapping("/status")
    public Result<Map<String, Object>> queryStatus(@RequestParam String tid) {
        try {
            Map<String, Object> result = qrCodeLoginService.queryStatus(tid);
            return Result.success(result);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("过期")) {
                return Result.error(400, e.getMessage());
            }
            return Result.error(404, e.getMessage());
        } catch (Exception e) {
            log.error("查询状态失败: {}", e.getMessage(), e);
            return Result.error(500, "查询状态失败");
        }
    }
    
    /**
     * 扫码（管理后台模拟扫码）
     */
    @Operation(summary = "扫码", description = "管理后台模拟扫码（双浏览器窗口方案）")
    @PostMapping("/scan")
    public Result<Void> scanCode(@RequestBody Map<String, Object> request) {
        String loginTid = (String) request.get("loginTid");
        
        // 安全获取 userId（处理 null 和类型转换）
        Object userIdObj = request.get("userId");
        if (userIdObj == null) {
            return Result.error(400, "userId 不能为空");
        }
        Long userId = userIdObj instanceof Number 
            ? ((Number) userIdObj).longValue() 
            : Long.valueOf(userIdObj.toString());
        
        String username = (String) request.get("username");
        String deviceId = (String) request.getOrDefault("deviceId", "browser");
        
        try {
            qrCodeLoginService.scanCode(loginTid, userId, username, deviceId);
            return Result.success("扫码成功");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("扫码失败: {}", e.getMessage(), e);
            return Result.error(500, "扫码失败");
        }
    }
    
    /**
     * 确认/拒绝登录（管理后台操作）
     */
    @Operation(summary = "确认/拒绝登录", description = "管理后台确认或拒绝登录")
    @PostMapping("/confirm")
    public Result<Void> confirmCode(@RequestBody Map<String, Object> request) {
        String loginTid = (String) request.get("loginTid");
        
        // 安全获取 userId
        Object userIdObj = request.get("userId");
        if (userIdObj == null) {
            return Result.error(400, "userId 不能为空");
        }
        Long userId = userIdObj instanceof Number 
            ? ((Number) userIdObj).longValue() 
            : Long.valueOf(userIdObj.toString());
        
        Boolean confirm = (Boolean) request.get("confirm");
        
        try {
            qrCodeLoginService.confirmCode(loginTid, userId, confirm);
            return Result.success(confirm ? "确认登录成功" : "已拒绝登录");
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("确认登录失败: {}", e.getMessage(), e);
            return Result.error(500, "确认登录失败");
        }
    }
    
    /**
     * 获取所有活跃会话（管理后台使用）
     */
    @Operation(summary = "获取活跃会话", description = "管理后台查看所有待确认的扫码会话")
    @GetMapping("/sessions")
    public Result<List<Map<String, Object>>> getActiveSessions() {
        try {
            List<Map<String, Object>> sessions = qrCodeLoginService.getActiveSessions();
            return Result.success(sessions);
        } catch (Exception e) {
            log.error("获取会话列表失败: {}", e.getMessage(), e);
            return Result.error(500, "获取会话列表失败");
        }
    }
}

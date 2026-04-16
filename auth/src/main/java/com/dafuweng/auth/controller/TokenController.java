package com.dafuweng.auth.controller;

import com.dafuweng.auth.entity.SysUserEntity;
import com.dafuweng.auth.service.SysUserService;
import com.dafuweng.auth.service.TokenStoreService;
import com.dafuweng.auth.utils.JwtUtil;
import com.dafuweng.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Token 管理控制器
 * 
 * 提供：
 * - Token 刷新接口
 * - 登出接口
 * - 多设备管理
 */
@RestController
@RequestMapping("/api/token")
public class TokenController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenStoreService tokenStoreService;

    @Autowired
    private SysUserService sysUserService;

    /**
     * 刷新 Token
     * 
     * 请求体：
     * {
     *   "refreshToken": "xxx",
     *   "accessToken": "xxx"  // 可选，用于验证当前用户
     * }
     * 
     * 返回：
     * {
     *   "code": 200,
     *   "data": {
     *     "accessToken": "新的JWT Token",
     *     "refreshToken": "新的Refresh Token",
     *     "expiresIn": 86400
     *   }
     * }
     */
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Result.error(400, "Refresh Token不能为空");
        }

        // 验证 Refresh Token
        TokenStoreService.RefreshTokenInfo tokenInfo = tokenStoreService.validateRefreshToken(refreshToken);
        if (tokenInfo == null) {
            return Result.error(401, "Refresh Token无效或已过期，请重新登录");
        }

        // 获取用户最新信息（可能权限有变更）
        SysUserEntity user = sysUserService.getById(tokenInfo.userId());
        if (user == null) {
            return Result.error(401, "用户不存在");
        }

        // 获取用户权限
        List<String> permissions = sysUserService.getPermCodesByUserId(user.getId());
        List<String> roles = new ArrayList<>();
        if (permissions.contains("*:*:*")) {
            roles.add("admin");
        } else {
            roles.add("common");
        }

        // 生成新的 Access Token
        String newAccessToken = jwtUtil.generateToken(
            user.getId(),
            user.getUsername(),
            roles.toArray(new String[0]),
            permissions.toArray(new String[0])
        );

        // 生成新的 Refresh Token（轮转）
        String newRefreshToken = tokenStoreService.createRefreshToken(
            user.getId(),
            user.getUsername(),
            tokenInfo.deviceId()
        );

        // 删除旧的 Refresh Token
        tokenStoreService.removeRefreshToken(refreshToken);

        // 返回新 Token
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", newAccessToken);
        result.put("refreshToken", newRefreshToken);
        result.put("expiresIn", jwtUtil.getExpirationSeconds());
        result.put("refreshExpiresIn", 604800); // 7天

        return Result.success(result);
    }

    /**
     * 登出
     * 
     * 请求体：
     * {
     *   "refreshToken": "xxx"  // 可选
     * }
     * 
     * 返回：
     * {
     *   "code": 200,
     *   "message": "登出成功"
     * }
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody(required = false) Map<String, String> request,
                                @RequestHeader(value = "Authorization", required = false) String authHeader) {
        // 如果提供了 refreshToken，删除它
        if (request != null && request.containsKey("refreshToken")) {
            tokenStoreService.removeRefreshToken(request.get("refreshToken"));
        }

        // 如果提供了 accessToken，将旧Token加入黑名单
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            try {
                String jti = jwtUtil.getJtiFromToken(accessToken);
                long expireAt = jwtUtil.getExpirationTime(accessToken);
                tokenStoreService.addToBlacklist(jti, jwtUtil.getUserIdFromToken(accessToken), expireAt);
            } catch (Exception e) {
                // Token 可能已过期或无效，忽略
            }
        }

        Result<Void> result = Result.success();
        result.setMessage("登出成功");
        return result;
    }

    /**
     * 获取当前登录设备信息
     * 
     * 返回：
     * {
     *   "code": 200,
     *   "data": {
     *     "activeDevices": 2,
     *     "currentDevice": "device-xxx"
     *   }
     * }
     */
    @GetMapping("/devices")
    public Result<Map<String, Object>> getDevices(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error(401, "未登录");
        }

        try {
            String accessToken = authHeader.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(accessToken);
            String currentDevice = "default"; // TODO: 从Token中获取deviceId

            Map<String, Object> result = new HashMap<>();
            result.put("activeDevices", tokenStoreService.getActiveDeviceCount(userId));
            result.put("currentDevice", currentDevice);

            return Result.success(result);
        } catch (Exception e) {
            return Result.error(401, "Token无效");
        }
    }

    /**
     * 强制登出所有设备
     * 
     * 返回：
     * {
     *   "code": 200,
     *   "message": "已强制登出所有设备"
     * }
     */
    @DeleteMapping("/devices")
    public Result<Void> logoutAllDevices(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error(401, "未登录");
        }

        try {
            String accessToken = authHeader.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(accessToken);
            
            // 删除所有设备的 Refresh Token
            tokenStoreService.removeAllUserTokens(userId);

            return Result.success("已强制登出所有设备");
        } catch (Exception e) {
            return Result.error(401, "Token无效");
        }
    }

    /**
     * 登出指定设备
     * 
     * 参数：
     * - deviceId: 设备ID
     * 
     * 返回：
     * {
     *   "code": 200,
     *   "message": "设备已登出"
     * }
     */
    @DeleteMapping("/devices/{deviceId}")
    public Result<Void> logoutDevice(@PathVariable String deviceId,
                                      @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.error(401, "未登录");
        }

        try {
            String accessToken = authHeader.substring(7);
            Long userId = jwtUtil.getUserIdFromToken(accessToken);
            
            // 删除指定设备的 Refresh Token
            tokenStoreService.removeUserDeviceToken(userId, deviceId);

            return Result.success("设备已登出");
        } catch (Exception e) {
            return Result.error(401, "Token无效");
        }
    }

    /**
     * 获取 Token 存储统计（仅管理员）
     * 
     * 返回：
     * {
     *   "code": 200,
     *   "data": {
     *     "activeRefreshTokens": 10,
     *     "blacklistedTokens": 5
     *   }
     * }
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(tokenStoreService.getStats());
    }
}

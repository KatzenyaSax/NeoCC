package com.dafuweng.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Token 存储服务
 * 
 * 功能：
 * - 管理 Refresh Token（支持用户登录多个设备）
 * - Token 黑名单（支持主动吊销）
 * - Token 使用追踪
 */
@Service
public class TokenStoreService {

    /**
     * Refresh Token 存储
     * Key: refreshToken, Value: {userId, createTime, deviceId}
     */
    private final Map<String, RefreshTokenInfo> refreshTokenStore = new ConcurrentHashMap<>();

    /**
     * 黑名单 Token 存储
     * Key: jti (JWT ID), Value: expireTime
     */
    private final Map<String, Long> tokenBlacklist = new ConcurrentHashMap<>();

    /**
     * Refresh Token 有效期（毫秒），默认 7 天
     */
    @Value("${jwt.refresh-expiration:604800000}")
    private Long refreshExpiration;

    /**
     * Token 清理调度器
     */
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public TokenStoreService() {
        // 启动定时清理任务，每小时清理过期的token
        scheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.HOURS);
    }

    /**
     * 创建 Refresh Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param deviceId 设备ID
     * @return Refresh Token
     */
    public String createRefreshToken(Long userId, String username, String deviceId) {
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        RefreshTokenInfo info = new RefreshTokenInfo(userId, username, deviceId, System.currentTimeMillis());
        refreshTokenStore.put(refreshToken, info);
        return refreshToken;
    }

    /**
     * 验证 Refresh Token
     *
     * @param refreshToken Refresh Token
     * @return 用户信息，如果无效返回 null
     */
    public RefreshTokenInfo validateRefreshToken(String refreshToken) {
        RefreshTokenInfo info = refreshTokenStore.get(refreshToken);
        if (info == null) {
            return null;
        }
        // 检查是否过期
        if (System.currentTimeMillis() - info.createTime > refreshExpiration) {
            refreshTokenStore.remove(refreshToken);
            return null;
        }
        return info;
    }

    /**
     * 删除 Refresh Token（登出）
     *
     * @param refreshToken Refresh Token
     */
    public void removeRefreshToken(String refreshToken) {
        refreshTokenStore.remove(refreshToken);
    }

    /**
     * 删除用户的所有 Refresh Token（强制登出所有设备）
     *
     * @param userId 用户ID
     */
    public void removeAllUserTokens(Long userId) {
        refreshTokenStore.entrySet().removeIf(entry -> 
            entry.getValue().userId.equals(userId));
    }

    /**
     * 删除用户的指定设备 Refresh Token
     *
     * @param userId   用户ID
     * @param deviceId 设备ID
     */
    public void removeUserDeviceToken(Long userId, String deviceId) {
        refreshTokenStore.entrySet().removeIf(entry -> 
            entry.getValue().userId.equals(userId) && 
            deviceId.equals(entry.getValue().deviceId));
    }

    /**
     * 添加 Token 到黑名单
     *
     * @param jti      JWT ID
     * @param userId   用户ID
     * @param expireAt Token 过期时间
     */
    public void addToBlacklist(String jti, Long userId, long expireAt) {
        tokenBlacklist.put(jti, expireAt);
    }

    /**
     * 检查 Token 是否在黑名单中
     *
     * @param jti JWT ID
     * @return 是否在黑名单中
     */
    public boolean isBlacklisted(String jti) {
        Long expireAt = tokenBlacklist.get(jti);
        if (expireAt == null) {
            return false;
        }
        // 如果已过期，从黑名单中移除
        if (System.currentTimeMillis() > expireAt) {
            tokenBlacklist.remove(jti);
            return false;
        }
        return true;
    }

    /**
     * 获取用户当前活跃的设备数量
     *
     * @param userId 用户ID
     * @return 设备数量
     */
    public int getActiveDeviceCount(Long userId) {
        return (int) refreshTokenStore.values().stream()
                .filter(info -> info.userId.equals(userId))
                .count();
    }

    /**
     * 清理过期的 Token
     */
    private void cleanupExpiredTokens() {
        long now = System.currentTimeMillis();
        
        // 清理过期的 Refresh Token
        refreshTokenStore.entrySet().removeIf(entry -> 
            now - entry.getValue().createTime > refreshExpiration);
        
        // 清理过期的黑名单记录
        tokenBlacklist.entrySet().removeIf(entry -> now > entry.getValue());
        
        System.out.println("[TokenStore] Cleaned up expired tokens. Active refresh tokens: " + 
            refreshTokenStore.size() + ", Blacklisted: " + tokenBlacklist.size());
    }

    /**
     * 获取存储统计信息
     */
    public Map<String, Object> getStats() {
        return Map.of(
            "activeRefreshTokens", refreshTokenStore.size(),
            "blacklistedTokens", tokenBlacklist.size()
        );
    }

    /**
     * Refresh Token 信息
     */
    public record RefreshTokenInfo(Long userId, String username, String deviceId, long createTime) {}
}

package com.dafuweng.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 二维码登录会话信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QrCodeLoginSession {
    
    /**
     * 登录事务唯一标识（Login Transaction ID）
     */
    private String loginTid;
    
    /**
     * 扫码用户ID（扫码后填充）
     */
    private Long userId;
    
    /**
     * 扫码用户名（扫码后填充）
     */
    private String username;
    
    /**
     * 当前状态
     */
    private QrCodeStatus status;
    
    /**
     * 扫码设备ID（双浏览器窗口暂时使用 "browser"）
     */
    private String deviceId;
    
    /**
     * 扫码时间戳
     */
    private Long scannedAt;
    
    /**
     * 确认时间戳
     */
    private Long confirmedAt;
    
    /**
     * 创建时间戳
     */
    private Long createdAt;
    
    /**
     * 过期时间戳
     */
    private Long expireAt;
    
    /**
     * 客户端类型（web/mobile）
     */
    private String clientType;
    
    /**
     * 设备描述
     */
    private String deviceName;
}

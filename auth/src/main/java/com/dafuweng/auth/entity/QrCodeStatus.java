package com.dafuweng.auth.entity;

/**
 * 二维码登录状态枚举
 */
public enum QrCodeStatus {
    /**
     * 已生成，等待扫码
     */
    GENERATED,
    
    /**
     * 已扫码，等待确认
     */
    SCANNED,
    
    /**
     * 已确认，登录成功
     */
    CONFIRMED,
    
    /**
     * 已拒绝
     */
    REJECTED,
    
    /**
     * 已过期
     */
    EXPIRED
}

package com.dafuweng.auth.service;

import com.dafuweng.auth.entity.QrCodeLoginSession;
import com.dafuweng.auth.entity.QrCodeStatus;
import com.dafuweng.auth.utils.JwtUtil;
import com.dafuweng.auth.utils.QrCodeGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 二维码登录服务
 * 管理二维码登录会话的完整生命周期
 */
@Slf4j
@Service
public class QrCodeLoginService {
    
    /**
     * 内存存储二维码登录会话
     * Key: loginTid, Value: QrCodeLoginSession
     */
    private final ConcurrentHashMap<String, QrCodeLoginSession> sessionStore = new ConcurrentHashMap<>();
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TokenStoreService tokenStoreService;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 二维码有效期（秒），默认 5 分钟
     */
    @Value("${qrcode.expire-seconds:300}")
    private Integer expireSeconds;
    
    /**
     * 二维码内容前缀（扫码后跳转的 URL）
     */
    @Value("${qrcode.url-prefix:https://yourapp.com/scan/login}")
    private String urlPrefix;
    
    /**
     * 生成二维码登录会话
     * 
     * @param clientType 客户端类型（web/mobile）
     * @param deviceName 设备描述
     * @return 二维码会话信息（包含 Base64 图片）
     */
    public Map<String, Object> generateSession(String clientType, String deviceName) {
        // 生成唯一的 loginTid
        String loginTid = UUID.randomUUID().toString().replace("-", "");
        
        long now = System.currentTimeMillis();
        long expireAt = now + (expireSeconds * 1000L);
        
        // 生成二维码内容 URL
        String qrcodeContent = urlPrefix + "?tid=" + loginTid + "&client=" + clientType;
        
        // 创建会话
        QrCodeLoginSession session = QrCodeLoginSession.builder()
                .loginTid(loginTid)
                .status(QrCodeStatus.GENERATED)
                .clientType(clientType)
                .deviceName(deviceName)
                .createdAt(now)
                .expireAt(expireAt)
                .build();
        
        // 存储到内存
        sessionStore.put(loginTid, session);
        
        // 生成二维码图片
        String qrcodeBase64;
        try {
            qrcodeBase64 = QrCodeGenerator.generateQrCodeBase64(qrcodeContent, 300, 300);
        } catch (Exception e) {
            log.error("生成二维码失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成二维码失败", e);
        }
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("loginTid", loginTid);
        result.put("qrcodeContent", qrcodeContent);
        result.put("qrcodeBase64", qrcodeBase64);
        result.put("expireIn", expireSeconds);
        result.put("pollInterval", 2000); // 前端轮询间隔 2 秒
        
        log.info("生成二维码会话: loginTid={}, expireIn={}s", loginTid, expireSeconds);
        return result;
    }
    
    /**
     * 查询二维码状态
     * 
     * @param loginTid 登录事务 ID
     * @return 状态信息（如果已确认，包含 Token）
     */
    public Map<String, Object> queryStatus(String loginTid) {
        QrCodeLoginSession session = sessionStore.get(loginTid);
        
        if (session == null) {
            throw new RuntimeException("二维码会话不存在");
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > session.getExpireAt()) {
            session.setStatus(QrCodeStatus.EXPIRED);
            sessionStore.remove(loginTid);
            throw new RuntimeException("二维码已过期，请刷新重试");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", session.getStatus().name().toLowerCase());
        
        // 根据状态返回不同信息
        switch (session.getStatus()) {
            case GENERATED:
                result.put("message", "等待扫码");
                break;
                
            case SCANNED:
                result.put("message", "已扫码，请在手机上确认");
                result.put("username", session.getUsername());
                break;
                
            case CONFIRMED:
                // 生成 Token
                String accessToken = generateAccessToken(session);
                String refreshToken = tokenStoreService.createRefreshToken(
                        session.getUserId(),
                        session.getUsername(),
                        session.getDeviceId()
                );
                
                result.put("message", "登录成功");
                result.put("token", accessToken);
                result.put("refreshToken", refreshToken);
                result.put("expiresIn", jwtUtil.getExpirationSeconds());
                result.put("refreshExpiresIn", 604800); // 7 天
                
                // 返回用户信息
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", session.getUserId());
                userInfo.put("username", session.getUsername());
                result.put("user", userInfo);
                
                // 删除会话（一次性使用）
                sessionStore.remove(loginTid);
                log.info("二维码登录成功: userId={}, username={}", session.getUserId(), session.getUsername());
                break;
                
            case REJECTED:
                result.put("message", "登录已被拒绝");
                sessionStore.remove(loginTid);
                break;
                
            case EXPIRED:
                result.put("message", "二维码已过期");
                sessionStore.remove(loginTid);
                break;
        }
        
        return result;
    }
    
    /**
     * 扫码（模拟手机扫码）
     * 
     * @param loginTid 登录事务 ID
     * @param userId 用户 ID
     * @param username 用户名
     * @param deviceId 设备 ID
     */
    public void scanCode(String loginTid, Long userId, String username, String deviceId) {
        QrCodeLoginSession session = sessionStore.get(loginTid);
        
        if (session == null) {
            throw new RuntimeException("二维码会话不存在");
        }
        
        if (session.getStatus() != QrCodeStatus.GENERATED) {
            throw new RuntimeException("二维码状态不正确，当前状态: " + session.getStatus());
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > session.getExpireAt()) {
            session.setStatus(QrCodeStatus.EXPIRED);
            sessionStore.remove(loginTid);
            throw new RuntimeException("二维码已过期");
        }
        
        // ⚠️ 安全增强：从数据库验证用户
        try {
            String sql = "SELECT id, username, status FROM sys_user WHERE id = ?";
            Map<String, Object> user = jdbcTemplate.queryForMap(sql, userId);
            
            if (user == null || user.isEmpty()) {
                throw new RuntimeException("用户不存在，userId: " + userId);
            }
            
            // 检查用户状态
            Integer status = (Integer) user.get("status");
            if (status == null || status != 1) {
                String disabledUsername = (String) user.get("username");
                throw new RuntimeException("用户已被禁用，username: " + disabledUsername);
            }
            
            // ⚠️ 使用数据库中的真实用户名（防止冒充）
            String realUsername = (String) user.get("username");
            Long realUserId = ((Number) user.get("id")).longValue();
            
            // 更新状态
            session.setStatus(QrCodeStatus.SCANNED);
            session.setUserId(realUserId);  // ✅ 使用数据库中的 userId
            session.setUsername(realUsername);  // ✅ 使用数据库中的用户名
            session.setDeviceId(deviceId);
            session.setScannedAt(System.currentTimeMillis());
            
            log.info("扫码成功: loginTid={}, userId={}, username={}, deviceId={}", 
                    loginTid, realUserId, realUsername, deviceId);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // 用户不存在
            throw new RuntimeException("用户不存在，userId: " + userId);
        } catch (Exception e) {
            // 其他异常
            if (e.getMessage() != null && e.getMessage().contains("用户")) {
                throw e;  // 重新抛出我们已经处理过的异常
            }
            log.error("扫码验证用户失败: userId={}", userId, e);
            throw new RuntimeException("扫码验证用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 确认/拒绝登录
     * 
     * @param loginTid 登录事务 ID
     * @param userId 用户 ID（用于验证）
     * @param confirm true=确认，false=拒绝
     */
    public void confirmCode(String loginTid, Long userId, boolean confirm) {
        QrCodeLoginSession session = sessionStore.get(loginTid);
        
        if (session == null) {
            throw new RuntimeException("二维码会话不存在");
        }
        
        if (session.getStatus() != QrCodeStatus.SCANNED) {
            throw new RuntimeException("二维码状态不正确，当前状态: " + session.getStatus());
        }
        
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("用户不匹配");
        }
        
        if (confirm) {
            // 确认登录
            session.setStatus(QrCodeStatus.CONFIRMED);
            session.setConfirmedAt(System.currentTimeMillis());
            log.info("确认登录: loginTid={}, userId={}", loginTid, userId);
        } else {
            // 拒绝登录
            session.setStatus(QrCodeStatus.REJECTED);
            sessionStore.remove(loginTid);
            log.info("拒绝登录: loginTid={}, userId={}", loginTid, userId);
        }
    }
    
    /**
     * 生成 Access Token
     */
    private String generateAccessToken(QrCodeLoginSession session) {
        // 获取用户权限（简化处理，实际应从数据库查询）
        String[] roles = {"common"};
        String[] perms = {};
        
        return jwtUtil.generateToken(
                session.getUserId(),
                session.getUsername(),
                roles,
                perms
        );
    }
    
    /**
     * 定时清理过期会话（每分钟执行一次）
     */
    @Scheduled(fixedRate = 60000)
    public void cleanExpiredSessions() {
        long now = System.currentTimeMillis();
        int cleanedCount = 0;
        
        Iterator<Map.Entry<String, QrCodeLoginSession>> iterator = sessionStore.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, QrCodeLoginSession> entry = iterator.next();
            QrCodeLoginSession session = entry.getValue();
            
            // 清理过期会话或终态会话
            if (now > session.getExpireAt() || 
                session.getStatus() == QrCodeStatus.CONFIRMED ||
                session.getStatus() == QrCodeStatus.REJECTED) {
                iterator.remove();
                cleanedCount++;
            }
        }
        
        if (cleanedCount > 0) {
            log.info("清理过期二维码会话: {} 个", cleanedCount);
        }
    }
    
    /**
     * 获取所有活跃会话（用于管理后台）
     */
    public List<Map<String, Object>> getActiveSessions() {
        List<Map<String, Object>> sessions = new ArrayList<>();
        long now = System.currentTimeMillis();
        
        for (QrCodeLoginSession session : sessionStore.values()) {
            // 只返回未过期的会话
            if (now <= session.getExpireAt() && 
                session.getStatus() != QrCodeStatus.CONFIRMED &&
                session.getStatus() != QrCodeStatus.REJECTED) {
                
                Map<String, Object> sessionInfo = new HashMap<>();
                sessionInfo.put("loginTid", session.getLoginTid());
                sessionInfo.put("status", session.getStatus().name().toLowerCase());
                sessionInfo.put("clientType", session.getClientType());
                sessionInfo.put("deviceName", session.getDeviceName());
                sessionInfo.put("createdAt", session.getCreatedAt());
                sessionInfo.put("expireAt", session.getExpireAt());
                sessionInfo.put("remainingSeconds", (session.getExpireAt() - now) / 1000);
                
                if (session.getUserId() != null) {
                    sessionInfo.put("userId", session.getUserId());
                    sessionInfo.put("username", session.getUsername());
                }
                
                sessions.add(sessionInfo);
            }
        }
        
        // 按创建时间倒序（处理 null 值）
        sessions.sort((a, b) -> {
            Long timeA = (Long) a.get("createdAt");
            Long timeB = (Long) b.get("createdAt");
            if (timeA == null) return 1;
            if (timeB == null) return -1;
            return Long.compare(timeB, timeA);
        });
        return sessions;
    }
}

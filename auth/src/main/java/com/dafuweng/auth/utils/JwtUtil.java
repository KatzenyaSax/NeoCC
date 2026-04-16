package com.dafuweng.auth.utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 工具类
 * 
 * 支持：
 * - JWT Token 生成（带签名和过期时间）
 * - JWT Token 验证
 * - Token 刷新
 * - Token 黑名单
 * - 解析 Token 获取用户信息
 */
@Component
public class JwtUtil {

    /**
     * JWT 密钥（从配置文件读取）
     * 注意：在生产环境中应使用更复杂的密钥，并妥善保管
     */
    @Value("${jwt.secret:NeoCC2024SecretKeyForJWTTokenGenerationAndValidation123456}")
    private String secret;

    /**
     * Token 过期时间（毫秒），默认 24 小时
     */
    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    /**
     * Token 前缀
     */
    @Value("${jwt.prefix:Bearer }")
    private String prefix;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        // 确保密钥长度足够（至少 256 位）
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // 填充密钥到至少 256 位
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, Math.min(keyBytes.length, 32));
            keyBytes = paddedKey;
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 Token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param roles    角色列表
     * @return JWT Token
     */
    public String generateToken(Long userId, String username, String... roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roles", roles);
        
        return Jwts.builder()
                .claims(claims)
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成 Token（带完整用户信息）
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param roles    角色列表
     * @param perms    权限列表
     * @return JWT Token
     */
    public String generateToken(Long userId, String username, String[] roles, String[] perms) {
        // 生成唯一的JWT ID
        String jti = UUID.randomUUID().toString();
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("jti", jti);        // JWT ID，用于黑名单
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roles", roles);
        claims.put("perms", perms);
        
        return Jwts.builder()
                .id(jti)  // 设置JWT ID
                .claims(claims)
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 验证 Token
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 解析 Token 获取用户ID
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 解析 Token 获取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 解析 Token 获取角色
     *
     * @param token JWT Token
     * @return 角色数组
     */
    @SuppressWarnings("unchecked")
    public String[] getRolesFromToken(String token) {
        Claims claims = parseToken(token);
        Object roles = claims.get("roles");
        if (roles instanceof String[]) {
            return (String[]) roles;
        }
        return new String[0];
    }

    /**
     * 解析 Token 获取权限
     *
     * @param token JWT Token
     * @return 权限数组
     */
    @SuppressWarnings("unchecked")
    public String[] getPermsFromToken(String token) {
        Claims claims = parseToken(token);
        Object perms = claims.get("perms");
        if (perms instanceof String[]) {
            return (String[]) perms;
        }
        return new String[0];
    }

    /**
     * 解析 Token 获取 JWT ID
     *
     * @param token JWT Token
     * @return JWT ID
     */
    public String getJtiFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getId();
    }

    /**
     * 解析 Token
     *
     * @param token JWT Token
     * @return Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 检查 Token 是否过期
     *
     * @param token JWT Token
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * 刷新 Token（生成新Token，保留原JTI用于旧Token拉黑）
     *
     * @param token 旧 Token
     * @return 新 Token
     */
    public String refreshToken(String token) {
        Claims claims = parseToken(token);
        Long userId = Long.parseLong(claims.getSubject());
        String username = claims.get("username", String.class);
        
        return generateToken(userId, username, getRolesFromToken(token), getPermsFromToken(token));
    }

    /**
     * 获取 Token 过期时间
     *
     * @param token JWT Token
     * @return 过期时间戳（毫秒）
     */
    public long getExpirationTime(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().getTime();
    }

    /**
     * 获取 Token 前缀
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * 获取 Access Token 有效期（秒）
     */
    public long getExpirationSeconds() {
        return expiration / 1000;
    }

    /**
     * 去除 Token 前缀
     *
     * @param token 带前缀的 Token
     * @return 纯 Token
     */
    public String removePrefix(String token) {
        if (token != null && token.startsWith(prefix)) {
            return token.substring(prefix.trim().length());
        }
        return token;
    }
}

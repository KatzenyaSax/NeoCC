package com.dafuweng.auth.controller;

import com.dafuweng.auth.annotation.OperLog;
import com.dafuweng.auth.entity.SysMenuEntity;
import com.dafuweng.auth.entity.SysUserEntity;
import com.dafuweng.auth.service.SysMenuService;
import com.dafuweng.auth.service.SysUserService;
import com.dafuweng.auth.service.TokenStoreService;
import com.dafuweng.auth.utils.JwtUtil;
import com.dafuweng.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * RuoYi 前端适配控制器
 * 适配 RuoYi-Vue3 前端所需的接口格式
 */
@RestController
public class RuoyiAdapterController {

    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private SysMenuService sysMenuService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TokenStoreService tokenStoreService;

    /**
     * 登录接口 - 适配 RuoYi 格式
     * 使用真正的JWT Token
     */
    @OperLog(title = "用户登录", businessType = 0)
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        String code = loginRequest.get("code");
        String uuid = loginRequest.get("uuid");
        
        // 验证验证码
        if (code != null && uuid != null && !code.isEmpty()) {
            if (!CaptchaController.verifyCaptcha(uuid, code)) {
                return Result.error(400, "验证码错误");
            }
        }
        
        // 调用原有登录逻辑
        SysUserEntity user = sysUserService.login(username, password, "unknown");
        
        // 获取用户权限
        List<String> permissions = sysUserService.getPermCodesByUserId(user.getId());
        List<String> roles = new ArrayList<>();
        if (permissions.contains("*:*:*")) {
            roles.add("admin");
        } else {
            roles.add("common");
        }
        
        // 生成真正的JWT Token
        String jwtToken = jwtUtil.generateToken(
            user.getId(),
            user.getUsername(),
            roles.toArray(new String[0]),
            permissions.toArray(new String[0])
        );
        
        // 生成Refresh Token（用于Token刷新）
        String deviceId = loginRequest.getOrDefault("deviceId", "default");
        String refreshToken = tokenStoreService.createRefreshToken(
            user.getId(),
            user.getUsername(),
            deviceId
        );
        
        // 适配 RuoYi 响应格式
        Map<String, Object> result = new HashMap<>();
        result.put("token", jwtToken); // 使用真正的JWT Token
        result.put("refreshToken", refreshToken); // Refresh Token
        result.put("expires_in", 86400); // Access Token 过期时间 24小时
        result.put("refreshExpiresIn", 604800); // Refresh Token 过期时间 7天
        
        return Result.success(result);
    }

    /**
     * 获取用户信息 - 适配 RuoYi 格式
     */
    @GetMapping("/getInfo")
    public Result<Map<String, Object>> getInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        // 从 token 获取用户ID（简化处理，实际应解析 token）
        Long userId = getUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        SysUserEntity user = sysUserService.getById(userId);
        if (user == null) {
            return Result.error(401, "用户不存在");
        }
        
        // 获取用户权限
        List<String> permissions = sysUserService.getPermCodesByUserId(userId);
        List<String> roles = new ArrayList<>();
        
        // 根据权限判断角色
        if (permissions.contains("*:*:*")) {
            roles.add("admin");
        } else {
            roles.add("common");
        }
        
        // 适配 RuoYi 用户信息格式
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userId", user.getId());
        userMap.put("userName", user.getUsername());
        userMap.put("nickName", user.getRealName());
        userMap.put("avatar", "");
        userMap.put("email", user.getEmail());
        userMap.put("phonenumber", user.getPhone());
        userMap.put("sex", "0");
        userMap.put("status", user.getStatus());
        userMap.put("createTime", user.getCreatedAt());
        
        Map<String, Object> result = new HashMap<>();
        result.put("user", userMap);
        result.put("roles", roles);
        result.put("permissions", permissions.isEmpty() ? Arrays.asList("*:*:*") : permissions);
        
        return Result.success(result);
    }

    /**
     * 获取路由菜单 - 从数据库动态加载
     */
    @GetMapping("/getRouters")
    public Result<List<Map<String, Object>>> getRouters(@RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = getUserIdFromToken(token);
        
        // 从数据库获取用户菜单
        List<SysMenuEntity> menus = sysMenuService.getUserMenus(userId);
        
        // 转换为RuoYi前端格式
        List<Map<String, Object>> routers = convertToRouters(menus);
        
        return Result.success(routers);
    }

    /**
     * 退出登录 - POST 请求
     */
    @PostMapping(value = "/logout", produces = "application/json")
    public Result<Void> logout() {
        // 简化处理，实际应清除 token
        return Result.success();
    }

    /**
     * 退出登录 - GET 请求（处理 /login?logout 跳转）
     */
    @GetMapping("/login")
    public Result<Void> logoutGet(@RequestParam(value = "logout", required = false) String logout) {
        // 简化处理，实际应清除 token
        return Result.success();
    }

    /**
     * 注册接口 - 放行直接通过（返回模拟成功）
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody Map<String, String> registerRequest) {
        // 注册功能暂不开放，返回成功但不实际注册
        return Result.success();
    }

    /**
     * 解锁屏幕 - 放行直接通过
     */
    @PostMapping("/unlockscreen")
    public Result<Void> unlockScreen(@RequestBody Map<String, String> request) {
        return Result.success();
    }

    // ========== 辅助方法 ==========
    
    /**
     * 从Token中获取用户ID
     * 使用JwtUtil解析真正的JWT Token
     */
    private Long getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            // 去除Bearer前缀
            String cleanToken = token.replace("Bearer ", "").trim();
            // 使用JwtUtil解析
            if (jwtUtil.validateToken(cleanToken)) {
                return jwtUtil.getUserIdFromToken(cleanToken);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private Map<String, Object> createMeta(String title, String icon, boolean noCache, String link) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("title", title);
        meta.put("icon", icon);
        meta.put("noCache", noCache);
        if (link != null) {
            meta.put("link", link);
        }
        return meta;
    }
    
    private Map<String, Object> createMenuItem(String name, String path, String component, String title, String icon) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("path", path);
        item.put("component", component);
        item.put("meta", createMeta(title, icon, false, null));
        return item;
    }
    
    /**
     * 将数据库菜单转换为RuoYi前端路由格式
     */
    private List<Map<String, Object>> convertToRouters(List<SysMenuEntity> menus) {
        List<Map<String, Object>> routers = new ArrayList<>();
        
        // 获取所有一级菜单（parentId=0）
        List<SysMenuEntity> parentMenus = menus.stream()
                .filter(m -> m.getParentId() == 0)
                .toList();
        
        for (SysMenuEntity parent : parentMenus) {
            Map<String, Object> router = new HashMap<>();
            router.put("name", parent.getRouteName());
            router.put("path", parent.getPath());
            router.put("hidden", "1".equals(parent.getVisible()));
            router.put("component", parent.getComponent());
            router.put("meta", createMeta(
                parent.getMenuName(),
                parent.getIcon(),
                parent.getIsCache() == 1,
                null
            ));
            
            // 获取子菜单
            List<SysMenuEntity> children = menus.stream()
                    .filter(m -> m.getParentId().equals(parent.getMenuId()))
                    .toList();
            
            if (!children.isEmpty()) {
                List<Map<String, Object>> childrenList = new ArrayList<>();
                for (SysMenuEntity child : children) {
                    childrenList.add(createMenuItem(
                        child.getRouteName(),
                        child.getPath(),
                        child.getComponent(),
                        child.getMenuName(),
                        child.getIcon()
                    ));
                }
                router.put("children", childrenList);
            }
            
            routers.add(router);
        }
        
        return routers;
    }
}

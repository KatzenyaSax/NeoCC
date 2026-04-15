package com.dafuweng.auth.controller;

import com.dafuweng.auth.entity.SysUserEntity;
import com.dafuweng.auth.service.SysUserService;
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

    /**
     * 验证码接口 - 放行直接通过（返回模拟验证码）
     */
    @GetMapping("/captchaImage")
    public Result<Map<String, Object>> captchaImage() {
        Map<String, Object> result = new HashMap<>();
        // 模拟返回验证码信息，实际使用时可以接入验证码生成库
        result.put("captchaEnabled", false); // 关闭验证码功能
        result.put("uuid", UUID.randomUUID().toString());
        result.put("img", ""); // 空图片，前端不显示验证码
        return Result.success(result);
    }

    /**
     * 登录接口 - 适配 RuoYi 格式
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        String code = loginRequest.get("code");
        String uuid = loginRequest.get("uuid");
        
        // 调用原有登录逻辑
        SysUserEntity user = sysUserService.login(username, password, "unknown");
        
        // 适配 RuoYi 响应格式
        Map<String, Object> result = new HashMap<>();
        result.put("token", user.getId().toString()); // 使用 userId 作为 token
        result.put("expires_in", 7200); // token 过期时间 2小时
        
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
     * 获取路由菜单 - 适配 RuoYi 格式
     */
    @GetMapping("/getRouters")
    public Result<List<Map<String, Object>>> getRouters(@RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = getUserIdFromToken(token);
        
        List<Map<String, Object>> routers = new ArrayList<>();
        
        // 系统管理菜单
        Map<String, Object> systemMenu = new HashMap<>();
        systemMenu.put("name", "System");
        systemMenu.put("path", "/system");
        systemMenu.put("hidden", false);
        systemMenu.put("component", "Layout");
        systemMenu.put("meta", createMeta("系统管理", "system", false, null));

        List<Map<String, Object>> systemChildren = new ArrayList<>();
        systemChildren.add(createMenuItem("User", "user", "system/user/index", "用户管理", "user"));
        systemChildren.add(createMenuItem("Role", "role", "system/role/index", "角色管理", "peoples"));
        systemChildren.add(createMenuItem("Menu", "menu", "system/menu/index", "菜单管理", "tree-table"));
        systemChildren.add(createMenuItem("Department", "department", "system/department/index", "部门管理", "tree"));
        systemChildren.add(createMenuItem("Zone", "zone", "system/zone/index", "区域管理", "map"));
        systemMenu.put("children", systemChildren);
        routers.add(systemMenu);

        // 销售管理菜单
        Map<String, Object> salesMenu = new HashMap<>();
        salesMenu.put("name", "Sales");
        salesMenu.put("path", "/sales");
        salesMenu.put("hidden", false);
        salesMenu.put("component", "Layout");
        salesMenu.put("meta", createMeta("销售管理", "shopping", false, null));

        List<Map<String, Object>> salesChildren = new ArrayList<>();
        salesChildren.add(createMenuItem("Customer", "customer", "sales/customer/index", "客户管理", "peoples"));
        salesChildren.add(createMenuItem("Contract", "contract", "sales/contract/index", "合同管理", "edit"));
        salesChildren.add(createMenuItem("ContactRecord", "contact-record", "sales/contact-record/index", "跟进记录", "message"));
        salesChildren.add(createMenuItem("WorkLog", "work-log", "sales/work-log/index", "工作日志", "log"));
        salesChildren.add(createMenuItem("PerformanceRecord", "performance-record", "sales/performance-record/index", "业绩记录", "chart"));
        salesChildren.add(createMenuItem("CustomerTransfer", "customer-transfer", "sales/customer-transfer/index", "客户转移记录", "sort"));
        salesMenu.put("children", salesChildren);
        routers.add(salesMenu);

        // 财务管理菜单
        Map<String, Object> financeMenu = new HashMap<>();
        financeMenu.put("name", "Finance");
        financeMenu.put("path", "/finance");
        financeMenu.put("hidden", false);
        financeMenu.put("component", "Layout");
        financeMenu.put("meta", createMeta("财务管理", "money", false, null));

        List<Map<String, Object>> financeChildren = new ArrayList<>();
        financeChildren.add(createMenuItem("LoanAudit", "loan-audit", "finance/loan-audit/index", "贷款审核", "audit"));
        financeChildren.add(createMenuItem("Commission", "commission", "finance/commission/index", "佣金记录", "dollar"));
        financeChildren.add(createMenuItem("ServiceFee", "service-fee", "finance/service-fee/index", "服务费记录", "money"));
        financeChildren.add(createMenuItem("Bank", "bank", "finance/bank/index", "银行管理", "card"));
        financeChildren.add(createMenuItem("FinanceProduct", "product", "finance/product/index", "金融产品", "list"));
        financeMenu.put("children", financeChildren);
        routers.add(financeMenu);
        
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
    
    private Long getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        // 简化处理：token 就是 userId
        try {
            return Long.parseLong(token.replace("Bearer ", ""));
        } catch (NumberFormatException e) {
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
}

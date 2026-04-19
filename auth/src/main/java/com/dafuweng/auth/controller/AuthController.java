package com.dafuweng.auth.controller;

import com.dafuweng.auth.entity.SysPermissionEntity;
import com.dafuweng.auth.entity.SysUserEntity;
import com.dafuweng.auth.service.SysPermissionService;
import com.dafuweng.auth.service.SysRoleService;
import com.dafuweng.auth.service.SysUserService;
import com.dafuweng.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysPermissionService sysPermissionService;

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * POST /auth/login
     * 请求: { username, password }
     * 响应: { code:200, data:{ token:"...", userId:Long, expires:Long } }
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String password = req.get("password");
        SysUserEntity user = sysUserService.login(username, password, "127.0.0.1");
        String token = String.valueOf(user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("expires", System.currentTimeMillis() + 7200000L);
        return Result.success(data);
    }

    /**
     * GET /auth/getInfo
     * Header: Authorization: Bearer <token(userId)>
     * 响应: { code:200, data:{ userId, userName, nickName, avatar, roles:[], permissions:[] } }
     */
    @GetMapping("/getInfo")
    public Result<Map<String, Object>> getInfo(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = extractUserId(auth);
        if (userId == null) {
            return Result.error401("未登录");
        }
        SysUserEntity user = sysUserService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        List<String> roleCodes = sysRoleService.getRoleCodesByUserId(userId);
        List<String> permCodes = sysUserService.getPermCodesByUserId(userId);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("userName", user.getUsername());
        data.put("nickName", user.getRealName());
        data.put("avatar", "");
        data.put("roles", roleCodes.stream().map(c -> "ROLE_" + c).collect(Collectors.toList()));
        data.put("permissions", permCodes);
        return Result.success(data);
    }

    /**
     * GET /auth/getRouters
     * 根据用户权限码，返回 Vue Router 格式菜单树
     * 响应: { code:200, data:[ { path, component, name, meta:{title,icon}, children:[...] } ] }
     */
    @GetMapping("/getRouters")
    public Result<List<Map<String, Object>>> getRouters(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        Long userId = extractUserId(auth);
        if (userId == null) {
            return Result.error401("未登录");
        }

        List<String> permCodes = sysUserService.getPermCodesByUserId(userId);
        List<SysPermissionEntity> allMenus = sysPermissionService.listByStatus((short) 1);

        List<Map<String, Object>> routes = buildRouteTree(allMenus, permCodes);
        return Result.success(routes);
    }

    /**
     * POST /auth/logout
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    // ========== 私有工具方法 ==========

    private Long extractUserId(String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return null;
        }
        try {
            return Long.parseLong(auth.substring(7).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 构建 Vue Router JSON 树，仅返回用户有权限的菜单
     */
    private List<Map<String, Object>> buildRouteTree(
            List<SysPermissionEntity> allMenus, List<String> permCodes) {
        Set<String> codeSet = new HashSet<>(permCodes);
        return allMenus.stream()
            .filter(m -> m.getDeleted() == 0 && m.getStatus() == 1)
            .filter(m -> m.getParentId() == 0 && codeSet.contains(m.getPermCode()))
            .map(parent -> {
                Map<String, Object> node = new HashMap<>();
                node.put("path", permCodeToRoutePath(parent.getPermCode()));
                node.put("component", "Layout");
                node.put("name", parent.getPermCode());
                node.put("meta", Map.of(
                    "title", parent.getPermName(),
                    "icon", parent.getIcon() != null ? parent.getIcon() : ""
                ));
                String parentPermCode = parent.getPermCode();
                List<Map<String, Object>> children = allMenus.stream()
                    .filter(m -> Objects.equals(m.getParentId(), parent.getId()))
                    .filter(m -> m.getDeleted() == 0 && codeSet.contains(m.getPermCode()))
                    .map(child -> {
                        Map<String, Object> c = new HashMap<>();
                        // Child route path: strip parent prefix from child's perm code
                        String childPath = deriveChildPath(parentPermCode, child.getPermCode());
                        c.put("path", childPath);
                        // Each child uses its own component (permCodeToComponent strips _LIST/_ADD etc.)
                        c.put("component", permCodeToComponent(child.getPermCode()));
                        c.put("name", child.getPermCode());
                        c.put("meta", Map.of(
                            "title", child.getPermName(),
                            "icon", child.getIcon() != null ? child.getIcon() : ""
                        ));
                        return c;
                    }).collect(Collectors.toList());
                node.put("children", children);
                return node;
            }).collect(Collectors.toList());
    }

    /** perm_code → Vue Router path */
    private String permCodeToRoutePath(String permCode) {
        String lower = permCode.toLowerCase();
        if (lower.startsWith("system")) return "system";
        if (lower.startsWith("sales")) return "sales";
        if (lower.startsWith("finance")) return "finance";
        return lower.replace("_", "-");
    }

    /**
     * Derive child route path from child/parent perm codes.
     * e.g. parent=SALES_CONTRACT, child=SALES_CONTRACT_LIST → "contract-list"
     *      parent=SALES_CONTRACT, child=SALES_CONTRACT_ADD    → "contract-add"
     */
    private String deriveChildPath(String parentPermCode, String childPermCode) {
        String parentLower = parentPermCode.toLowerCase();
        String childLower = childPermCode.toLowerCase();
        // Strip parent prefix + underscore
        if (childLower.startsWith(parentLower + "_")) {
            String suffix = childLower.substring(parentLower.length() + 1);
            return suffix.replace("_", "-");
        }
        // Fallback: use full child perm code
        return childLower.replace("_", "-");
    }

    /** perm_code → Vue component 路径（相对于 src/views/） */
    private String permCodeToComponent(String permCode) {
        String lower = permCode.toLowerCase();
        // 去掉常见操作后缀，映射到同名实体组件
        // SALES_CONTRACT_LIST / SALES_CONTRACT_ADD / SALES_CONTRACT_EDIT → sales/contract/index
        String[] suffixes = {"_list", "_add", "_edit", "_detail", "_info"};
        for (String s : suffixes) {
            if (lower.endsWith(s)) {
                lower = lower.substring(0, lower.length() - s.length());
                break;
            }
        }
        String[] parts = lower.split("_");
        if (parts.length >= 2) {
            String prefix = parts[0];
            String module = String.join("-", Arrays.copyOfRange(parts, 1, parts.length));
            if ("system".equals(prefix)) return "system/" + module + "/index";
            if ("sales".equals(prefix)) return "sales/" + module + "/index";
            if ("finance".equals(prefix)) return "finance/" + module + "/index";
        }
        return lower.replace("_", "-") + "/index";
    }
}

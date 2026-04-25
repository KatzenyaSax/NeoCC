package com.dafuweng.auth.controller;

import com.dafuweng.auth.entity.SysUserEntity;
import com.dafuweng.auth.service.SysUserService;
import com.dafuweng.auth.vo.UserVO;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sysUser")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/{id}")
    public Result<SysUserEntity> getById(@PathVariable Long id) {
        return Result.success(sysUserService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<SysUserEntity>> pageList(PageRequest request) {
        return Result.success(sysUserService.pageList(request));
    }

    /**
     * 分页查询用户列表（带部门名称）
     */
    @GetMapping("/page/with-dept")
    public Result<PageResponse<UserVO>> pageListWithDeptName(PageRequest request) {
        return Result.success(sysUserService.pageListWithDeptName(request));
    }

    @GetMapping("/{id}/roles")
    public Result<List<Long>> getRoleIds(@PathVariable Long id) {
        return Result.success(sysUserService.getRoleIdsByUserId(id));
    }

    @GetMapping("/{id}/permCodes")
    public Result<List<String>> getPermCodes(@PathVariable Long id) {
        return Result.success(sysUserService.getPermCodesByUserId(id));
    }

    @PostMapping("/login")
    public Result<SysUserEntity> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        String loginIp = loginRequest.getOrDefault("loginIp", "unknown");
        return Result.success(sysUserService.login(username, password, loginIp));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody Map<String, Long> request) {
        sysUserService.logout(request.get("userId"));
        return Result.success();
    }

    @PostMapping
    public Result<SysUserEntity> save(@RequestBody SysUserEntity entity) {
        return Result.success(sysUserService.save(entity));
    }

    @PutMapping
    public Result<SysUserEntity> update(@RequestBody SysUserEntity entity) {
        return Result.success(sysUserService.update(entity));
    }

    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody Map<String, List<Long>> request) {
        sysUserService.assignRoles(id, request.get("roleIds"));
        return Result.success();
    }

    @PutMapping("/{id}/unlock")
    public Result<Void> unlock(@PathVariable Long id) {
        sysUserService.unlock(id);
        return Result.success();
    }

    @PutMapping("/{id}/password")
    public Result<Void> changePassword(@PathVariable Long id, @RequestBody Map<String, String> request) {
        sysUserService.changePassword(id, request.get("oldPassword"), request.get("newPassword"));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.delete(id);
        return Result.success();
    }

    /**
     * 获取所有销售代表列表（下拉用）
     * GET /api/sysUser/sales-reps
     * 支持根据zoneId、deptId或salesRepId过滤
     */
    @GetMapping("/sales-reps")
    public Result<List<SysUserEntity>> listSalesReps(
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long salesRepId) {
        return Result.success(sysUserService.listSalesReps(zoneId, deptId, salesRepId));
    }

    /**
     * GET /api/sysUser/count
     * 获取用户总数
     */
    @GetMapping("/count")
    public Result<Long> count() {
        return Result.success(sysUserService.count());
    }

    /**
     * GET /api/sysUser/ids-by-dept/{deptId}
     * 根据部门ID查询用户ID列表
     */
    @GetMapping("/ids-by-dept/{deptId}")
    public Result<List<Long>> listUserIdsByDeptId(@PathVariable Long deptId) {
        return Result.success(sysUserService.listUserIdsByDeptId(deptId));
    }

    /**
     * GET /api/sysUser/ids-by-zone/{zoneId}
     * 根据战区ID查询用户ID列表
     */
    @GetMapping("/ids-by-zone/{zoneId}")
    public Result<List<Long>> listUserIdsByZoneId(@PathVariable Long zoneId) {
        return Result.success(sysUserService.listUserIdsByZoneId(zoneId));
    }

    /**
     * 根据用户ID列表查询真实姓名
     */
    @PostMapping("/names/by-ids")
    public Result<Map<Long, String>> listUserNamesByIds(@RequestBody List<Long> ids) {
        return Result.success(sysUserService.listRealNamesByIds(ids));
    }

    /**
     * 根据角色ID列表查询用户列表（用于下拉选择）
     */
    @PostMapping("/by-role-ids")
    public Result<List<SysUserEntity>> listByRoleIds(@RequestBody List<Long> roleIds) {
        return Result.success(sysUserService.listByRoleIds(roleIds));
    }

    /**
     * 获取最小可用用户ID（自动分配）
     */
    @GetMapping("/min-available-id")
    public Result<Long> getMinAvailableId() {
        return Result.success(sysUserService.selectMinAvailableId());
    }

    /**
     * 获取最小未使用用户ID
     */
    @GetMapping("/min-unused-id")
    public Result<Long> getMinUnusedId() {
        return Result.success(sysUserService.selectMinAvailableId());
    }

    // ===== 临时调试接口（开发环境用），后续删除 =====
    @PostMapping("/dev/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, Object> req) {
        Long userId = ((Number) req.get("userId")).longValue();
        String newPassword = (String) req.get("newPassword");
        sysUserService.resetPassword(userId, newPassword);
        return Result.success();
    }
}

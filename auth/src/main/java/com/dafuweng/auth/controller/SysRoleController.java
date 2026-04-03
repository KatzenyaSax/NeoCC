package com.dafuweng.auth.controller;

import com.dafuweng.auth.entity.SysRoleEntity;
import com.dafuweng.auth.service.SysRoleService;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sysRole")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @GetMapping("/{id}")
    public Result<SysRoleEntity> getById(@PathVariable Long id) {
        return Result.success(sysRoleService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<SysRoleEntity>> pageList(PageRequest request) {
        return Result.success(sysRoleService.pageList(request));
    }

    @GetMapping("/listByStatus")
    public Result<List<SysRoleEntity>> listByStatus(@RequestParam Short status) {
        return Result.success(sysRoleService.listByStatus(status));
    }

    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getPermissionIds(@PathVariable Long id) {
        return Result.success(sysRoleService.getPermissionIdsByRoleId(id));
    }

    @PostMapping
    public Result<SysRoleEntity> save(@RequestBody SysRoleEntity entity) {
        return Result.success(sysRoleService.save(entity));
    }

    @PutMapping
    public Result<SysRoleEntity> update(@RequestBody SysRoleEntity entity) {
        return Result.success(sysRoleService.update(entity));
    }

    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody Map<String, List<Long>> request) {
        sysRoleService.assignPermissions(id, request.get("permissionIds"));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysRoleService.delete(id);
        return Result.success();
    }
}

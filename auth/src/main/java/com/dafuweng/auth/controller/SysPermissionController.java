package com.dafuweng.auth.controller;

import com.dafuweng.auth.entity.SysPermissionEntity;
import com.dafuweng.auth.service.SysPermissionService;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sysPermission")
public class SysPermissionController {

    @Autowired
    private SysPermissionService sysPermissionService;

    @GetMapping("/{id}")
    public Result<SysPermissionEntity> getById(@PathVariable Long id) {
        return Result.success(sysPermissionService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<SysPermissionEntity>> pageList(PageRequest request) {
        return Result.success(sysPermissionService.pageList(request));
    }

    @GetMapping("/tree")
    public Result<List<SysPermissionEntity>> treeList() {
        return Result.success(sysPermissionService.treeList());
    }

    @GetMapping("/children")
    public Result<List<SysPermissionEntity>> listByParentId(@RequestParam Long parentId) {
        return Result.success(sysPermissionService.listByParentId(parentId));
    }

    @GetMapping("/listByStatus")
    public Result<List<SysPermissionEntity>> listByStatus(@RequestParam Short status) {
        return Result.success(sysPermissionService.listByStatus(status));
    }

    @PostMapping
    public Result<SysPermissionEntity> save(@RequestBody SysPermissionEntity entity) {
        return Result.success(sysPermissionService.save(entity));
    }

    @PutMapping
    public Result<SysPermissionEntity> update(@RequestBody SysPermissionEntity entity) {
        return Result.success(sysPermissionService.update(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysPermissionService.delete(id);
        return Result.success();
    }

    /**
     * 获取最小未使用的权限ID
     */
    @GetMapping("/min-unused-id")
    public Result<Long> getMinUnusedId() {
        return Result.success(sysPermissionService.getMinUnusedId());
    }
}

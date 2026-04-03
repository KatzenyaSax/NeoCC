package com.dafuweng.system.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.system.entity.SysOperationLogEntity;
import com.dafuweng.system.service.SysOperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sysOperationLog")
public class SysOperationLogController {

    @Autowired
    private SysOperationLogService sysOperationLogService;

    @GetMapping("/{id}")
    public Result<SysOperationLogEntity> getById(@PathVariable Long id) {
        return Result.success(sysOperationLogService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<SysOperationLogEntity>> pageList(PageRequest request) {
        return Result.success(sysOperationLogService.pageList(request));
    }

    @GetMapping("/listByUserId/{userId}")
    public Result<List<SysOperationLogEntity>> listByUserId(@PathVariable Long userId) {
        return Result.success(sysOperationLogService.listByUserId(userId));
    }

    @GetMapping("/listByModule/{module}")
    public Result<List<SysOperationLogEntity>> listByModule(@PathVariable String module) {
        return Result.success(sysOperationLogService.listByModule(module));
    }

    @PostMapping
    public Result<SysOperationLogEntity> save(@RequestBody SysOperationLogEntity entity) {
        return Result.success(sysOperationLogService.save(entity));
    }
}

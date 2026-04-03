package com.dafuweng.system.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.system.entity.SysParamEntity;
import com.dafuweng.system.service.SysParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sysParam")
public class SysParamController {

    @Autowired
    private SysParamService sysParamService;

    @GetMapping("/{id}")
    public Result<SysParamEntity> getById(@PathVariable Long id) {
        return Result.success(sysParamService.getById(id));
    }

    @GetMapping("/getByParamKey")
    public Result<SysParamEntity> getByParamKey(@RequestParam String paramKey) {
        return Result.success(sysParamService.getByParamKey(paramKey));
    }

    @GetMapping("/page")
    public Result<PageResponse<SysParamEntity>> pageList(PageRequest request) {
        return Result.success(sysParamService.pageList(request));
    }

    @GetMapping("/listByParamGroup")
    public Result<List<SysParamEntity>> listByParamGroup(@RequestParam String paramGroup) {
        return Result.success(sysParamService.listByParamGroup(paramGroup));
    }

    @PostMapping
    public Result<SysParamEntity> save(@RequestBody SysParamEntity entity) {
        return Result.success(sysParamService.save(entity));
    }

    @PutMapping
    public Result<SysParamEntity> update(@RequestBody SysParamEntity entity) {
        return Result.success(sysParamService.update(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysParamService.delete(id);
        return Result.success();
    }
}

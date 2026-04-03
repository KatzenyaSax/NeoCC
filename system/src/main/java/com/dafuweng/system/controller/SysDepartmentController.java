package com.dafuweng.system.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.system.entity.SysDepartmentEntity;
import com.dafuweng.system.service.SysDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sysDepartment")
public class SysDepartmentController {

    @Autowired
    private SysDepartmentService sysDepartmentService;

    @GetMapping("/{id}")
    public Result<SysDepartmentEntity> getById(@PathVariable Long id) {
        return Result.success(sysDepartmentService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<SysDepartmentEntity>> pageList(PageRequest request) {
        return Result.success(sysDepartmentService.pageList(request));
    }

    @GetMapping("/listByParentId/{parentId}")
    public Result<List<SysDepartmentEntity>> listByParentId(@PathVariable Long parentId) {
        return Result.success(sysDepartmentService.listByParentId(parentId));
    }

    @GetMapping("/listByZoneId/{zoneId}")
    public Result<List<SysDepartmentEntity>> listByZoneId(@PathVariable Long zoneId) {
        return Result.success(sysDepartmentService.listByZoneId(zoneId));
    }

    @PostMapping
    public Result<SysDepartmentEntity> save(@RequestBody SysDepartmentEntity entity) {
        return Result.success(sysDepartmentService.save(entity));
    }

    @PutMapping
    public Result<SysDepartmentEntity> update(@RequestBody SysDepartmentEntity entity) {
        return Result.success(sysDepartmentService.update(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysDepartmentService.delete(id);
        return Result.success();
    }
}

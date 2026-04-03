package com.dafuweng.system.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.system.entity.SysZoneEntity;
import com.dafuweng.system.service.SysZoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sysZone")
public class SysZoneController {

    @Autowired
    private SysZoneService sysZoneService;

    @GetMapping("/{id}")
    public Result<SysZoneEntity> getById(@PathVariable Long id) {
        return Result.success(sysZoneService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<SysZoneEntity>> pageList(PageRequest request) {
        return Result.success(sysZoneService.pageList(request));
    }

    @GetMapping("/listAll")
    public Result<List<SysZoneEntity>> listAll() {
        return Result.success(sysZoneService.listAll());
    }

    @GetMapping("/listByStatus")
    public Result<List<SysZoneEntity>> listByStatus(@RequestParam Short status) {
        return Result.success(sysZoneService.listByStatus(status));
    }

    @PostMapping
    public Result<SysZoneEntity> save(@RequestBody SysZoneEntity entity) {
        return Result.success(sysZoneService.save(entity));
    }

    @PutMapping
    public Result<SysZoneEntity> update(@RequestBody SysZoneEntity entity) {
        return Result.success(sysZoneService.update(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysZoneService.delete(id);
        return Result.success();
    }
}

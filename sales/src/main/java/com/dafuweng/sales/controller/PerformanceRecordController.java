package com.dafuweng.sales.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.sales.entity.PerformanceRecordEntity;
import com.dafuweng.sales.service.PerformanceRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/performanceRecord")
public class PerformanceRecordController {

    @Autowired
    private PerformanceRecordService performanceRecordService;

    @GetMapping("/{id}")
    public Result<PerformanceRecordEntity> getById(@PathVariable Long id) {
        return Result.success(performanceRecordService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<PerformanceRecordEntity>> pageList(PageRequest request) {
        return Result.success(performanceRecordService.pageList(request));
    }

    @GetMapping("/listBySalesRepId/{salesRepId}")
    public Result<List<PerformanceRecordEntity>> listBySalesRepId(@PathVariable Long salesRepId) {
        return Result.success(performanceRecordService.listBySalesRepId(salesRepId));
    }

    @PostMapping
    public Result<PerformanceRecordEntity> save(@RequestBody PerformanceRecordEntity entity) {
        return Result.success(performanceRecordService.save(entity));
    }

    @PutMapping
    public Result<PerformanceRecordEntity> update(@RequestBody PerformanceRecordEntity entity) {
        return Result.success(performanceRecordService.update(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        performanceRecordService.delete(id);
        return Result.success();
    }

    /**
     * PUT /api/performanceRecord/{id}/confirm
     * 确认业绩记录
     */
    @PutMapping("/{id}/confirm")
    public Result<PerformanceRecordEntity> confirm(@PathVariable Long id) {
        return Result.success(performanceRecordService.confirm(id));
    }
}

package com.dafuweng.sales.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.sales.entity.WorkLogEntity;
import com.dafuweng.sales.service.WorkLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workLog")
public class WorkLogController {

    @Autowired
    private WorkLogService workLogService;

    @GetMapping("/{id}")
    public Result<WorkLogEntity> getById(@PathVariable Long id) {
        return Result.success(workLogService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<WorkLogEntity>> pageList(PageRequest request) {
        return Result.success(workLogService.pageList(request));
    }

    @GetMapping("/listBySalesRepId/{salesRepId}")
    public Result<List<WorkLogEntity>> listBySalesRepId(@PathVariable Long salesRepId) {
        return Result.success(workLogService.listBySalesRepId(salesRepId));
    }

    @GetMapping("/checkDuplicate")
    public Result<Boolean> checkDuplicate(@RequestParam Long salesRepId, @RequestParam String logDate) {
        return Result.success(workLogService.isDuplicate(salesRepId, logDate));
    }

    @PostMapping
    public Result<WorkLogEntity> save(@RequestBody WorkLogEntity entity) {
        return Result.success(workLogService.save(entity));
    }

    @PutMapping
    public Result<WorkLogEntity> update(@RequestBody WorkLogEntity entity) {
        return Result.success(workLogService.update(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        workLogService.delete(id);
        return Result.success();
    }
}

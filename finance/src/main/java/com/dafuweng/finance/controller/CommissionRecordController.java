package com.dafuweng.finance.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.finance.entity.CommissionRecordEntity;
import com.dafuweng.finance.service.CommissionRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/commissionRecord")
public class CommissionRecordController {

    @Autowired
    private CommissionRecordService commissionRecordService;

    @GetMapping("/{id}")
    public Result<CommissionRecordEntity> getById(@PathVariable Long id) {
        return Result.success(commissionRecordService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<CommissionRecordEntity>> pageList(PageRequest request) {
        return Result.success(commissionRecordService.pageList(request));
    }

    @GetMapping("/listBySalesRepId/{salesRepId}")
    public Result<List<CommissionRecordEntity>> listBySalesRepId(@PathVariable Long salesRepId) {
        return Result.success(commissionRecordService.listBySalesRepId(salesRepId));
    }

    @PostMapping
    public Result<CommissionRecordEntity> save(@RequestBody CommissionRecordEntity entity) {
        return Result.success(commissionRecordService.save(entity));
    }

    @PutMapping
    public Result<CommissionRecordEntity> update(@RequestBody CommissionRecordEntity entity) {
        return Result.success(commissionRecordService.update(entity));
    }

@DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commissionRecordService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        commissionRecordService.confirm(id);
        return Result.success();
    }

    @PostMapping("/{id}/grant")
    public Result<Void> grant(@PathVariable Long id, @RequestBody Map<String, String> req) {
        commissionRecordService.grant(id, req.get("grantAccount"), req.getOrDefault("remark", ""));
        return Result.success();
    }
}

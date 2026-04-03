package com.dafuweng.finance.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.finance.entity.ServiceFeeRecordEntity;
import com.dafuweng.finance.service.ServiceFeeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/serviceFeeRecord")
public class ServiceFeeRecordController {

    @Autowired
    private ServiceFeeRecordService serviceFeeRecordService;

    @GetMapping("/{id}")
    public Result<ServiceFeeRecordEntity> getById(@PathVariable Long id) {
        return Result.success(serviceFeeRecordService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<ServiceFeeRecordEntity>> pageList(PageRequest request) {
        return Result.success(serviceFeeRecordService.pageList(request));
    }

    @GetMapping("/listByContractId/{contractId}")
    public Result<List<ServiceFeeRecordEntity>> listByContractId(@PathVariable Long contractId) {
        return Result.success(serviceFeeRecordService.listByContractId(contractId));
    }

    @PostMapping
    public Result<ServiceFeeRecordEntity> save(@RequestBody ServiceFeeRecordEntity entity) {
        return Result.success(serviceFeeRecordService.save(entity));
    }

    @PutMapping
    public Result<ServiceFeeRecordEntity> update(@RequestBody ServiceFeeRecordEntity entity) {
        return Result.success(serviceFeeRecordService.update(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        serviceFeeRecordService.delete(id);
        return Result.success();
    }
}

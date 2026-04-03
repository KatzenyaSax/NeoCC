package com.dafuweng.finance.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.finance.entity.FinanceProductEntity;
import com.dafuweng.finance.service.FinanceProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/financeProduct")
public class FinanceProductController {

    @Autowired
    private FinanceProductService financeProductService;

    @GetMapping("/{id}")
    public Result<FinanceProductEntity> getById(@PathVariable Long id) {
        return Result.success(financeProductService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<FinanceProductEntity>> pageList(PageRequest request) {
        return Result.success(financeProductService.pageList(request));
    }

    @GetMapping("/listByBankId/{bankId}")
    public Result<List<FinanceProductEntity>> listByBankId(@PathVariable Long bankId) {
        return Result.success(financeProductService.listByBankId(bankId));
    }

    @GetMapping("/listByStatus")
    public Result<List<FinanceProductEntity>> listByStatus(@RequestParam Short status) {
        return Result.success(financeProductService.listByStatus(status));
    }

    @PostMapping
    public Result<FinanceProductEntity> save(@RequestBody FinanceProductEntity entity) {
        return Result.success(financeProductService.save(entity));
    }

    @PutMapping
    public Result<FinanceProductEntity> update(@RequestBody FinanceProductEntity entity) {
        return Result.success(financeProductService.update(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        financeProductService.delete(id);
        return Result.success();
    }
}

package com.dafuweng.finance.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.finance.entity.BankEntity;
import com.dafuweng.finance.service.BankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bank")
public class BankController {

    @Autowired
    private BankService bankService;

    @GetMapping("/{id}")
    public Result<BankEntity> getById(@PathVariable Long id) {
        return Result.success(bankService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<BankEntity>> pageList(PageRequest request) {
        return Result.success(bankService.pageList(request));
    }

    @GetMapping("/listByStatus")
    public Result<List<BankEntity>> listByStatus(@RequestParam Short status) {
        return Result.success(bankService.listByStatus(status));
    }

    @PostMapping
    public Result<BankEntity> save(@RequestBody BankEntity entity) {
        return Result.success(bankService.save(entity));
    }

    @PutMapping
    public Result<BankEntity> update(@RequestBody BankEntity entity) {
        return Result.success(bankService.update(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        bankService.delete(id);
        return Result.success();
    }

    @GetMapping("/min-unused-id")
    public Result<Long> getMinUnusedId() {
        return Result.success(bankService.getMinUnusedId());
    }
}

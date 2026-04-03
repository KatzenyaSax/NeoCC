package com.dafuweng.finance.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.finance.entity.LoanAuditRecordEntity;
import com.dafuweng.finance.service.LoanAuditRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loanAuditRecord")
public class LoanAuditRecordController {

    @Autowired
    private LoanAuditRecordService loanAuditRecordService;

    @GetMapping("/{id}")
    public Result<LoanAuditRecordEntity> getById(@PathVariable Long id) {
        return Result.success(loanAuditRecordService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<LoanAuditRecordEntity>> pageList(PageRequest request) {
        return Result.success(loanAuditRecordService.pageList(request));
    }

    @GetMapping("/listByLoanAuditId/{loanAuditId}")
    public Result<List<LoanAuditRecordEntity>> listByLoanAuditId(@PathVariable Long loanAuditId) {
        return Result.success(loanAuditRecordService.listByLoanAuditId(loanAuditId));
    }

    @PostMapping
    public Result<LoanAuditRecordEntity> save(@RequestBody LoanAuditRecordEntity entity) {
        return Result.success(loanAuditRecordService.save(entity));
    }
}

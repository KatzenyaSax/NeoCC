package com.dafuweng.finance.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.finance.entity.LoanAuditEntity;
import com.dafuweng.finance.service.LoanAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loanAudit")
public class LoanAuditController {

    @Autowired
    private LoanAuditService loanAuditService;

    @GetMapping("/{id}")
    public Result<LoanAuditEntity> getById(@PathVariable Long id) {
        return Result.success(loanAuditService.getById(id));
    }

    @GetMapping("/getByContractId/{contractId}")
    public Result<LoanAuditEntity> getByContractId(@PathVariable Long contractId) {
        return Result.success(loanAuditService.getByContractId(contractId));
    }

    @GetMapping("/page")
    public Result<PageResponse<LoanAuditEntity>> pageList(PageRequest request) {
        return Result.success(loanAuditService.pageList(request));
    }

    @GetMapping("/listByFinanceSpecialistId/{financeSpecialistId}")
    public Result<List<LoanAuditEntity>> listByFinanceSpecialistId(@PathVariable Long financeSpecialistId) {
        return Result.success(loanAuditService.listByFinanceSpecialistId(financeSpecialistId));
    }

    @PostMapping
    public Result<LoanAuditEntity> save(@RequestBody LoanAuditEntity entity) {
        return Result.success(loanAuditService.save(entity));
    }

    @PutMapping
    public Result<LoanAuditEntity> update(@RequestBody LoanAuditEntity entity) {
        return Result.success(loanAuditService.update(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        loanAuditService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/receive")
    public Result<Void> receive(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        loanAuditService.receive(
            id,
            ((Number) req.get("operatorId")).longValue(),
            (String) req.get("operatorName"),
            (String) req.get("operatorRole"),
            (String) req.getOrDefault("comment", "")
        );
        return Result.success();
    }

    @PostMapping("/{id}/review")
    public Result<Void> review(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        loanAuditService.review(
            id,
            ((Number) req.get("operatorId")).longValue(),
            (String) req.get("operatorName"),
            (String) req.get("operatorRole"),
            (String) req.getOrDefault("comment", "")
        );
        return Result.success();
    }

    @PostMapping("/{id}/submit-bank")
    public Result<Void> submitBank(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        loanAuditService.submitBank(
            id,
            ((Number) req.get("bankId")).longValue(),
            ((Number) req.get("operatorId")).longValue(),
            (String) req.get("operatorName"),
            (String) req.get("operatorRole"),
            (String) req.getOrDefault("comment", "")
        );
        return Result.success();
    }

    @PostMapping("/{id}/bank-result")
    public Result<Void> bankResult(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        loanAuditService.bankResult(
            id,
            (Boolean) req.get("approved"),
            (String) req.get("bankFeedbackContent"),
            ((Number) req.get("operatorId")).longValue(),
            (String) req.get("operatorName"),
            (String) req.get("operatorRole"),
            (String) req.getOrDefault("comment", "")
        );
        return Result.success();
    }

    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        BigDecimal actualLoanAmount = req.get("actualLoanAmount") == null
            ? null : new BigDecimal(req.get("actualLoanAmount").toString());
        BigDecimal actualInterestRate = req.get("actualInterestRate") == null
            ? null : new BigDecimal(req.get("actualInterestRate").toString());
        Date loanGrantedDate = req.get("loanGrantedDate") == null
            ? null : new Date(Long.parseLong(req.get("loanGrantedDate").toString()));
        loanAuditService.approve(
            id,
            ((Number) req.get("operatorId")).longValue(),
            (String) req.get("operatorName"),
            (String) req.get("operatorRole"),
            (String) req.getOrDefault("comment", ""),
            actualLoanAmount,
            actualInterestRate,
            loanGrantedDate
        );
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        loanAuditService.reject(
            id,
            ((Number) req.get("operatorId")).longValue(),
            (String) req.get("operatorName"),
            (String) req.get("operatorRole"),
            (String) req.getOrDefault("comment", "")
        );
        return Result.success();
    }
}

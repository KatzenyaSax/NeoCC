package com.dafuweng.finance.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.common.entity.vo.ContractDetailVO;
import com.dafuweng.finance.service.ContractAuditService;
import com.dafuweng.sales.entity.ContractEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contractAudit")
public class ContractAuditController {

    @Autowired
    private ContractAuditService contractAuditService;

    /**
     * GET /api/finance/contract-audit/page
     * 分页查询审核中的合同（status = 4）
     */
    @GetMapping("/page")
    public Result<PageResponse<ContractEntity>> pageList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(contractAuditService.pageList(pageNum, pageSize, (short) 4));
    }

    /**
     * GET /api/finance/contract-audit/{contractId}/detail
     * 获取合同详情（含关联信息）
     */
    @GetMapping("/{contractId}/detail")
    public Result<ContractDetailVO> getDetail(@PathVariable Long contractId) {
        return Result.success(contractAuditService.getContractDetail(contractId));
    }

    /**
     * POST /api/finance/contract-audit/{contractId}/approve
     * 通过审核，将合同状态从 4 改为 5
     */
    @PostMapping("/{contractId}/approve")
    public Result<Void> approve(@PathVariable Long contractId, @RequestBody Map<String, String> request) {
        String auditOpinion = request.getOrDefault("auditOpinion", "");
        contractAuditService.approve(contractId, auditOpinion);
        return Result.success();
    }

    /**
     * POST /api/finance/contract-audit/{contractId}/reject
     * 拒绝审核，将合同状态从 4 改为 6
     */
    @PostMapping("/{contractId}/reject")
    public Result<Void> reject(@PathVariable Long contractId, @RequestBody Map<String, String> request) {
        String rejectReason = request.getOrDefault("rejectReason", "");
        contractAuditService.reject(contractId, rejectReason);
        return Result.success();
    }
}

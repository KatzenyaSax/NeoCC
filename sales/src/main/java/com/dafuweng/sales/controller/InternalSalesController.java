package com.dafuweng.sales.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.common.entity.dto.PerformanceCreateDTO;
import com.dafuweng.common.entity.vo.ContractVO;
import com.dafuweng.sales.entity.ContractEntity;
import com.dafuweng.sales.entity.PerformanceRecordEntity;
import com.dafuweng.sales.service.ContractService;
import com.dafuweng.sales.service.PerformanceRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sales/internal")
public class InternalSalesController {

    @Autowired
    private PerformanceRecordService performanceRecordService;

    @Autowired
    private ContractService contractService;

    /**
     * 按状态分页查询合同（含关联名称）
     */
    @GetMapping("/contracts/status/{status}/page-with-names")
    public Result<PageResponse<com.dafuweng.sales.entity.vo.ContractDetailVO>> pageContractsByStatusWithNames(
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize") int pageSize,
            @PathVariable("status") Short status) {
        return Result.success(contractService.pageListByStatusWithNames(pageNum, pageSize, status));
    }

    /**
     * 获取合同详情（含关联名称，供内部调用）
     */
    @GetMapping("/contracts/{id}/with-names")
    public Result<com.dafuweng.sales.entity.vo.ContractDetailVO> getContractWithNames(@PathVariable Long id) {
        com.dafuweng.sales.entity.vo.ContractDetailVO vo = contractService.getDetailWithNames(id);
        if (vo == null) {
            return Result.error("合同不存在");
        }
        return Result.success(vo);
    }

    /**
     * POST /sales/internal/performances/create
     * 创建业绩记录（供 finance 服务调用）
     *
     * 幂等性：contract_id 有唯一索引，重复插入会抛 DuplicateKeyException
     */
    @PostMapping("/performances/create")
    public Result<?> createPerformance(@RequestBody PerformanceCreateDTO dto) {
        // 检查是否已存在（幂等保护）
        PerformanceRecordEntity existing = performanceRecordService.getByContractId(dto.getContractId());
        if (existing != null) {
            return Result.error(400, "该合同已创建业绩记录，请勿重复提交");
        }

        PerformanceRecordEntity entity = new PerformanceRecordEntity();
        entity.setContractId(dto.getContractId());
        entity.setSalesRepId(dto.getSalesRepId());
        entity.setDeptId(dto.getDeptId());
        entity.setZoneId(dto.getZoneId());
        entity.setContractAmount(dto.getContractAmount());
        entity.setCommissionRate(dto.getCommissionRate());
        entity.setCommissionAmount(dto.getCommissionAmount());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);  // 默认待计算
        entity.setCalculateTime(dto.getCalculateTime() != null ? dto.getCalculateTime() : new java.util.Date());
        entity.setRemark(dto.getRemark());

        try {
            performanceRecordService.save(entity);
            return Result.success(entity);
        } catch (DuplicateKeyException e) {
            return Result.error(400, "该合同已创建业绩记录，幂等冲突");
        }
    }

    /**
     * GET /sales/internal/contracts/{id}
     * 查询合同详情（供 finance 服务调用）
     */
    @GetMapping("/contracts/{id}")
    public Result<ContractVO> getContract(@PathVariable Long id) {
        ContractEntity entity = contractService.getById(id);
        if (entity == null) {
            return Result.error("合同不存在");
        }
        ContractVO vo = new ContractVO();
        vo.setId(entity.getId());
        vo.setContractNo(entity.getContractNo());
        vo.setCustomerId(entity.getCustomerId());
        vo.setSalesRepId(entity.getSalesRepId());
        vo.setDeptId(entity.getDeptId());
        vo.setProductId(entity.getProductId());
        vo.setZoneId(entity.getZoneId());
        vo.setContractAmount(entity.getContractAmount());
        vo.setActualLoanAmount(entity.getActualLoanAmount());
        vo.setServiceFeeRate(entity.getServiceFeeRate());
        vo.setServiceFee1(entity.getServiceFee1());
        vo.setServiceFee2(entity.getServiceFee2());
        vo.setServiceFee1Paid(entity.getServiceFee1Paid());
        vo.setServiceFee2Paid(entity.getServiceFee2Paid());
        vo.setStatus(entity.getStatus());
        vo.setSignDate(entity.getSignDate());
        vo.setPaperContractNo(entity.getPaperContractNo());
        vo.setLoanUse(entity.getLoanUse());
        vo.setRejectReason(entity.getRejectReason());
        vo.setRemark(entity.getRemark());
        return Result.success(vo);
    }

    /**
     * PUT /sales/internal/contracts/{id}/status
     * 更新合同状态（供 finance 服务调用）
     *
     * finance 审核流程中：
     * - 合同签署后 status=2（已签署）→ 发送金融部后 status=4（已发送金融部）
     * - 银行放款后 status=7（已放款）
     */
    @PutMapping("/contracts/{id}/status")
    public Result<?> updateContractStatus(@PathVariable Long id, @RequestParam Short status) {
        ContractEntity entity = contractService.getById(id);
        if (entity == null) {
            return Result.error("合同不存在");
        }
        entity.setStatus(status);
        contractService.update(entity);
        return Result.success();
    }

    /**
     * PUT /sales/internal/contracts/{id}/service-fee-paid
     * 更新合同服务费支付状态（供 finance 服务调用）
     *
     * @param feeType 1=首期服务费已付 2=二期服务费已付
     */
    @PutMapping("/contracts/{id}/service-fee-paid")
    public Result<?> updateServiceFeePaid(@PathVariable Long id, @RequestParam Short feeType) {
        ContractEntity entity = contractService.getById(id);
        if (entity == null) {
            return Result.error("合同不存在");
        }
        if (feeType == 1) {
            entity.setServiceFee1Paid((short) 1);
        } else if (feeType == 2) {
            entity.setServiceFee2Paid((short) 1);
        }
        contractService.update(entity);
        return Result.success();
    }

    /**
     * GET /sales/internal/contracts/status/{status}/page
     * 按状态分页查询合同
     */
    @GetMapping("/contracts/status/{status}/page")
    public Result<PageResponse<ContractEntity>> pageContractsByStatus(
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize") int pageSize,
            @PathVariable("status") Short status) {
        return Result.success(contractService.pageListByStatus(pageNum, pageSize, status));
    }

    /**
     * PUT /sales/internal/contracts/{id}/status-with-reason
     * 更新合同状态并设置拒绝原因（供 finance 服务调用）
     */
    @PutMapping("/contracts/{id}/status-with-reason")
    public Result<?> updateContractStatusWithReason(@PathVariable Long id,
                                                     @RequestParam Short status,
                                                     @RequestParam(required = false) String rejectReason) {
        ContractEntity entity = contractService.getById(id);
        if (entity == null) {
            return Result.error("合同不存在");
        }
        entity.setStatus(status);
        if (rejectReason != null && !rejectReason.isEmpty()) {
            entity.setRejectReason(rejectReason);
        }
        contractService.update(entity);
        return Result.success();
    }
}

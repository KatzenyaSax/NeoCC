package com.dafuweng.sales.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.sales.entity.ContractEntity;
import com.dafuweng.sales.service.ContractService;
import com.dafuweng.sales.service.ContractSignService;
import com.dafuweng.sales.service.PerformanceRecordService;
import com.dafuweng.sales.feign.FinanceFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contract")
public class ContractController {

    private static final Logger log = LoggerFactory.getLogger(ContractController.class);
    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.10");

    @Autowired
    private ContractService contractService;

    @Autowired
    private ContractSignService contractSignService;

    @Autowired
    private PerformanceRecordService performanceRecordService;

    @Autowired
    private FinanceFeignClient financeFeignClient;

    @GetMapping("/{id}")
    public Result<ContractEntity> getById(@PathVariable Long id) {
        return Result.success(contractService.getById(id));
    }

    @GetMapping("/getByContractNo/{contractNo}")
    public Result<ContractEntity> getByContractNo(@PathVariable String contractNo) {
        return Result.success(contractService.getByContractNo(contractNo));
    }

    @GetMapping("/page")
    public Result<PageResponse<ContractEntity>> pageList(PageRequest request) {
        return Result.success(contractService.pageList(request));
    }

    @GetMapping("/listBySalesRepId/{salesRepId}")
    public Result<List<ContractEntity>> listBySalesRepId(@PathVariable Long salesRepId) {
        return Result.success(contractService.listBySalesRepId(salesRepId));
    }

    @GetMapping("/listByStatus")
    public Result<List<ContractEntity>> listByStatus(@RequestParam Short status) {
        return Result.success(contractService.listByStatus(status));
    }

    @PostMapping
    public Result<ContractEntity> save(@RequestBody ContractEntity entity) {
        return Result.success(contractService.save(entity));
    }

    @PutMapping
    public Result<ContractEntity> update(@RequestBody ContractEntity entity) {
        return Result.success(contractService.update(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        contractService.delete(id);
        return Result.success();
    }

    /**
     * POST /api/contract/{id}/sign
     * 签署合同并发送事件通知金融部
     * 签署成功后自动生成业绩记录
     */
    @PostMapping("/{id}/sign")
    public Result<Void> sign(@PathVariable Long id) {
        log.info("签署合同开始，contractId={}", id);
        contractSignService.sign(id);
        // 签署成功后自动生成业绩记录
        var performance = performanceRecordService.generateFromContract(id);
        if (performance != null) {
            log.info("业绩记录自动生成成功，id={}", performance.getId());
        }
        return Result.success();
    }

    /**
     * GET /api/contract/generateNo
     * 生成新合同编号
     */
    @GetMapping("/generateNo")
    public Result<String> generateNo() {
        return Result.success(contractService.generateContractNo());
    }

    /**
     * GET /api/contract/{id}/detail
     * 获取合同详情（含关联信息）
     */
    @GetMapping("/{id}/detail")
    public Result<ContractEntity> getDetail(@PathVariable Long id) {
        return Result.success(contractService.getDetail(id));
    }

    /**
     * GET /api/contract/{id}/detail-with-rate
     * 获取合同详情（含关联信息和佣金比例）
     */
    @GetMapping("/{id}/detail-with-rate")
    public Result<Map<String, Object>> getDetailWithRate(@PathVariable Long id) {
        ContractEntity contract = contractService.getDetail(id);
        if (contract == null) {
            return Result.error("合同不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("contractId", contract.getId());
        result.put("contractNo", contract.getContractNo());
        result.put("contractAmount", contract.getContractAmount());
        result.put("salesRepId", contract.getSalesRepId());
        result.put("deptId", contract.getDeptId());
        result.put("zoneId", contract.getZoneId());
        result.put("productId", contract.getProductId());

        // 获取产品佣金比例
        BigDecimal commissionRate = getProductCommissionRate(contract.getProductId());
        result.put("commissionRate", commissionRate);

        // 计算佣金金额
        BigDecimal commissionAmount = BigDecimal.ZERO;
        if (contract.getContractAmount() != null) {
            commissionAmount = contract.getContractAmount().multiply(commissionRate).setScale(2, RoundingMode.HALF_UP);
        }
        result.put("commissionAmount", commissionAmount);

        return Result.success(result);
    }

    /**
     * 获取产品佣金比例
     */
    private BigDecimal getProductCommissionRate(Long productId) {
        if (productId == null) {
            return DEFAULT_COMMISSION_RATE;
        }
        try {
            var res = financeFeignClient.getProductById(productId);
            if (res != null && res.getCode() == 200 && res.getData() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> product = (Map<String, Object>) res.getData();
                Object rate = product.get("commissionRate");
                if (rate != null) {
                    return new BigDecimal(rate.toString());
                }
            }
        } catch (Exception e) {
            log.warn("获取产品[{}]佣金比例失败: {}", productId, e.getMessage());
        }
        return DEFAULT_COMMISSION_RATE;
    }
}

package com.dafuweng.sales.feign;

import com.dafuweng.common.entity.Result;
import com.dafuweng.finance.entity.CommissionRecordEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "finance", contextId = "financeProductClientForSales", url = "http://localhost:8084")
public interface FinanceFeignClient {

    /**
     * 根据产品ID查询金融产品详情
     */
    @GetMapping("/api/financeProduct/{id}")
    Result<?> getById(@PathVariable Long id);

    /**
     * 创建提成记录
     */
    @PostMapping("/api/commissionRecord")
    Result<CommissionRecordEntity> createCommissionRecord(@RequestBody CommissionRecordEntity entity);

    /**
     * 获取最小未使用提成记录ID
     */
    @GetMapping("/api/commissionRecord/min-unused-id")
    Result<Long> getMinUnusedCommissionRecordId();
}
package com.dafuweng.sales.feign;

import com.dafuweng.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    Result<?> createCommissionRecord(@RequestBody Map<String, Object> record);

    /**
     * 获取最小未使用提成记录ID
     */
    @GetMapping("/api/commissionRecord/min-unused-id")
    Result<Long> getMinUnusedCommissionRecordId();

    /**
     * 创建服务费记录
     */
    @PostMapping("/api/serviceFeeRecord")
    Result<?> createServiceFeeRecord(@RequestBody Map<String, Object> record);

    /**
     * 获取最小未使用服务费记录ID
     */
    @GetMapping("/api/serviceFeeRecord/min-unused-id")
    Result<Long> getMinUnusedServiceFeeRecordId();
}
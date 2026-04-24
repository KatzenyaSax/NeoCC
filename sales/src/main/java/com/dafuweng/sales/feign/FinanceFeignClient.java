package com.dafuweng.sales.feign;

import com.dafuweng.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "finance", contextId = "financeProductClientForSales", url = "http://localhost:8084")
public interface FinanceFeignClient {

    /**
     * 根据产品ID查询金融产品详情
     */
    @GetMapping("/api/financeProduct/{id}")
    Result<?> getById(@PathVariable Long id);
}
package com.dafuweng.sales.feign;

import com.dafuweng.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Finance 服务 Feign Client
 * 用于 sales 模块调用 finance 服务的接口
 */
@FeignClient(name = "dafuweng-finance", contextId = "financeClientForSales", url = "http://localhost:8084")
public interface FinanceFeignClient {

    /**
     * 根据产品ID获取金融产品信息
     */
    @GetMapping("/api/financeProduct/{id}")
    Result<?> getProductById(@PathVariable Long id);
}

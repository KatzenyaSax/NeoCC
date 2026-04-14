package com.dafuweng.finance.feign;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.dto.PerformanceCreateDTO;
import com.dafuweng.common.entity.vo.ContractVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "dafuweng-sales", contextId = "salesClient")
public interface SalesFeignClient {
    @PostMapping("/sales/internal/performances/create")
    Result<?> createPerformance(@RequestBody PerformanceCreateDTO dto);

    @PutMapping("/sales/internal/contracts/{id}/status")
    Result<?> updateContractStatus(@PathVariable Long id, @RequestParam Short status);

    @GetMapping("/sales/internal/contracts/{id}")
    Result<ContractVO> getContract(@PathVariable Long id);

    @PutMapping("/sales/internal/contracts/{id}/service-fee-paid")
    Result<?> updateServiceFeePaid(@PathVariable Long id,
                                   @RequestParam("feeType") Short feeType);
}
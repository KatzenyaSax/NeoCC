package com.dafuweng.finance.feign;

import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.dto.PerformanceCreateDTO;
import com.dafuweng.common.entity.vo.ContractVO;
import com.dafuweng.sales.entity.ContractEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "sales", contextId = "salesClient")
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

    @GetMapping("/sales/internal/contracts/status/{status}/page")
    Result<PageResponse<ContractEntity>> pageContractsByStatus(@RequestParam("pageNum") int pageNum,
                                                               @RequestParam("pageSize") int pageSize,
                                                               @PathVariable("status") Short status);

    @GetMapping("/sales/internal/contracts/status/{status}/page-with-names")
    Result<PageResponse<com.dafuweng.sales.entity.vo.ContractDetailVO>> pageContractsByStatusWithNames(
            @RequestParam("pageNum") int pageNum,
            @RequestParam("pageSize") int pageSize,
            @PathVariable("status") Short status);

    @GetMapping("/sales/internal/contracts/{id}/with-names")
    Result<com.dafuweng.sales.entity.vo.ContractDetailVO> getContractWithNames(@PathVariable("id") Long id);

    @GetMapping("/sales/internal/contracts/{id}")
    Result<ContractVO> getContractById(@PathVariable Long id);

    @PutMapping("/sales/internal/contracts/{id}/status-with-reason")
    Result<?> updateContractStatusWithReason(@PathVariable Long id,
                                            @RequestParam Short status,
                                            @RequestParam(required = false) String rejectReason);
}
package com.dafuweng.sales.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.sales.entity.ContractEntity;
import com.dafuweng.sales.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contract")
public class ContractController {

    @Autowired
    private ContractService contractService;

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
}

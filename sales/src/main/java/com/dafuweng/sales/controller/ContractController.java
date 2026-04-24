package com.dafuweng.sales.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.sales.entity.ContractEntity;
import com.dafuweng.sales.service.ContractService;
import com.dafuweng.sales.service.ContractSignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contract")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @Autowired
    private ContractSignService contractSignService;

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
     */
    @PostMapping("/{id}/sign")
    public Result<Void> sign(@PathVariable Long id) {
        contractSignService.sign(id);
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
     * GET /api/contract/{id}/detail-with-names
     * 获取合同详情（含关联名称）
     */
    @GetMapping("/{id}/detail-with-names")
    public Result<?> getDetailWithNames(@PathVariable Long id) {
        return Result.success(contractService.getDetailWithNames(id));
    }

    /**
     * GET /api/contract/count
     * 获取合同总数
     */
    @GetMapping("/count")
    public Result<Long> count() {
        return Result.success(contractService.count());
    }

    /**
     * GET /api/contract/count-by-status?status=4
     * 按状态获取合同数量
     */
    @GetMapping("/count-by-status")
    public Result<Long> countByStatus(@RequestParam Short status) {
        return Result.success(contractService.countByStatus(status));
    }
}

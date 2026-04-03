package com.dafuweng.sales.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.sales.entity.ContractAttachmentEntity;
import com.dafuweng.sales.service.ContractAttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contractAttachment")
public class ContractAttachmentController {

    @Autowired
    private ContractAttachmentService contractAttachmentService;

    @GetMapping("/{id}")
    public Result<ContractAttachmentEntity> getById(@PathVariable Long id) {
        return Result.success(contractAttachmentService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<ContractAttachmentEntity>> pageList(PageRequest request) {
        return Result.success(contractAttachmentService.pageList(request));
    }

    @GetMapping("/listByContractId/{contractId}")
    public Result<List<ContractAttachmentEntity>> listByContractId(@PathVariable Long contractId) {
        return Result.success(contractAttachmentService.listByContractId(contractId));
    }

    @PostMapping
    public Result<ContractAttachmentEntity> save(@RequestBody ContractAttachmentEntity entity) {
        return Result.success(contractAttachmentService.save(entity));
    }

    @PutMapping
    public Result<ContractAttachmentEntity> update(@RequestBody ContractAttachmentEntity entity) {
        return Result.success(contractAttachmentService.update(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        contractAttachmentService.delete(id);
        return Result.success();
    }
}

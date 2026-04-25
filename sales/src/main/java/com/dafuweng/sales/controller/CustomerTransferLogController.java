package com.dafuweng.sales.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.sales.entity.CustomerTransferLogEntity;
import com.dafuweng.sales.service.CustomerTransferLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customerTransferLog")
public class CustomerTransferLogController {

    @Autowired
    private CustomerTransferLogService customerTransferLogService;

    @GetMapping("/{id}")
    public Result<CustomerTransferLogEntity> getById(@PathVariable Long id) {
        return Result.success(customerTransferLogService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<CustomerTransferLogEntity>> pageList(PageRequest request) {
        return Result.success(customerTransferLogService.pageList(request));
    }

    @GetMapping("/listByCustomerId/{customerId}")
    public Result<List<CustomerTransferLogEntity>> listByCustomerId(@PathVariable Long customerId) {
        return Result.success(customerTransferLogService.listByCustomerId(customerId));
    }

    @PostMapping
    public Result<CustomerTransferLogEntity> save(@RequestBody CustomerTransferLogEntity entity) {
        return Result.success(customerTransferLogService.save(entity));
    }

    @PutMapping
    public Result<CustomerTransferLogEntity> update(@RequestBody CustomerTransferLogEntity entity) {
        return Result.success(customerTransferLogService.update(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerTransferLogService.delete(id);
        return Result.success();
    }

    @GetMapping("/min-unused-id")
    public Result<Long> getMinUnusedId() {
        return Result.success(customerTransferLogService.getMinUnusedId());
    }
}

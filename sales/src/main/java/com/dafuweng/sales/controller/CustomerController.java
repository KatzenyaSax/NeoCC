package com.dafuweng.sales.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.sales.entity.CustomerEntity;
import com.dafuweng.sales.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/{id}")
    public Result<CustomerEntity> getById(@PathVariable Long id) {
        return Result.success(customerService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<CustomerEntity>> pageList(PageRequest request,
            @RequestParam(required = false) String filterRole,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long zoneId) {
        return Result.success(customerService.pageList(request, filterRole, userId, deptId, zoneId));
    }

    @GetMapping("/listBySalesRepId/{salesRepId}")
    public Result<List<CustomerEntity>> listBySalesRepId(@PathVariable Long salesRepId) {
        return Result.success(customerService.listBySalesRepId(salesRepId));
    }

    @GetMapping("/listByStatus")
    public Result<List<CustomerEntity>> listByStatus(@RequestParam Short status) {
        return Result.success(customerService.listByStatus(status));
    }

    @PostMapping
    public Result<CustomerEntity> save(@RequestBody CustomerEntity entity) {
        return Result.success(customerService.save(entity));
    }

    @PutMapping
    public Result<CustomerEntity> update(@RequestBody CustomerEntity entity) {
        return Result.success(customerService.update(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return Result.success();
    }

    /**
     * GET /api/customer/count
     * 获取客户总数
     */
    @GetMapping("/count")
    public Result<Long> count() {
        return Result.success(customerService.count());
    }
}

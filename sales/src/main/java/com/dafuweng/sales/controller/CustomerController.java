package com.dafuweng.sales.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.sales.entity.CustomerEntity;
import com.dafuweng.sales.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    /**
     * 获取销售代表列表（下拉用）
     * GET /api/customer/sales-reps
     */
    @GetMapping("/sales-reps")
    public Result<?> listSalesReps(
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long salesRepId) {
        return Result.success(customerService.listSalesReps(zoneId, deptId, salesRepId));
    }

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

    /**
     * 批量查询客户名称
     * POST /api/customer/names/by-ids
     */
    @PostMapping("/names/by-ids")
    public Result<Map<Long, String>> getCustomerNamesByIds(@RequestBody List<Long> ids) {
        Map<Long, String> nameMap = customerService.getCustomerNamesByIds(ids);
        return Result.success(nameMap);
    }

    /**
     * 获取最小未使用的客户ID
     * GET /api/customer/min-unused-id
     */
    @GetMapping("/min-unused-id")
    public Result<Long> getMinUnusedId() {
        return Result.success(customerService.getMinUnusedId());
    }
}

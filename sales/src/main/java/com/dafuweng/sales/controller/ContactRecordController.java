package com.dafuweng.sales.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.sales.entity.ContactRecordEntity;
import com.dafuweng.sales.feign.AuthFeignClient;
import com.dafuweng.sales.service.ContactRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contactRecord")
public class ContactRecordController {

    @Autowired
    private ContactRecordService contactRecordService;

    @Autowired
    private AuthFeignClient authFeignClient;

    @GetMapping("/{id}")
    public Result<ContactRecordEntity> getById(@PathVariable Long id) {
        return Result.success(contactRecordService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<ContactRecordEntity>> pageList(PageRequest request) {
        return Result.success(contactRecordService.pageList(request));
    }

    /**
     * 按角色条件查询跟进记录列表
     */
    @GetMapping("/list-by-role")
    public Result<List<ContactRecordEntity>> listByRoleConditions(
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long salesRepId) {
        // 先查询符合条件的销售代表ID列表
        Result<List<Map<String, Object>>> salesRepsResult = authFeignClient.listSalesReps(zoneId, deptId, salesRepId);
        List<Map<String, Object>> salesReps = (List<Map<String, Object>>) salesRepsResult.getData();
        List<Long> salesRepIds = salesReps.stream()
                .map(reps -> ((Number) reps.get("id")).longValue())
                .collect(java.util.stream.Collectors.toList());

        // 根据销售代表ID列表查询跟进记录
        return Result.success(contactRecordService.listBySalesRepIds(salesRepIds));
    }

    @GetMapping("/listByCustomerId/{customerId}")
    public Result<List<ContactRecordEntity>> listByCustomerId(@PathVariable Long customerId) {
        return Result.success(contactRecordService.listByCustomerId(customerId));
    }

    @GetMapping("/listBySalesRepId/{salesRepId}")
    public Result<List<ContactRecordEntity>> listBySalesRepId(@PathVariable Long salesRepId) {
        return Result.success(contactRecordService.listBySalesRepId(salesRepId));
    }

    @GetMapping("/listBySalesRepIds")
    public Result<List<ContactRecordEntity>> listBySalesRepIds(@RequestParam List<Long> salesRepIds) {
        return Result.success(contactRecordService.listBySalesRepIds(salesRepIds));
    }

    @PostMapping
    public Result<ContactRecordEntity> save(@RequestBody ContactRecordEntity entity) {
        return Result.success(contactRecordService.save(entity));
    }

    @PutMapping
    public Result<ContactRecordEntity> update(@RequestBody ContactRecordEntity entity) {
        return Result.success(contactRecordService.update(entity));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        contactRecordService.delete(id);
        return Result.success();
    }
}

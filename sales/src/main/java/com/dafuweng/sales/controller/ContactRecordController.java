package com.dafuweng.sales.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.PageResponse;
import com.dafuweng.sales.entity.ContactRecordEntity;
import com.dafuweng.sales.service.ContactRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contactRecord")
public class ContactRecordController {

    @Autowired
    private ContactRecordService contactRecordService;

    @GetMapping("/{id}")
    public Result<ContactRecordEntity> getById(@PathVariable Long id) {
        return Result.success(contactRecordService.getById(id));
    }

    @GetMapping("/page")
    public Result<PageResponse<ContactRecordEntity>> pageList(PageRequest request) {
        return Result.success(contactRecordService.pageList(request));
    }

    @GetMapping("/listByCustomerId/{customerId}")
    public Result<List<ContactRecordEntity>> listByCustomerId(@PathVariable Long customerId) {
        return Result.success(contactRecordService.listByCustomerId(customerId));
    }

    @GetMapping("/listBySalesRepId/{salesRepId}")
    public Result<List<ContactRecordEntity>> listBySalesRepId(@PathVariable Long salesRepId) {
        return Result.success(contactRecordService.listBySalesRepId(salesRepId));
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

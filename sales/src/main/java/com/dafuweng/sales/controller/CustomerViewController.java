package com.dafuweng.sales.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.sales.service.CustomerViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerViewController {

    @Autowired
    private CustomerViewService customerViewService;

    /**
     * 客户详情（聚合所有相关数据）
     * GET /api/customer/view/{id}
     *
     * @param id 客户ID
     */
    @GetMapping("/view/{id}")
    public Result<?> view(@PathVariable Long id) {
        return Result.success(customerViewService.getCustomerView(id));
    }
}

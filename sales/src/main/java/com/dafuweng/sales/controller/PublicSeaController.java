package com.dafuweng.sales.controller;

import com.dafuweng.common.entity.PageRequest;
import com.dafuweng.common.entity.Result;
import com.dafuweng.sales.service.PublicSeaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customer")
public class PublicSeaController {

    @Autowired
    private PublicSeaService publicSeaService;

    /**
     * 公海客户分页列表
     * GET /api/customer/public-sea/page
     */
    @GetMapping("/public-sea/page")
    public Result<?> publicSeaPage(PageRequest request) {
        return Result.success(publicSeaService.pageList(request));
    }

    /**
     * 转移公海客户
     * PUT /api/customer/public-sea/transfer
     *
     * @param req { customerId, toRepId, reason, operatorId }
     */
    @PutMapping("/public-sea/transfer")
    public Result<?> transfer(@RequestBody Map<String, Object> req) {
        Long customerId = ((Number) req.get("customerId")).longValue();
        Long toRepId = ((Number) req.get("toRepId")).longValue();
        String reason = (String) req.getOrDefault("reason", "");
        Long operatorId = ((Number) req.get("operatorId")).longValue();
        publicSeaService.transfer(customerId, toRepId, reason, operatorId);
        return Result.success("转移成功");
    }

    /**
     * 获取销售代表列表
     * GET /api/customer/sales-reps
     */
    @GetMapping("/sales-reps")
    public Result<?> listSalesReps() {
        return Result.success(publicSeaService.listSalesReps());
    }
}

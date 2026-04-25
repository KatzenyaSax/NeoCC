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
     * @param req { customerId, toRepId, reason, operatorId, operateType }
     */
    @PutMapping("/public-sea/transfer")
    public Result<?> transfer(@RequestBody Map<String, Object> req) {
        Long customerId = ((Number) req.get("customerId")).longValue();
        Long toRepId = ((Number) req.get("toRepId")).longValue();
        String reason = (String) req.getOrDefault("reason", "");
        Long operatorId = ((Number) req.get("operatorId")).longValue();
        String operateType = (String) req.getOrDefault("operateType", "ASSIGN");
        publicSeaService.transfer(customerId, toRepId, reason, operatorId, operateType);
        return Result.success("转移成功");
    }

    /**
     * 获取销售代表列表（公海客户使用）
     * GET /api/customer/public-sea/sales-reps
     */
    @GetMapping("/public-sea/sales-reps")
    public Result<?> listSalesReps(
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long salesRepId) {
        return Result.success(publicSeaService.listSalesReps(zoneId, deptId, salesRepId));
    }
}

package com.dafuweng.sales.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.sales.service.PerfSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 业绩汇总 Controller
 * 提供按维度聚合的业绩统计接口
 */
@RestController
@RequestMapping("/api/perfSummary")
public class PerfSummaryController {

    @Autowired
    private PerfSummaryService perfSummaryService;

    /**
     * 业绩汇总（按维度分组）
     *
     * @param beginTime  统计开始日期，格式：yyyy-MM-dd（可选）
     * @param endTime    统计结束日期，格式：yyyy-MM-dd（可选）
     * @param deptId     部门ID筛选（可选）
     * @param zoneId     战区ID筛选（可选）
     * @param groupBy    聚合维度：
     *                   - sales_rep  按销售人员
     *                   - dept       按部门
     *                   - zone       按战区
     *                   - month      按月份
     * @return 汇总数据
     */
    @GetMapping("/summary")
    public Result<Map<String, Object>> summary(
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(required = false, defaultValue = "sales_rep") String groupBy
    ) {
        return Result.success(perfSummaryService.summary(beginTime, endTime, deptId, zoneId, groupBy));
    }
}

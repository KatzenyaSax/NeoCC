package com.dafuweng.sales.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.sales.service.PerfRankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/perfRanking")
public class PerfRankingController {

    @Autowired
    private PerfRankingService perfRankingService;

    /**
     * 业绩排名列表
     * GET /api/perfRanking/list
     *
     * @param beginTime   统计开始日期（yyyy-MM-dd）
     * @param endTime     统计结束日期（yyyy-MM-dd）
     * @param deptId      部门维度筛选（可选）
     * @param zoneId      战区维度筛选（可选）
     * @param rankingType 排名类型：commission（按提成）/ contract_amount（按合同额）/ count（按合同数）
     * @param limit       返回前 N 名，默认 20
     */
    @GetMapping("/list")
    public Result<?> list(
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long zoneId,
            @RequestParam(defaultValue = "contract_amount") String rankingType,
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        return Result.success(perfRankingService.getRankingList(
                beginTime, endTime, deptId, zoneId, rankingType, limit));
    }
}

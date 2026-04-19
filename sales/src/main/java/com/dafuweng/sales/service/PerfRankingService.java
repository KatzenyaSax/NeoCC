package com.dafuweng.sales.service;

import java.util.Map;

public interface PerfRankingService {

    /**
     * 获取业绩排名列表
     *
     * @param beginTime   开始时间（yyyy-MM-dd，可为 null 表示不限）
     * @param endTime     结束时间（yyyy-MM-dd，可为 null 表示不限）
     * @param deptId      部门筛选（可选）
     * @param zoneId      战区筛选（可选）
     * @param rankingType 排名类型：commission / contract_amount / count
     * @param limit       返回条数限制
     * @return 包含 rankings 列表和 totalSalesReps 的 Map
     */
    Map<String, Object> getRankingList(String beginTime, String endTime,
            Long deptId, Long zoneId, String rankingType, Integer limit);
}

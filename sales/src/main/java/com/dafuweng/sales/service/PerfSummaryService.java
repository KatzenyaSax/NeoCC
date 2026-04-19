package com.dafuweng.sales.service;

import java.util.Map;

/**
 * 业绩汇总 Service 接口
 */
public interface PerfSummaryService {

    /**
     * 按维度聚合业绩数据
     *
     * @param beginTime 统计开始日期
     * @param endTime   统计结束日期
     * @param deptId    部门ID筛选（可选）
     * @param zoneId    战区ID筛选（可选）
     * @param groupBy   聚合维度：sales_rep / dept / zone / month
     * @return 汇总结果
     */
    Map<String, Object> summary(String beginTime, String endTime, Long deptId, Long zoneId, String groupBy);
}

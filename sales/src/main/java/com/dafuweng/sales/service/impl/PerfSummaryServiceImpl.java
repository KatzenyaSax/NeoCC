package com.dafuweng.sales.service.impl;

import com.dafuweng.sales.dao.PerformanceRecordDao;
import com.dafuweng.sales.entity.PerformanceRecordEntity;
import com.dafuweng.sales.service.PerfSummaryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * 业绩汇总 Service 实现
 */
@Service
public class PerfSummaryServiceImpl implements PerfSummaryService {

    @Autowired
    private PerformanceRecordDao performanceRecordDao;

    @Override
    public Map<String, Object> summary(String beginTime, String endTime,
                                         Long deptId, Long zoneId, String groupBy) {
        // ========== 1. 构建查询条件 ==========
        LambdaQueryWrapper<PerformanceRecordEntity> wrapper = new LambdaQueryWrapper<>();

        // 只查询已确认的业绩（status = 2 已确认）
        wrapper.eq(PerformanceRecordEntity::getStatus, 2);

        // 时间范围筛选（按 calculate_time）
        if (StringUtils.hasText(beginTime)) {
            wrapper.ge(PerformanceRecordEntity::getCalculateTime, beginTime + " 00:00:00");
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le(PerformanceRecordEntity::getCalculateTime, endTime + " 23:59:59");
        }

        // 部门筛选
        if (deptId != null) {
            wrapper.eq(PerformanceRecordEntity::getDeptId, deptId);
        }

        // 战区筛选
        if (zoneId != null) {
            wrapper.eq(PerformanceRecordEntity::getZoneId, zoneId);
        }

        // ========== 2. 查询全部符合条件的数据（用于内存聚合） ==========
        List<PerformanceRecordEntity> records = performanceRecordDao.selectList(wrapper);

        // ========== 3. 计算总计 ==========
        BigDecimal totalContractAmount = BigDecimal.ZERO;
        BigDecimal totalCommissionAmount = BigDecimal.ZERO;
        for (PerformanceRecordEntity r : records) {
            if (r.getContractAmount() != null) {
                totalContractAmount = totalContractAmount.add(r.getContractAmount());
            }
            if (r.getCommissionAmount() != null) {
                totalCommissionAmount = totalCommissionAmount.add(r.getCommissionAmount());
            }
        }

        // ========== 4. 按 groupBy 维度分组 ==========
        List<Map<String, Object>> groupedList = new ArrayList<>();

        if ("sales_rep".equals(groupBy)) {
            // 按销售人员分组
            Map<Long, List<PerformanceRecordEntity>> bySalesRep = new HashMap<>();
            for (PerformanceRecordEntity r : records) {
                bySalesRep.computeIfAbsent(r.getSalesRepId(), k -> new ArrayList<>()).add(r);
            }
            for (Map.Entry<Long, List<PerformanceRecordEntity>> entry : bySalesRep.entrySet()) {
                Map<String, Object> row = new HashMap<>();
                row.put("dimension", entry.getKey()); // salesRepId
                row.put("contractAmount", sumContractAmount(entry.getValue()));
                row.put("commissionAmount", sumCommissionAmount(entry.getValue()));
                row.put("count", entry.getValue().size());
                groupedList.add(row);
            }
        } else if ("dept".equals(groupBy)) {
            // 按部门分组
            Map<Long, List<PerformanceRecordEntity>> byDept = new HashMap<>();
            for (PerformanceRecordEntity r : records) {
                byDept.computeIfAbsent(r.getDeptId(), k -> new ArrayList<>()).add(r);
            }
            for (Map.Entry<Long, List<PerformanceRecordEntity>> entry : byDept.entrySet()) {
                Map<String, Object> row = new HashMap<>();
                row.put("dimension", entry.getKey()); // deptId
                row.put("contractAmount", sumContractAmount(entry.getValue()));
                row.put("commissionAmount", sumCommissionAmount(entry.getValue()));
                row.put("count", entry.getValue().size());
                groupedList.add(row);
            }
        } else if ("zone".equals(groupBy)) {
            // 按战区分组
            Map<Long, List<PerformanceRecordEntity>> byZone = new HashMap<>();
            for (PerformanceRecordEntity r : records) {
                byZone.computeIfAbsent(r.getZoneId(), k -> new ArrayList<>()).add(r);
            }
            for (Map.Entry<Long, List<PerformanceRecordEntity>> entry : byZone.entrySet()) {
                Map<String, Object> row = new HashMap<>();
                row.put("dimension", entry.getKey()); // zoneId
                row.put("contractAmount", sumContractAmount(entry.getValue()));
                row.put("commissionAmount", sumCommissionAmount(entry.getValue()));
                row.put("count", entry.getValue().size());
                groupedList.add(row);
            }
        } else if ("month".equals(groupBy)) {
            // 按月份分组
            Map<String, List<PerformanceRecordEntity>> byMonth = new HashMap<>();
            for (PerformanceRecordEntity r : records) {
                if (r.getCalculateTime() != null) {
                    String month = String.format("%04d-%02d",
                            r.getCalculateTime().getYear() + 1900,
                            r.getCalculateTime().getMonth() + 1);
                    byMonth.computeIfAbsent(month, k -> new ArrayList<>()).add(r);
                }
            }
            for (Map.Entry<String, List<PerformanceRecordEntity>> entry : byMonth.entrySet()) {
                Map<String, Object> row = new HashMap<>();
                row.put("dimension", entry.getKey()); // 月份，如 "2026-04"
                row.put("contractAmount", sumContractAmount(entry.getValue()));
                row.put("commissionAmount", sumCommissionAmount(entry.getValue()));
                row.put("count", entry.getValue().size());
                groupedList.add(row);
            }
        }

        // 按合同金额降序排列
        groupedList.sort((a, b) -> {
            BigDecimal aAmt = (BigDecimal) a.get("contractAmount");
            BigDecimal bAmt = (BigDecimal) b.get("contractAmount");
            return bAmt.compareTo(aAmt); // 降序
        });

        // ========== 5. 组装返回结果 ==========
        Map<String, Object> result = new HashMap<>();
        result.put("totalContractAmount", totalContractAmount);
        result.put("totalCommissionAmount", totalCommissionAmount);
        result.put("totalCount", records.size());
        result.put("groupedList", groupedList);
        return result;
    }

    /** 计算一组记录的合同金额合计 */
    private BigDecimal sumContractAmount(List<PerformanceRecordEntity> list) {
        BigDecimal sum = BigDecimal.ZERO;
        for (PerformanceRecordEntity r : list) {
            if (r.getContractAmount() != null) {
                sum = sum.add(r.getContractAmount());
            }
        }
        return sum;
    }

    /** 计算一组记录的提成金额合计 */
    private BigDecimal sumCommissionAmount(List<PerformanceRecordEntity> list) {
        BigDecimal sum = BigDecimal.ZERO;
        for (PerformanceRecordEntity r : list) {
            if (r.getCommissionAmount() != null) {
                sum = sum.add(r.getCommissionAmount());
            }
        }
        return sum;
    }
}

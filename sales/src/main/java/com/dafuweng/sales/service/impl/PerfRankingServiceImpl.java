package com.dafuweng.sales.service.impl;

import com.dafuweng.sales.entity.PerformanceRecordEntity;
import com.dafuweng.sales.dao.PerformanceRecordDao;
import com.dafuweng.sales.service.PerfRankingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dafuweng.sales.feign.AuthFeignClient;
import com.dafuweng.sales.feign.SystemFeignClient;
import com.dafuweng.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

@Service
public class PerfRankingServiceImpl implements PerfRankingService {

    @Autowired
    private PerformanceRecordDao performanceRecordDao;

    @Autowired
    private AuthFeignClient authFeignClient;

    @Autowired
    private SystemFeignClient systemFeignClient;

    @Override
    public Map<String, Object> getRankingList(String beginTime, String endTime,
            Long deptId, Long zoneId, String rankingType, Integer limit) {

        // 1. 构建查询条件
        LambdaQueryWrapper<PerformanceRecordEntity> wrapper = new LambdaQueryWrapper<>();
        // 只查已确认和已发放的业绩（status = 2 或 3）
        wrapper.in(PerformanceRecordEntity::getStatus, 2, 3);
        wrapper.eq(deptId != null, PerformanceRecordEntity::getDeptId, deptId);
        wrapper.eq(zoneId != null, PerformanceRecordEntity::getZoneId, zoneId);
        if (StringUtils.hasText(beginTime)) {
            wrapper.ge(PerformanceRecordEntity::getConfirmTime, beginTime);
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le(PerformanceRecordEntity::getConfirmTime, endTime + " 23:59:59");
        }

        // 2. 查询所有符合条件的记录
        List<PerformanceRecordEntity> records = performanceRecordDao.selectList(wrapper);

        // 3. 按 salesRepId 分组聚合
        Map<Long, RankingItem> itemMap = new LinkedHashMap<>();
        for (PerformanceRecordEntity r : records) {
            RankingItem item = itemMap.computeIfAbsent(r.getSalesRepId(),
                    k -> new RankingItem(r.getSalesRepId()));
            item.addContractAmount(r.getContractAmount());
            item.addCommissionAmount(r.getCommissionAmount());
            item.incrementCount();
            if (item.deptId == null) {
                item.deptId = r.getDeptId();
            }
        }

        // 4. 填充用户姓名和部门名称
        for (RankingItem item : itemMap.values()) {
            fillUserName(item);
            fillDeptName(item);
        }

        // 5. 排序
        List<RankingItem> sorted = new ArrayList<>(itemMap.values());
        if ("commission".equals(rankingType)) {
            sorted.sort((a, b) -> b.commissionAmount.compareTo(a.commissionAmount));
        } else if ("count".equals(rankingType)) {
            sorted.sort((a, b) -> Integer.compare(b.count, a.count));
        } else {
            // 默认为 contract_amount
            sorted.sort((a, b) -> b.contractAmount.compareTo(a.contractAmount));
        }

        // 6. 取前 N 名并设置 rank
        List<Map<String, Object>> rankings = new ArrayList<>();
        int rank = 1;
        for (RankingItem item : sorted) {
            if (rank > limit) break;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("rank", rank++);
            row.put("salesRepId", item.salesRepId);
            row.put("salesRepName", item.salesRepName);
            row.put("deptId", item.deptId);
            row.put("deptName", item.deptName);
            row.put("contractAmount", item.contractAmount);
            row.put("commissionAmount", item.commissionAmount);
            row.put("count", item.count);
            rankings.add(row);
        }

        // 7. 返回结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rankings", rankings);
        result.put("totalSalesReps", sorted.size());
        return result;
    }

    private void fillUserName(RankingItem item) {
        try {
            Result<?> res = authFeignClient.getUserById(item.salesRepId);
            if (res != null && res.getCode() == 200 && res.getData() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> user = (Map<String, Object>) res.getData();
                Object name = user.get("realName");
                if (name == null) name = user.get("real_name");
                item.salesRepName = name != null ? name.toString() : "未知(" + item.salesRepId + ")";
                if (item.deptId == null && user.get("deptId") != null) {
                    item.deptId = ((Number) user.get("deptId")).longValue();
                }
            }
        } catch (Exception e) {
            item.salesRepName = "未知(" + item.salesRepId + ")";
        }
    }

    private void fillDeptName(RankingItem item) {
        if (item.deptId == null) return;
        try {
            Result<?> res = systemFeignClient.getDepartmentById(item.deptId);
            if (res != null && res.getCode() == 200 && res.getData() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dept = (Map<String, Object>) res.getData();
                Object name = dept.get("deptName");
                if (name == null) name = dept.get("dept_name");
                item.deptName = name != null ? name.toString() : null;
            }
        } catch (Exception e) {
            // 部门名称获取失败不影响主流程
        }
    }

    /** 内存中用于聚合的临时对象 */
    private static class RankingItem {
        Long salesRepId;
        Long deptId;
        String deptName;
        String salesRepName;
        BigDecimal contractAmount = BigDecimal.ZERO;
        BigDecimal commissionAmount = BigDecimal.ZERO;
        int count = 0;

        RankingItem(Long salesRepId) {
            this.salesRepId = salesRepId;
        }

        void addContractAmount(BigDecimal amount) {
            if (amount != null) {
                this.contractAmount = this.contractAmount.add(amount);
            }
        }

        void addCommissionAmount(BigDecimal amount) {
            if (amount != null) {
                this.commissionAmount = this.commissionAmount.add(amount);
            }
        }

        void incrementCount() {
            this.count++;
        }
    }
}

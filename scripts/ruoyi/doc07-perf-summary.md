# perf-summary 业绩汇总模块开发详细规划

> 本文档是 doc07.md 中 `perf-summary`（业绩汇总）功能的完整开发指南，面向零基础开发者，可直接照此开发。
>
> 生成日期：2026-04-19
> 所属模块：业绩统计 /performance
> 前端路由：`/perf-summary`
> 后端接口：`GET /api/perfSummary/summary`
> 状态：待开发

---

## 一、功能说明

业绩汇总页面用于展示销售团队在指定时间范围内的**合同金额合计**、**提成金额合计**、**合同数量**，并按销售人员、部门、战区等维度分组展示明细。

业务场景示例：
- 销售总监查看本季度各销售人员的业绩汇总
- 按部门查看各部门的合同总额和提成总额
- 按月查看业绩趋势（各月合计对比）

---

## 二、技术架构

### 2.1 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Element Plus + Pinia + axios |
| 前端 HTTP | `@/utils/request.js`（已有） |
| 后端 | Spring Boot + MyBatis-Plus |
| 数据库 | MySQL |
| 网关 | Spring Cloud Gateway（8086） |

### 2.2 请求链路

```
前端 request.js
  → Vite proxy /system/api → Gateway :8086
    → sales 服务 :8083
      → PerfSummaryController
```

### 2.3 模块归属决策

**为什么放在 `sales` 模块，而不是 `system` 模块？**

| 考量 | 结论 |
|------|------|
| 数据表 `performance_record` 所在数据库 | `dafuweng_sales` | → 归属 sales |
| Controller、Service 等代码 | 已在 sales 模块下 | → 归属 sales |
| 其他业绩相关代码（performance-record） | 已在 sales 模块 | → 一致性 |
| 后续业绩相关功能（如 perf-ranking） | 应同属 sales | → 保持一致 |

结论：**在 `sales` 模块新建 `PerfSummaryController`**，复用现有 `performance_record` 表和 DAO，无需新建数据库表。

---

## 三、数据库表

### 3.1 performance_record（已存在，无需新建）

**表名**：`performance_record`（数据库 `dafuweng_sales`）

| 字段名 | Java 字段 | 类型 | 说明 |
|--------|----------|------|------|
| `id` | `id` | BIGINT | 主键 |
| `contract_id` | `contractId` | BIGINT | 合同 ID |
| `sales_rep_id` | `salesRepId` | BIGINT | 销售人员 ID |
| `dept_id` | `deptId` | BIGINT | 部门 ID |
| `zone_id` | `zoneId` | BIGINT | 战区 ID |
| `contract_amount` | `contractAmount` | DECIMAL(15,2) | 合同金额 |
| `commission_rate` | `commissionRate` | DECIMAL(6,4) | 提成比例 |
| `commission_amount` | `commissionAmount` | DECIMAL(15,2) | 提成金额 |
| `status` | `status` | TINYINT | 1=计算中 2=已确认 3=已发放 4=已取消 |
| `calculate_time` | `calculateTime` | DATETIME | 计算时间 |
| `confirm_time` | `confirmTime` | DATETIME | 确认时间 |
| `grant_time` | `grantTime` | DATETIME | 发放时间 |
| `cancel_reason` | `cancelReason` | VARCHAR(200) | 取消原因 |
| `remark` | `remark` | TEXT | 备注 |
| `created_by` | `createdBy` | BIGINT | 创建人 |
| `created_at` | `createdAt` | DATETIME | 创建时间 |
| `updated_by` | `updatedBy` | BIGINT | 更新人 |
| `updated_at` | `updatedAt` | DATETIME | 更新时间 |
| `deleted` | `deleted` | TINYINT | 逻辑删除 |

### 3.2 关联表（已存在，用于查名称）

| 表名 | 用途 | 关键字段 |
|------|------|---------|
| `sys_user`（auth 模块） | 查销售人员姓名 | `id`, `real_name` |
| `sys_department`（system 模块） | 查部门名称 | `id`, `dept_name` |
| `sys_zone`（system 模块） | 查战区名称 | `id`, `zone_name` |

> **注意**：跨模块查名称需要使用 Feign 远程调用或直接 JOIN 视图。本规划**先仅返回 ID**，名称显示在后续优化阶段完成。如需立即显示名称，请与后端确认是否已有跨服务查询工具。

---

## 四、后端开发（按步骤）

### 第一步：新建 Controller

**文件**：`sales/src/main/java/com/dafuweng/sales/controller/PerfSummaryController.java`

```java
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
```

---

### 第二步：新建 Service 接口

**文件**：`sales/src/main/java/com/dafuweng/sales/service/PerfSummaryService.java`

```java
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
```

---

### 第三步：新建 Service 实现类

**文件**：`sales/src/main/java/com/dafuweng/sales/service/impl/PerfSummaryServiceImpl.java`

```java
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
        // 注意：数据量超过 10000 条时建议改用 SQL 聚合（见下方 SQL 方案）
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
```

> **性能说明**：以上实现先将数据全部加载到内存再聚合，适用于数据量小于 10000 条的场景。
>
> **生产优化方案**：当数据量超过 10000 条时，建议在 `PerformanceRecordDao.xml` 中使用原生 SQL 聚合（`SELECT sales_rep_id, SUM(contract_amount), ... FROM performance_record GROUP BY sales_rep_id`），然后在 Service 中直接返回 SQL 聚合结果，不做内存聚合。

---

### 第四步：确认 DAO 已存在

**文件**：`sales/src/main/java/com/dafuweng/sales/dao/PerformanceRecordDao.java`

该文件**已存在**，无需新建。确认以下内容已包含：

```java
package com.dafuweng.sales.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.sales.entity.PerformanceRecordEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PerformanceRecordDao extends BaseMapper<PerformanceRecordEntity> {
    // BaseMapper 已提供 selectList() 方法，无需额外编写
}
```

---

### 第五步：确认 Gateway 路由

**文件**：`gateway/src/main/resources/application.yml`

**问题**：`/api/perfSummary/**` 路由目前**不存在**，需要在 `sales-api-route` 中添加。

在 `sales-api-route` 的 `predicates` 中**追加**以下两条：

```yaml
- id: sales-api-route
  uri: http://localhost:8083
  predicates:
    # ... 已有路径 ...
    - Path=/api/perfSummary/**    # 【新增】
    - Path=/perfSummary/**        # 【新增】（兼容非 /api 前缀格式）
  filters:
    - StripPrefix=0
```

> **说明**：`StripPrefix=0` 表示不剥离任何路径前缀，因此：
> - 请求 `/api/perfSummary/summary` → 到达 `sales:8083` 时保持原路径 → Controller `@RequestMapping("/api/perfSummary")` 正确匹配

**验证**：添加路由后，重启 Gateway，然后测试：
```bash
curl http://localhost:8086/api/perfSummary/summary
# 应返回 {"code":200,"data":{...}} 而非 404
```

---

## 五、前端开发（按步骤）

### 第一步：新建 API 文件

**文件**：`ruoyi-ui/src/api/perf-summary.js`

```javascript
import request from '@/utils/request'

/***********************************************************************
 * 业绩汇总 API
 * 对应后端：GET /api/perfSummary/summary
 ***********************************************************************/

/告
/**
 * 获取业绩汇总数据
 * @param {Object} params - 查询参数
 * @param {string} [params.beginTime]   - 统计开始日期，格式 yyyy-MM-dd
 * @param {string} [params.endTime]     - 统计结束日期，格式 yyyy-MM-dd
 * @param {number} [params.deptId]      - 部门ID（可选）
 * @param {number} [params.zoneId]      - 战区ID（可选）
 * @param {string} [params.groupBy='sales_rep'] - 聚合维度：
 *        sales_rep=按销售人员, dept=按部门, zone=按战区, month=按月份
 * @returns {Promise} 汇总结果
 */
export function getSummary(params) {
  return request({
    url: '/perfSummary/summary',
    method: 'get',
    params
  })
}
```

> **注意**：URL 写成 `/perfSummary/summary`（无 `/api` 前缀），
> 因为 `request.js` 中的 `getBaseURL()` 函数会根据 URL 前缀自动拼接 baseURL。
> 但目前 `request.js` 中**没有** `/perfSummary` 前缀的判断，会走 `/dev-api`。
>
> **需要同步修改** `ruoyi-ui/src/utils/request.js` 的 `getBaseURL()` 函数，
> 在 sales 模块判断中**追加** `|| url.startsWith('/perfSummary')`：
>
> ```javascript
> // 找到 sales 模块的判断（约第 34 行），修改为：
> if (url.startsWith('/customer') || url.startsWith('/contract') ||
>     url.startsWith('/contactRecord') || url.startsWith('/workLog') ||
>     url.startsWith('/performanceRecord') || url.startsWith('/customerTransferLog') ||
>     url.startsWith('/contractAttachment') || url.startsWith('/perfSummary')) {
>   return '/sales/api'
> }
> ```

---

### 第二步：新建 Vue 视图文件

**文件**：`ruoyi-ui/src/views/perf-summary/index.vue`

```vue
<template>
  <div class="app-container">
    <!-- ====== 搜索区域 ====== -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <!-- 日期范围 -->
      <el-form-item label="统计周期">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 240px"
          @change="handleDateChange"
        />
      </el-form-item>
      <!-- 聚合维度 -->
      <el-form-item label="分组维度">
        <el-select v-model="queryParams.groupBy" placeholder="请选择" clearable style="width: 140px">
          <el-option label="按销售人员" value="sales_rep" />
          <el-option label="按部门" value="dept" />
          <el-option label="按战区" value="zone" />
          <el-option label="按月份" value="month" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">查询</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- ====== 顶部统计卡片 ====== -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">合同总金额（元）</div>
          <div class="stat-value">{{ formatNumber(summaryData.totalContractAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">提成总金额（元）</div>
          <div class="stat-value">{{ formatNumber(summaryData.totalCommissionAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-label">合同总数（个）</div>
          <div class="stat-value">{{ summaryData.totalCount || 0 }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- ====== 分组明细表格 ====== -->
    <el-table
      v-loading="loading"
      :data="groupedList"
      border
      stripe
      class="summary-table"
    >
      <!-- 维度名称列（根据 groupBy 显示不同表头） -->
      <el-table-column label="维度" align="center" min-width="120">
        <template #default="scope">
          <span v-if="queryParams.groupBy === 'sales_rep'">销售 {{ scope.row.dimension }}</span>
          <span v-else-if="queryParams.groupBy === 'dept'">部门 {{ scope.row.dimension }}</span>
          <span v-else-if="queryParams.groupBy === 'zone'">战区 {{ scope.row.dimension }}</span>
          <span v-else>{{ scope.row.dimension }}</span>
        </template>
      </el-table-column>

      <!-- 各指标列 -->
      <el-table-column label="合同金额（元）" align="center">
        <template #default="scope">
          {{ formatNumber(scope.row.contractAmount) }}
        </template>
      </el-table-column>
      <el-table-column label="提成金额（元）" align="center">
        <template #default="scope">
          {{ formatNumber(scope.row.commissionAmount) }}
        </template>
      </el-table-column>
      <el-table-column label="合同数量" align="center" prop="count" />
      <!-- 占比列 -->
      <el-table-column label="合同金额占比" align="center">
        <template #default="scope">
          {{
            summaryData.totalContractAmount > 0
              ? ((scope.row.contractAmount / summaryData.totalContractAmount) * 100).toFixed(1) + '%'
              : '0%'
          }}
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { getSummary } from '@/api/perf-summary'

// ========== 响应式数据 ==========
const loading = ref(false)
const showSearch = ref(true)
const dateRange = ref([])
const queryParams = reactive({
  beginTime: '',
  endTime: '',
  groupBy: 'sales_rep'   // 默认按销售人员
})
const summaryData = reactive({
  totalContractAmount: 0,
  totalCommissionAmount: 0,
  totalCount: 0,
  groupedList: []
})
const groupedList = computed(() => summaryData.groupedList || [])

// ========== 生命周期 ==========
onMounted(() => {
  // 默认显示本月数据
  const now = new Date()
  const firstDay = new Date(now.getFullYear(), now.getMonth(), 1)
  const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0)
  dateRange.value = [
    formatDate(firstDay),
    formatDate(lastDay)
  ]
  queryParams.beginTime = dateRange.value[0]
  queryParams.endTime = dateRange.value[1]
  getList()
})

// ========== 方法 ==========
function handleDateChange(val) {
  if (val && val.length === 2) {
    queryParams.beginTime = val[0]
    queryParams.endTime = val[1]
  } else {
    queryParams.beginTime = ''
    queryParams.endTime = ''
  }
}

function handleQuery() {
  summaryData.groupedList = []
  getList()
}

function resetQuery() {
  dateRange.value = []
  queryParams.beginTime = ''
  queryParams.endTime = ''
  queryParams.groupBy = 'sales_rep'
  handleQuery()
}

function getList() {
  loading.value = true
  getSummary({
    beginTime: queryParams.beginTime || undefined,
    endTime: queryParams.endTime || undefined,
    groupBy: queryParams.groupBy
  }).then(res => {
    const data = res.data || {}
    summaryData.totalContractAmount = data.totalContractAmount || 0
    summaryData.totalCommissionAmount = data.totalCommissionAmount || 0
    summaryData.totalCount = data.totalCount || 0
    summaryData.groupedList = data.groupedList || []
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// ========== 格式化工具 ==========
/**
 * 格式化数字，千分位分隔，保留两位小数
 */
function formatNumber(val) {
  if (val == null || val === '') return '0.00'
  const num = parseFloat(val)
  if (isNaN(num)) return '0.00'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/**
 * 将 Date 对象格式化为 yyyy-MM-dd
 */
function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}
</script>

<style scoped>
/* ====== 统计卡片样式 ====== */
.stat-row {
  margin-bottom: 20px;
}
.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  padding: 20px;
  color: #fff;
  text-align: center;
}
.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
}
/* 如果是绿色主题，用这个：background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); */

/* ====== 表格样式 ====== */
.summary-table {
  margin-top: 10px;
}
</style>
```

---

## 六、完整文件清单

### 6.1 后端（新建）

| 序号 | 文件路径 | 说明 |
|------|---------|------|
| 1 | `sales/src/main/java/com/dafuweng/sales/controller/PerfSummaryController.java` | Controller |
| 2 | `sales/src/main/java/com/dafuweng/sales/service/PerfSummaryService.java` | Service 接口 |
| 3 | `sales/src/main/java/com/dafuweng/sales/service/impl/PerfSummaryServiceImpl.java` | Service 实现 |

### 6.2 后端（确认/修改）

| 序号 | 文件路径 | 说明 |
|------|---------|------|
| 4 | `gateway/src/main/resources/application.yml` | 在 `sales-api-route` 添加 `/api/perfSummary/**` 路由断言 |
| 5 | `ruoyi-ui/src/utils/request.js` | 在 sales 模块判断中追加 `/perfSummary` 前缀 |

### 6.3 前端（新建）

| 序号 | 文件路径 | 说明 |
|------|---------|------|
| 6 | `ruoyi-ui/src/api/perf-summary.js` | API 文件 |
| 7 | `ruoyi-ui/src/views/perf-summary/index.vue` | Vue 视图文件 |

---

## 七、开发自检清单（完成后必读）

完成代码编写后，按以下顺序逐一验证：

### 后端自检

- [ ] `PerfSummaryController.java` 编译通过（无语法错误）
- [ ] `PerfSummaryServiceImpl.java` 编译通过
- [ ] Gateway 已添加 `/api/perfSummary/**` 路由，并已**重启 Gateway**
- [ ] 使用 curl 测试接口返回正常 JSON：
  ```bash
  curl "http://localhost:8086/api/perfSummary/summary?groupBy=sales_rep"
  # 期望：{"code":200,"data":{"totalContractAmount":...,"groupedList":[...]}}
  ```
- [ ] 同样测试带日期范围的查询：
  ```bash
  curl "http://localhost:8086/api/perfSummary/summary?beginTime=2026-01-01&endTime=2026-04-30&groupBy=dept"
  ```

### 前端自检

- [ ] `ruoyi-ui/src/utils/request.js` 已添加 `/perfSummary` 前缀判断
- [ ] `npm run dev` 启动无报错
- [ ] 访问 `/perf-summary` 页面正常显示（无 404）
- [ ] 页面加载后显示本月统计数据（默认时间范围）
- [ ] 切换"分组维度"下拉框，表格数据正确变化
- [ ] 切换日期范围，表格数据正确变化
- [ ] 表格中的数字显示为千分位格式（如 1,234,567.00）

---

## 八、接口契约（供前端对接用）

### 请求

```
GET /api/perfSummary/summary
Query 参数：
  - beginTime  （可选）格式 yyyy-MM-dd，如 "2026-01-01"
  - endTime    （可选）格式 yyyy-MM-dd，如 "2026-04-30"
  - deptId     （可选）BIGINT，如 1
  - zoneId     （可选）BIGINT，如 1
  - groupBy    （可选，默认 sales_rep）
                  sales_rep = 按销售人员
                  dept      = 按部门
                  zone      = 按战区
                  month     = 按月份
```

### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalContractAmount": 5000000.00,
    "totalCommissionAmount": 500000.00,
    "totalCount": 120,
    "groupedList": [
      {
        "dimension": 5,
        "contractAmount": 500000.00,
        "commissionAmount": 50000.00,
        "count": 12
      },
      {
        "dimension": 3,
        "contractAmount": 300000.00,
        "commissionAmount": 30000.00,
        "count": 8
      }
    ]
  }
}
```

### 错误码说明

| code | 含义 |
|------|------|
| 200 | 成功 |
| 500 | 服务器内部错误（检查日志） |

---

## 九、常见问题排查

### Q1：接口返回 404

**原因**：Gateway 路由未添加或未重启 Gateway。

**排查**：检查 `gateway/application.yml` 中 `sales-api-route` 是否包含 `Path=/api/perfSummary/**`，Gateway 是否已重启。

### Q2：接口返回 401/403

**原因**：请求未带 Authorization header 或 Token 过期。

**排查**：前端 request.js 是否在请求中正确添加了 Bearer Token。

### Q3：表格数据为空

**原因**：`performance_record` 表中 `status=2`（已确认）的记录不存在。

**排查**：直接查数据库 `SELECT * FROM performance_record WHERE status=2 LIMIT 10`。

### Q4：日期筛选无效

**原因**：`calculate_time` 字段存储格式与查询格式不匹配。

**排查**：检查数据库中 `calculate_time` 的实际格式（如 `2026-04-01 00:00:00` 还是 `2026-04-01`），调整 Service 中的拼接方式（`beginTime + " 00:00:00"`）。

### Q5：数字显示为 NaN 或 undefined

**原因**：`formatNumber` 接收了非数字值。

**排查**：检查后端返回的 `contractAmount` / `commissionAmount` 字段是否为 `BigDecimal` 序列化后的字符串（JSON 反序列化问题）。

---

## 十、优化建议（可选，非本次必须）

| 优化项 | 说明 | 工作量 |
|--------|------|--------|
| 显示姓名而非 ID | 前端表格中显示"张三"而非"5"，需要后端提供 id→name 映射 | 0.5d |
| SQL 聚合替代内存聚合 | 当数据量超过 1 万条时，使用原生 SQL `GROUP BY` 替代内存聚合 | 0.5d |
| 趋势折线图 | 引入 ECharts，在页面底部增加业绩趋势折线图 | 1d |
| 导出 Excel | 增加"导出汇总"按钮，生成 Excel 文件 | 0.5d |

---

*本文档由 Claude Code 基于 NeoCC 项目实际代码结构生成，参考了 `SysZoneController`、`PerformanceRecordServiceImpl` 等现有实现的完整代码模式。*

# perf-ranking 业绩排名模块开发详细规划

> 本文档是 doc07.md 中 `perf-ranking`（业绩排名）功能的完整开发指南，面向零基础开发者，可直接照此开发。
>
> 生成日期：2026-04-19
> 所属模块：业绩统计 /performance
> 前端路由：`/perf-ranking`
> 后端接口：`GET /api/perfRanking/list`
> 状态：待开发

---

## 一、功能说明

业绩排名页面用于展示销售团队在指定时间范围内的**排名对比**，支持按合同金额、提成金额、合同数量等维度排名，并展示前三名高亮。

业务场景示例：
- 销售总监查看本月/本季度销售业绩排行榜
- 按部门维度查看部门排名
- 导出排名数据用于会议汇报

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
  → Vite proxy /sales/api → Gateway :8086
    → sales 服务 :8083
      → PerfRankingController
```

### 2.3 模块归属决策

**放在 `sales` 模块。**

`performance_record` 表在 `dafuweng_sales` 数据库，`PerfRankingController` 应放在 `sales` 模块，与 `PerformanceRecordController` 同模块。

---

## 三、数据库表

### 3.1 performance_record（dafuweng_sales）

> 业绩记录表，perf-ranking 的主要数据源。

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键 |
| `contract_id` | BIGINT | 合同 ID |
| `sales_rep_id` | BIGINT | 销售人员 ID |
| `dept_id` | BIGINT | 部门 ID |
| `zone_id` | BIGINT | 战区 ID |
| `contract_amount` | DECIMAL(15,2) | 合同金额 |
| `commission_rate` | DECIMAL(6,4) | 提成比例 |
| `commission_amount` | DECIMAL(15,2) | 提成金额 |
| `status` | TINYINT | 1-计算中 2-已确认 3-已发放 4-已取消 |
| `calculate_time` | DATETIME | 计算时间 |
| `confirm_time` | DATETIME | 确认时间 |
| `grant_time` | DATETIME | 发放时间 |
| `deleted` | TINYINT | 0-未删除 1-已删除 |

### 3.2 sys_user（dafuweng_auth）

> 用户表，存储销售人员姓名。

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键 |
| `username` | VARCHAR(50) | 用户名 |
| `nick_name` | VARCHAR(30) | 昵称/姓名 |
| `dept_id` | BIGINT | 部门 ID |

### 3.3 sys_department（dafuweng_auth）

> 部门表，存储部门名称。

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | BIGINT | 主键 |
| `dept_name` | VARCHAR(30) | 部门名称 |
| `parent_id` | BIGINT | 父部门 ID |

---

## 四、后端接口规格

### 4.1 PerfRankingController

**文件**：`sales/src/main/java/com/dafuweng/sales/controller/PerfRankingController.java`

```java
package com.dafuweng.sales.controller;

import com.dafuweng.common.entity.Result;
import com.dafuweng.sales.service.PerfRankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/perfRanking")
public class PerfRankingController {

    @Autowired
    private PerfRankingService perfRankingService;

    /**
     * 业绩排名列表
     * GET /api/perfRanking/list
     *
     * @param beginTime  统计开始日期（yyyy-MM-dd）
     * @param endTime    统计结束日期（yyyy-MM-dd）
     * @param deptId     部门维度筛选（可选）
     * @param zoneId     战区维度筛选（可选）
     * @param rankingType 排名类型：commission（按提成）/ contract_amount（按合同额）/ count（按合同数）
     * @param limit      返回前 N 名，默认 20
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
```

### 4.2 PerfRankingService 接口

**文件**：`sales/src/main/java/com/dafuweng/sales/service/PerfRankingService.java`

```java
package com.dafuweng.sales.service;

import java.util.List;
import java.util.Map;

public interface PerfRankingService {

    /**
     * 获取业绩排名列表
     *
     * @param beginTime  开始时间（yyyy-MM-dd，可为 null 表示不限）
     * @param endTime    结束时间（yyyy-MM-dd，可为 null 表示不限）
     * @param deptId     部门筛选（可选）
     * @param zoneId     战区筛选（可选）
     * @param rankingType 排名类型：commission / contract_amount / count
     * @param limit      返回条数限制
     * @return 包含 rankings 列表和 totalSalesReps 的 Map
     */
    Map<String, Object> getRankingList(String beginTime, String endTime,
            Long deptId, Long zoneId, String rankingType, Integer limit);
}
```

### 4.3 PerfRankingServiceImpl

**文件**：`sales/src/main/java/com/dafuweng/sales/service/impl/PerfRankingServiceImpl.java`

> 本实现使用内存聚合，适合数据量可控的场景（< 10000 条）。
> 如果性能_record 表数据量大（> 10万），应改用 SQL 聚合（参考 doc07-perf-summary.md 的 SQL 聚合方案）。

```java
package com.dafuweng.sales.service.impl;

import com.dafuweng.sales.entity.PerformanceRecordEntity;
import com.dafuweng.sales.dao.PerformanceRecordDao;
import com.dafuweng.sales.service.PerfRankingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dafuweng.sales.feign.AuthFeignClient;
import com.dafuweng.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PerfRankingServiceImpl implements PerfRankingService {

    @Autowired
    private PerformanceRecordDao performanceRecordDao;

    @Autowired
    private AuthFeignClient authFeignClient;

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
            // 取第一个发现的 deptId（同一销售应该属于同一部门）
            if (item.deptId == null) {
                item.deptId = r.getDeptId();
            }
        }

        // 4. 填充用户姓名（异步调用 authFeignClient）
        for (RankingItem item : itemMap.values()) {
            fillUserName(item);
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
                item.salesRepName = (String) user.get("nickName");
                // 如果 deptId 未被业绩记录填充，则尝试取用户自己的 deptId
                if (item.deptId == null && user.get("deptId") != null) {
                    item.deptId = ((Number) user.get("deptId")).longValue();
                }
            }
        } catch (Exception e) {
            item.salesRepName = "未知(" + item.salesRepId + ")";
        }

        // 尝试用 SystemFeignClient 补 deptName（如果需要）
        // 此处简化处理，deptName 可在后续优化
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
```

### 4.4 PerfRankingDao

**文件**：`sales/src/main/java/com/dafuweng/sales/dao/PerfRankingDao.java`

> 内存聚合方案不需要复杂 DAO，用 MyBatis-Plus 的 `selectList` 即可。
> 此文件可省略，或直接注入 `PerformanceRecordDao` 使用（已在 ServiceImpl 中注入）。

如果使用 SQL 聚合方案（推荐用于大数据量），新建：

```java
package com.dafuweng.sales.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dafuweng.sales.entity.PerformanceRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PerfRankingDao extends BaseMapper<PerformanceRecordEntity> {

    // SQL 聚合方案（备选）：直接用数据库 GROUP BY
    // 此 SQL 由后端拼接，按 rankingType 动态选择排序字段
    @Select("<script>" +
            "SELECT pr.sales_rep_id AS salesRepId, " +
            "       SUM(pr.contract_amount) AS contractAmount, " +
            "       SUM(pr.commission_amount) AS commissionAmount, " +
            "       COUNT(*) AS count " +
            "FROM performance_record pr " +
            "WHERE pr.deleted = 0 AND pr.status IN (2,3) " +
            "<if test='beginTime != null'> AND pr.confirm_time &gt;= #{beginTime} </if>" +
            "<if test='endTime != null'> AND pr.confirm_time &lt;= #{endTime} </if>" +
            "<if test='deptId != null'> AND pr.dept_id = #{deptId} </if>" +
            "<if test='zoneId != null'> AND pr.zone_id = #{zoneId} </if>" +
            "GROUP BY pr.sales_rep_id " +
            "ORDER BY " +
            "  CASE WHEN #{rankingType} = 'commission' THEN SUM(pr.commission_amount) END DESC," +
            "  CASE WHEN #{rankingType} = 'count' THEN COUNT(*) END DESC," +
            "  CASE WHEN #{rankingType} = 'contract_amount' OR #{rankingType} IS NULL THEN SUM(pr.contract_amount) END DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<Map<String, Object>> selectRankingList(
            @Param("beginTime") String beginTime,
            @Param("endTime") String endTime,
            @Param("deptId") Long deptId,
            @Param("zoneId") Long zoneId,
            @Param("rankingType") String rankingType,
            @Param("limit") Integer limit
    );
}
```

> **注意**：SQL 聚合方案更高效，推荐生产环境使用。
> 如果用 SQL 聚合，则 ServiceImpl 直接调用 `perfRankingDao.selectRankingList()` 即可，无需内存聚合。

---

## 五、Gateway 路由配置

**文件**：`gateway/src/main/resources/application.yml`

在 `sales-api-route` 的 predicates 中追加 `/api/perfRanking/**`：

```yaml
        - id: sales-api-route
          uri: http://localhost:8083
          predicates:
            - Path=/api/customer/**
            - Path=/api/contract/**
            - Path=/api/contactRecord/**
            - Path=/api/workLog/**
            - Path=/api/performanceRecord/**
            - Path=/api/customerTransferLog/**
            - Path=/api/contractAttachment/**
            - Path=/api/perfRanking/**        # ← 新增
          filters:
            - StripPrefix=0
```

---

## 六、request.js 前端路由配置

**文件**：`ruoyi-ui/src/utils/request.js`

在 `getBaseURL()` 的 sales 模块判断中追加 `/perfRanking`：

```javascript
  // sales 模块
  if (url.startsWith('/customer') || url.startsWith('/contract') ||
      url.startsWith('/contactRecord') || url.startsWith('/workLog') ||
      url.startsWith('/performanceRecord') || url.startsWith('/customerTransferLog') ||
      url.startsWith('/contractAttachment') ||
      url.startsWith('/perfRanking')) {       // ← 新增
    return '/sales/api'
  }
```

---

## 七、前端 API 文件

**文件**：`ruoyi-ui/src/api/perf-ranking.js`

```javascript
import request from '@/utils/request'

// 获取业绩排名列表
export function getRankingList(params) {
  return request({
    url: '/perfRanking/list',
    method: 'get',
    params
  })
}

// 导出排名（可选）
export function exportRanking(params) {
  return request({
    url: '/perfRanking/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
```

---

## 八、前端视图文件

**文件**：`ruoyi-ui/src/views/perf-ranking/index.vue`

```vue
<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="日期范围" prop="dateRange">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="handleDateChange"
        />
      </el-form-item>
      <el-form-item label="排名类型" prop="rankingType">
        <el-select v-model="queryParams.rankingType" placeholder="请选择" clearable>
          <el-option label="按合同金额" value="contract_amount" />
          <el-option label="按提成金额" value="commission" />
          <el-option label="按合同数量" value="count" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 统计摘要 -->
    <el-row :gutter="20" class="mb8">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">参与排名人数</div>
          <div class="stat-value">{{ rankingData.totalSalesReps || 0 }} 人</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">TOP1 合同金额</div>
          <div class="stat-value primary">
            {{ formatMoney(rankingData.topContractAmount) }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">TOP1 提成金额</div>
          <div class="stat-value success">
            {{ formatMoney(rankingData.topCommissionAmount) }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-title">合同总数</div>
          <div class="stat-value">{{ rankingData.totalCount || 0 }} 单</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 排名表格 -->
    <el-table v-loading="loading" :data="rankingData.rankings" border class="mt16">
      <el-table-column label="排名" align="center" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.rank === 1" type="warning" effect="dark">
            {{ scope.row.rank }}
          </el-tag>
          <el-tag v-else-if="scope.row.rank === 2" type="warning">
            {{ scope.row.rank }}
          </el-tag>
          <el-tag v-else-if="scope.row.rank === 3" type="success">
            {{ scope.row.rank }}
          </el-tag>
          <span v-else>{{ scope.row.rank }}</span>
        </template>
      </el-table-column>
      <el-table-column label="销售人员" align="center" prop="salesRepName" />
      <el-table-column label="部门" align="center" prop="deptName" />
      <el-table-column label="合同金额" align="right" prop="contractAmount">
        <template #default="scope">
          {{ formatMoney(scope.row.contractAmount) }}
        </template>
      </el-table-column>
      <el-table-column label="提成金额" align="right" prop="commissionAmount">
        <template #default="scope">
          {{ formatMoney(scope.row.commissionAmount) }}
        </template>
      </el-table-column>
      <el-table-column label="合同数量" align="center" prop="count" />
    </el-table>

    <pagination
      v-show="(rankingData.totalSalesReps || 0) > 0"
      :total="rankingData.totalSalesReps || 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script setup>
import { getRankingList } from '@/api/perf-ranking'
import { formatMoney } from '@/utils/ruoyi'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const showSearch = ref(true)
const dateRange = ref([])
const rankingData = ref({
  rankings: [],
  totalSalesReps: 0,
  topContractAmount: 0,
  topCommissionAmount: 0,
  totalCount: 0
})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  rankingType: 'contract_amount'
})

onMounted(() => {
  getList()
})

function handleDateChange(val) {
  if (val) {
    queryParams.beginTime = val[0]
    queryParams.endTime = val[1]
  } else {
    queryParams.beginTime = null
    queryParams.endTime = null
  }
}

function getList() {
  loading.value = true
  getRankingList(queryParams).then(res => {
    const data = res.data || {}
    rankingData.value = {
      rankings: data.rankings || [],
      totalSalesReps: data.totalSalesReps || 0,
      // 计算 top 值
      topContractAmount: data.rankings?.[0]?.contractAmount || 0,
      topCommissionAmount: data.rankings?.[0]?.commissionAmount || 0,
      totalCount: data.rankings?.reduce((sum, r) => sum + (r.count || 0), 0) || 0
    }
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  queryParams.beginTime = null
  queryParams.endTime = null
  queryParams.rankingType = 'contract_amount'
  queryParams.pageNum = 1
  getList()
}
</script>

<style scoped>
.mt16 { margin-top: 16px; }
.mb8 { margin-bottom: 8px; }
.stat-title { font-size: 13px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 24px; font-weight: bold; }
.stat-value.primary { color: #409eff; }
.stat-value.success { color: #67c23a; }
</style>
```

---

## 九、完整文件清单

| # | 文件路径 | 操作 |
|---|---------|------|
| 1 | `sales/src/main/java/com/dafuweng/sales/controller/PerfRankingController.java` | 新建 |
| 2 | `sales/src/main/java/com/dafuweng/sales/service/PerfRankingService.java` | 新建 |
| 3 | `sales/src/main/java/com/dafuweng/sales/service/impl/PerfRankingServiceImpl.java` | 新建 |
| 4 | `sales/src/main/java/com/dafuweng/sales/dao/PerfRankingDao.java` | 新建（可选，SQL聚合方案用） |
| 5 | `gateway/src/main/resources/application.yml` | 修改：追加 `/api/perfRanking/**` 到 `sales-api-route` |
| 6 | `ruoyi-ui/src/utils/request.js` | 修改：追加 `/perfRanking` 到 sales 模块判断 |
| 7 | `ruoyi-ui/src/api/perf-ranking.js` | 新建 |
| 8 | `ruoyi-ui/src/views/perf-ranking/index.vue` | 新建 |

---

## 十、自检清单（上线前必须全部通过）

- [ ] `PerfRankingController.java` 编译通过
- [ ] `PerfRankingServiceImpl.java` 编译通过
- [ ] Gateway 配置已更新并重启 Gateway 服务
- [ ] `request.js` 已追加 `/perfRanking` 前缀路由
- [ ] 前端 `perf-ranking.js` API 文件已创建
- [ ] 前端 `views/perf-ranking/index.vue` 视图已创建
- [ ] Vite 开发服务器已重启
- [ ] 访问 `/perf-ranking` 页面可正常打开
- [ ] 选择不同 rankingType（合同金额/提成/数量）排名结果正确
- [ ] 日期筛选器范围查询返回正确数据
- [ ] 前三名高亮显示（TOP3 标签颜色不同）
- [ ] 底部统计卡片数据与表格数据一致

---

## 十一、接口合同

### GET /api/perfRanking/list

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| beginTime | String | 否 | 统计开始日期（yyyy-MM-dd） |
| endTime | String | 否 | 统计结束日期（yyyy-MM-dd） |
| deptId | Long | 否 | 部门维度筛选 |
| zoneId | Long | 否 | 战区维度筛选 |
| rankingType | String | 否 | 排名类型：contract_amount / commission / count（默认 contract_amount） |
| limit | Integer | 否 | 返回前 N 名（默认 20） |

**响应示例**：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "rankings": [
      {
        "rank": 1,
        "salesRepId": 10,
        "salesRepName": "张三",
        "deptId": 5,
        "deptName": "销售一部",
        "contractAmount": 5000000.00,
        "commissionAmount": 500000.00,
        "count": 45
      },
      {
        "rank": 2,
        "salesRepId": 11,
        "salesRepName": "李四",
        "deptId": 5,
        "deptName": "销售一部",
        "contractAmount": 4500000.00,
        "commissionAmount": 450000.00,
        "count": 40
      }
    ],
    "totalSalesReps": 25
  }
}
```

---

## 十二、常见错误排查

| 错误现象 | 原因 | 解决方案 |
|---------|------|---------|
| 页面 404 | Gateway 未配置 `/api/perfRanking/**` | 检查 application.yml 的 sales-api-route |
| 排名全为 0 | `performance_record` 表 status 不在 2/3 | 检查 status 条件，或查询所有 status |
| 姓名显示"未知" | authFeignClient 调用失败 | 检查 auth 服务是否启动，或在 PerfRankingDao 中 JOIN sys_user 表 |
| 内存聚合慢（> 3s） | 记录数量 > 10000 | 改用 SQL 聚合方案（PerfRankingDao.selectRankingList） |
| 日期筛选无效 | SQL 条件拼接错误 | 检查 confirm_time 的 >= 和 <= 条件，确认日期格式 |

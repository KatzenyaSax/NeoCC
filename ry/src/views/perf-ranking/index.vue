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
        <el-select v-model="queryParams.rankingType" placeholder="请选择" clearable @change="handleQuery">
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
    <el-row :gutter="20" class="mb16">
      <el-col :span="6">
        <div class="stat-card stat-info">
          <div class="stat-label">参与排名人数</div>
          <div class="stat-value">{{ rankingData.totalSalesReps || 0 }} 人</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-primary">
          <div class="stat-label">TOP1 合同金额</div>
          <div class="stat-value">{{ formatMoney(rankingData.topContractAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-success">
          <div class="stat-label">TOP1 提成金额</div>
          <div class="stat-value">{{ formatMoney(rankingData.topCommissionAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card stat-warning">
          <div class="stat-label">合同总数</div>
          <div class="stat-value">{{ rankingData.totalCount || 0 }} 单</div>
        </div>
      </el-col>
    </el-row>

    <!-- ====== 横向柱状图 ====== -->
    <el-card shadow="hover" class="chart-card mb16">
      <template #header>
        <div class="card-header">
          <span>业绩排名 TOP10</span>
          <el-tag size="small" type="info">按{{ rankingTypeLabel }}</el-tag>
        </div>
      </template>
      <div ref="rankChartRef" class="rank-chart-container"></div>
    </el-card>

    <!-- 排名表格 -->
    <el-table v-loading="loading" :data="rankingData.rankings" border class="rank-table">
      <el-table-column label="排名" align="center" width="80">
        <template #default="scope">
          <div :class="'rank-badge rank-' + scope.row.rank">
            {{ scope.row.rank }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="销售人员" align="center" prop="salesRepName">
        <template #default="scope">
          <span v-if="scope.row.salesRepName">{{ scope.row.salesRepName }}</span>
          <span v-else class="text-muted">销售{{ scope.row.salesRepId }}</span>
        </template>
      </el-table-column>
      <el-table-column label="部门" align="center" prop="deptName">
        <template #default="scope">
          <span v-if="scope.row.deptName">{{ scope.row.deptName }}</span>
          <span v-else class="text-muted">部门{{ scope.row.deptId }}</span>
        </template>
      </el-table-column>
      <el-table-column label="合同金额" align="right" prop="contractAmount">
        <template #default="scope">
          <span class="money">{{ formatMoney(scope.row.contractAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="提成金额" align="right" prop="commissionAmount">
        <template #default="scope">
          <span class="money success">{{ formatMoney(scope.row.commissionAmount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="合同数量" align="center" prop="count" />
      <el-table-column label="占比" align="center" width="100">
        <template #default="scope">
          {{
            rankingData.topContractAmount > 0
              ? ((scope.row.contractAmount / rankingData.topContractAmount) * 100).toFixed(1) + '%'
              : '0%'
          }}
        </template>
      </el-table-column>
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
import * as echarts from 'echarts'
import { getRankingList } from '@/api/perf-ranking'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const showSearch = ref(true)
const dateRange = ref([])
const rankChartRef = ref(null)
let rankChart = null

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

const rankingTypeLabel = computed(() => {
  const map = { contract_amount: '合同金额', commission: '提成金额', count: '合同数量' }
  return map[queryParams.rankingType] || '合同金额'
})

const rankingField = computed(() => queryParams.rankingType === 'commission' ? 'commissionAmount' : 'contractAmount')

// 排名颜色
const rankColors = ['#f56c6c', '#e6a23c', '#67c23a', '#409eff', '#9c27b0', '#00bcd4', '#ff5722', '#795548', '#607d8b', '#3f51b5']

onMounted(() => {
  // 默认显示本月数据
  const now = new Date()
  const firstDay = new Date(now.getFullYear(), now.getMonth(), 1)
  const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0)
  dateRange.value = [formatDate(firstDay), formatDate(lastDay)]
  queryParams.beginTime = dateRange.value[0]
  queryParams.endTime = dateRange.value[1]
  getList()

  // 初始化图表
  nextTick(() => {
    if (rankChartRef.value) {
      rankChart = echarts.init(rankChartRef.value)
      window.addEventListener('resize', handleResize)
    }
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  rankChart?.dispose()
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
  getRankingList({
    beginTime: queryParams.beginTime || undefined,
    endTime: queryParams.endTime || undefined,
    rankingType: queryParams.rankingType
  }).then(res => {
    const data = res.data || {}
    rankingData.value = {
      rankings: data.rankings || [],
      totalSalesReps: data.totalSalesReps || 0,
      topContractAmount: data.rankings?.[0]?.contractAmount || 0,
      topCommissionAmount: data.rankings?.[0]?.commissionAmount || 0,
      totalCount: data.rankings?.reduce((sum, r) => sum + (r.count || 0), 0) || 0
    }
    loading.value = false
    // 更新图表
    nextTick(() => updateChart())
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

function updateChart() {
  if (!rankChart || rankingData.value.rankings.length === 0) return

  const top10 = rankingData.value.rankings.slice(0, 10)
  const field = rankingField.value
  const label = rankingTypeLabel.value

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const item = params[0]
        const rank = top10[item.dataIndex]
        return `<strong>${rank.salesRepName || '销售' + rank.salesRepId}</strong><br/>
                排名: 第${rank.rank}名<br/>
                ${label}: ${formatMoney(rank[field]) || '0.00'}元<br/>
                合同数: ${rank.count}单`
      }
    },
    grid: { left: '3%', right: '12%', bottom: '3%', top: '5%', containLabel: true },
    xAxis: {
      type: 'value',
      name: '金额（元）',
      axisLabel: { formatter: (v) => formatAxisValue(v) }
    },
    yAxis: {
      type: 'category',
      data: top10.map(r => r.salesRepName || `销售${r.salesRepId}`).reverse(),
      axisLabel: { fontSize: 11 }
    },
    series: [{
      type: 'bar',
      data: top10.map((r, i) => ({
        value: r[field] || 0,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: rankColors[9 - i] || '#409eff' },
            { offset: 1, color: rankColors[9 - i] + '99' || '#409eff99' }
          ]),
          borderRadius: [0, 4, 4, 0]
        }
      })).reverse(),
      barWidth: '60%',
      label: {
        show: true,
        position: 'right',
        formatter: (p) => formatAxisValue(p.value),
        fontSize: 10,
        color: '#666'
      },
      emphasis: {
        itemStyle: { shadowBlur: 10, shadowColor: 'rgba(0,0,0,0.3)' }
      }
    }]
  }
  rankChart.setOption(option)
}

function handleResize() {
  rankChart?.resize()
}

function formatMoney(val) {
  if (val == null || val === '') return '0.00'
  const num = parseFloat(val)
  if (isNaN(num)) return '0.00'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatAxisValue(val) {
  if (val >= 100000000) return (val / 100000000).toFixed(1) + 'E'
  if (val >= 10000) return (val / 10000).toFixed(1) + 'W'
  if (val >= 1000) return (val / 1000).toFixed(1) + 'K'
  return val
}

function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}
</script>

<style scoped>
.mb16 { margin-bottom: 16px; }
.mb8 { margin-bottom: 8px; }

.stat-card {
  border-radius: 8px;
  padding: 20px;
  color: #fff;
  text-align: center;
  min-height: 80px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.stat-primary { background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%); }
.stat-success { background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%); }
.stat-warning { background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%); }
.stat-info { background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%); }
.stat-label { font-size: 13px; opacity: 0.9; margin-bottom: 6px; }
.stat-value { font-size: 22px; font-weight: bold; }

.chart-card { min-height: 380px; }
.rank-chart-container { height: 320px; width: 100%; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

.rank-table .rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  font-weight: bold;
  font-size: 14px;
  color: #fff;
}
.rank-1 { background: linear-gradient(135deg, #f56c6c, #e6a23c); }
.rank-2 { background: linear-gradient(135deg, #e6a23c, #67c23a); }
.rank-3 { background: linear-gradient(135deg, #67c23a, #409eff); }
.rank-4, .rank-5, .rank-6, .rank-7, .rank-8, .rank-9, .rank-10 {
  background: #909399;
}

.money { font-family: 'Courier New', monospace; font-weight: 500; }
.money.success { color: #67c23a; }
.text-muted { color: #999; font-size: 12px; }
</style>

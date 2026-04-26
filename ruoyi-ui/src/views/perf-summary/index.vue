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
        <el-select v-model="queryParams.groupBy" placeholder="请选择" clearable style="width: 140px" @change="handleQuery">
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
        <div class="stat-card stat-primary">
          <div class="stat-label">合同总金额（元）</div>
          <div class="stat-value">{{ formatNumber(summaryData.totalContractAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card stat-success">
          <div class="stat-label">提成总金额（元）</div>
          <div class="stat-value">{{ formatNumber(summaryData.totalCommissionAmount) }}</div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card stat-warning">
          <div class="stat-label">合同总数（个）</div>
          <div class="stat-value">{{ summaryData.totalCount || 0 }}</div>
        </div>
      </el-col>
    </el-row>

    <!-- ====== 图表区域 ====== -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>业绩对比（柱状图）</span>
            </div>
          </template>
          <div ref="barChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <div class="card-header">
              <span>合同金额占比（饼图）</span>
            </div>
          </template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
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
          <span v-if="scope.row.dimensionName">{{ scope.row.dimensionName }}</span>
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
import * as echarts from 'echarts'
import { getSummary } from '@/api/perf-summary'

// ========== 响应式数据 ==========
const loading = ref(false)
const showSearch = ref(true)
const dateRange = ref([])
const barChartRef = ref(null)
const pieChartRef = ref(null)
let barChart = null
let pieChart = null
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

// 图表颜色
const chartColors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de', '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc']

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

  // 初始化图表
  initCharts()
  // 监听窗口变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  barChart?.dispose()
  pieChart?.dispose()
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
    // 更新图表
    nextTick(() => updateCharts())
  }).catch(() => {
    loading.value = false
  })
}

function initCharts() {
  if (barChartRef.value) {
    barChart = echarts.init(barChartRef.value)
  }
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
  }
}

function updateCharts() {
  updateBarChart()
  updatePieChart()
}

function updateBarChart() {
  if (!barChart || groupedList.value.length === 0) return

  const data = groupedList.value.slice(0, 10) // 取前10条

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const item = params[0]
        return `${item.name}<br/>合同金额: ${formatNumber(item.value)}元<br/>提成金额: ${formatNumber(data[item.dataIndex]?.commissionAmount || 0)}元`
      }
    },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(item => {
        if (queryParams.groupBy === 'month') return item.dimension
        return item.dimensionName || `D${item.dimension}`
      }),
      axisLabel: { rotate: 30, fontSize: 10 }
    },
    yAxis: { type: 'value', name: '金额（元）', axisLabel: { formatter: (v) => formatAxisValue(v) } },
    series: [
      {
        name: '合同金额',
        type: 'bar',
        data: data.map(item => item.contractAmount || 0),
        itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#5470c6' },
          { offset: 1, color: '#91cc75' }
        ]) },
        barWidth: '50%',
        label: { show: true, position: 'top', formatter: (p) => formatAxisValue(p.value), fontSize: 9 }
      }
    ]
  }
  barChart.setOption(option)
}

function updatePieChart() {
  if (!pieChart || groupedList.value.length === 0) return

  const data = groupedList.value.slice(0, 8).map(item => ({
    name: queryParams.groupBy === 'month' ? item.dimension : (item.dimensionName || `D${item.dimension}`),
    value: item.contractAmount || 0
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: (p) => `${p.name}<br/>金额: ${formatNumber(p.value)}元<br/>占比: ${p.percent}%`
    },
    legend: { orient: 'vertical', left: 'left', top: 'middle', textStyle: { fontSize: 11 } },
    series: [{
      type: 'pie',
      radius: ['35%', '65%'],
      center: ['60%', '50%'],
      avoidLabelOverlap: true,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%', fontSize: 10 },
      emphasis: { label: { show: true, fontSize: 12, fontWeight: 'bold' } },
      data: data,
      color: chartColors
    }]
  }
  pieChart.setOption(option)
}

function handleResize() {
  barChart?.resize()
  pieChart?.resize()
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

function formatAxisValue(val) {
  if (val >= 100000000) return (val / 100000000).toFixed(1) + 'E'
  if (val >= 10000) return (val / 10000).toFixed(1) + 'W'
  return val
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
  border-radius: 8px;
  padding: 24px;
  color: #fff;
  text-align: center;
  min-height: 100px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.stat-primary { background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%); }
.stat-success { background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%); }
.stat-warning { background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%); }
.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
}

.chart-row {
  margin-bottom: 20px;
}
.chart-card {
  height: 360px;
}
.chart-container {
  height: 280px;
  width: 100%;
}
.card-header {
  font-weight: 600;
  font-size: 14px;
}

/* ====== 表格样式 ====== */
.summary-table {
  margin-top: 10px;
}
</style>

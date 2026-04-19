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

/* ====== 表格样式 ====== */
.summary-table {
  margin-top: 10px;
}
</style>

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

function formatMoney(val) {
  if (val == null || val === '') return '0.00'
  const num = parseFloat(val)
  if (isNaN(num)) return '0.00'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
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

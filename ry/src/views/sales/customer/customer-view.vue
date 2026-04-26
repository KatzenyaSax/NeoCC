<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="客户搜索" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          placeholder="输入客户ID或客户名称"
          clearable
          @keyup.enter="handleSearch"
          style="width: 200px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 无数据提示 -->
    <el-empty v-if="!hasData" description="请输入客户ID或客户名称进行搜索" />

    <!-- 详情Tabs -->
    <el-tabs v-else v-model="activeTab" class="mt16">
      <!-- 基本信息 -->
      <el-tab-pane label="基本信息" name="basic">
        <el-table :data="[viewData.customer]" border>
          <el-table-column label="字段" prop="" width="150">
            <template #default="scope">
              <span class="field-label">客户名称</span>
            </template>
          </el-table-column>
          <el-table-column label="值">
            <template #default="scope">
              {{ viewData.customer?.name || '-' }}
            </template>
          </el-table-column>
        </el-table>
        <el-table :data="basicFields" border class="mt8">
          <el-table-column label="字段" prop="label" width="150" />
          <el-table-column label="值" prop="value">
            <template #default="scope">
              {{ scope.row.value || '-' }}
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 联系记录 -->
      <el-tab-pane label="联系记录" name="contact">
        <el-table :data="viewData.contactRecords" border>
          <el-table-column label="联系类型" prop="contactType" width="120" />
          <el-table-column label="联系时间" prop="contactDate" width="160" />
          <el-table-column label="联系内容" prop="content" />
          <el-table-column label="意向变化" width="160">
            <template #default="scope">
              {{ scope.row.intentionBefore }} → {{ scope.row.intentionAfter }}
            </template>
          </el-table-column>
          <el-table-column label="下次跟进" prop="followUpDate" width="160" />
        </el-table>
        <el-empty v-if="!viewData.contactRecords?.length" description="暂无联系记录" />
      </el-tab-pane>

      <!-- 合同记录 -->
      <el-tab-pane label="合同记录" name="contract">
        <el-table :data="viewData.contracts" border>
          <el-table-column label="合同编号" prop="contractNo" width="180" />
          <el-table-column label="合同金额" prop="contractAmount" width="140" align="right">
            <template #default="scope">
              {{ formatMoney(scope.row.contractAmount) }}
            </template>
          </el-table-column>
          <el-table-column label="实际放款" prop="actualLoanAmount" width="140" align="right">
            <template #default="scope">
              {{ formatMoney(scope.row.actualLoanAmount) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="100" />
          <el-table-column label="签订日期" prop="signDate" width="120" />
          <el-table-column label="产品ID" prop="productId" width="100" />
        </el-table>
        <el-empty v-if="!viewData.contracts?.length" description="暂无合同记录" />
      </el-tab-pane>

      <!-- 业绩记录 -->
      <el-tab-pane label="业绩记录" name="performance">
        <el-table :data="viewData.performanceRecords" border>
          <el-table-column label="合同金额" prop="contractAmount" width="140" align="right">
            <template #default="scope">
              {{ formatMoney(scope.row.contractAmount) }}
            </template>
          </el-table-column>
          <el-table-column label="提成金额" prop="commissionAmount" width="120" align="right">
            <template #default="scope">
              {{ formatMoney(scope.row.commissionAmount) }}
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="100">
            <template #default="scope">
              {{ statusText(scope.row.status) }}
            </template>
          </el-table-column>
          <el-table-column label="确认时间" prop="confirmTime" width="160" />
        </el-table>
        <el-empty v-if="!viewData.performanceRecords?.length" description="暂无业绩记录" />
      </el-tab-pane>

      <!-- 转移记录 -->
      <el-tab-pane label="转移记录" name="transfer">
        <el-table :data="viewData.transferLogs" border>
          <el-table-column label="操作类型" prop="operateType" width="120" />
          <el-table-column label="操作人" prop="operatedBy" width="100" />
          <el-table-column label="操作时间" prop="operatedAt" width="160" />
          <el-table-column label="原因" prop="reason" />
        </el-table>
        <el-empty v-if="!viewData.transferLogs?.length" description="暂无转移记录" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { getCustomerView } from '@/api/sales/customer'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const showSearch = ref(true)
const activeTab = ref('basic')
const hasData = ref(false)

const queryParams = reactive({
  keyword: ''
})

const viewData = ref({
  customer: null,
  contactRecords: [],
  contracts: [],
  performanceRecords: [],
  transferLogs: []
})

// 基本信息表格的字段列表
const basicFields = computed(() => {
  const c = viewData.value.customer || {}
  return [
    { label: '客户名称', value: c.name },
    { label: '联系电话', value: c.phone },
    { label: '身份证号', value: c.idCard },
    { label: '公司名称', value: c.companyName },
    { label: '公司法人', value: c.companyLegalPerson },
    { label: '注册资本', value: c.companyRegCapital },
    { label: '客户类型', value: c.customerType },
    { label: '意向等级', value: c.intentionLevel },
    { label: '状态', value: c.status === 1 ? '有效' : '无效' },
    { label: '最后联系', value: c.lastContactDate },
    { label: '下次跟进', value: c.nextFollowUpDate },
    { label: '公海时间', value: c.publicSeaTime },
    { label: '公海原因', value: c.publicSeaReason },
    { label: '来源', value: c.source },
    { label: '贷款意向金额', value: c.loanIntentionAmount },
    { label: '贷款意向产品', value: c.loanIntentionProduct },
    { label: '创建时间', value: c.createdAt },
    { label: '更新时间', value: c.updatedAt },
  ]
})

function handleSearch() {
  const keyword = queryParams.keyword?.trim()
  if (!keyword) return

  // 判断是ID还是名称
  const isId = /^\d+$/.test(keyword)
  if (isId) {
    fetchView(Number(keyword))
  } else {
    // 按名称模糊搜索，先查客户列表找到ID
    searchCustomerByName(keyword)
  }
}

function searchCustomerByName(name) {
  loading.value = true
  // 使用已有的 listCustomer 接口模糊搜索
  import('@/api/sales/customer').then(module => {
    module.listCustomer({ name, pageNum: 1, pageSize: 20 }).then(res => {
      const list = res.data?.records || res.records || []
      if (list.length === 0) {
        proxy.$modal.msgWarning('未找到客户')
        loading.value = false
        return
      }
      // 如果只有一个结果，直接查看详情
      if (list.length === 1) {
        fetchView(list[0].id)
      } else {
        // 多个结果提示选择
        proxy.$modal.msgWarning('找到多个客户，请输入更精确的客户ID进行查看')
        loading.value = false
      }
    }).catch(() => {
      loading.value = false
    })
  })
}

function fetchView(id) {
  loading.value = true
  getCustomerView(id).then(res => {
    const data = res.data || {}
    viewData.value = {
      customer: data.customer || null,
      contactRecords: data.contactRecords || [],
      contracts: data.contracts || [],
      performanceRecords: data.performanceRecords || [],
      transferLogs: data.transferLogs || []
    }
    hasData.value = !!data.customer
    activeTab.value = 'basic'
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

function resetQuery() {
  queryParams.keyword = ''
  hasData.value = false
  viewData.value = { customer: null, contactRecords: [], contracts: [], performanceRecords: [], transferLogs: [] }
}

function formatMoney(val) {
  if (val == null || val === '') return '-'
  const num = parseFloat(val)
  if (isNaN(num)) return '-'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function statusText(status) {
  const map = { 1: '待接收', 2: '已确认', 3: '已发放' }
  return map[status] || status
}
</script>

<style scoped>
.mt16 { margin-top: 16px; }
.mt8 { margin-top: 8px; }
.field-label { color: #909399; }
</style>

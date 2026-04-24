<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="客户名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入客户名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="客户类型" prop="customerType">
        <el-select v-model="queryParams.customerType" placeholder="客户类型" clearable style="width: 100px">
          <el-option label="个人" :value="1" />
          <el-option label="企业" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="意向等级" prop="intentionLevel">
        <el-select v-model="queryParams.intentionLevel" placeholder="意向等级" clearable style="width: 110px">
          <el-option label="低" :value="1" />
          <el-option label="中" :value="2" />
          <el-option label="高" :value="3" />
          <el-option label="很有意向" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f56c6c, #e6a23c)">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ total }}</div>
            <div class="stat-label">公海客户总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #67c23a, #85ce61)">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ personalCount }}</div>
            <div class="stat-label">个人客户</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #409eff, #66b1ff)">
            <el-icon><OfficeBuilding /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ enterpriseCount }}</div>
            <div class="stat-label">企业客户</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #7c3aed, #a78bfa)">
            <el-icon><Calendar /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ monthCount }}</div>
            <div class="stat-label">本月新增</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 公海客户列表 -->
    <el-table v-loading="loading" :data="publicSeaList" border stripe class="mt16">
      <el-table-column label="客户名称" align="center" prop="name" min-width="120" show-overflow-tooltip>
        <template #default="scope">
          <div class="customer-name">
            <el-icon><UserFilled /></el-icon>
            <span>{{ scope.row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="联系电话" align="center" prop="phone" width="130">
        <template #default="scope">
          <span class="phone-text">{{ scope.row.phone || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="客户类型" align="center" prop="customerType" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.customerType === 1 ? 'primary' : 'warning'" size="small">
            {{ customerTypeText(scope.row.customerType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="意向等级" align="center" prop="intentionLevel" width="100">
        <template #default="scope">
          <el-tag :type="getIntentionTagType(scope.row.intentionLevel)" size="small">
            {{ intentionText(scope.row.intentionLevel) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="贷款意向金额" align="center" prop="loanIntentionAmount" width="130">
        <template #default="scope">
          {{ formatMoney(scope.row.loanIntentionAmount) }}
        </template>
      </el-table-column>
      <el-table-column label="公海时间" align="center" prop="publicSeaTime" width="120">
        <template #default="scope">
          <span class="time-text">{{ formatDate(scope.row.publicSeaTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="公海原因" align="center" prop="publicSeaReason" min-width="150" show-overflow-tooltip>
        <template #default="scope">
          <span class="reason-text">{{ scope.row.publicSeaReason || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Refresh" @click="handleTransfer(scope.row)">转移</el-button>
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button v-if="isManager" link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态提示 -->
    <el-empty v-if="!loading && publicSeaList.length === 0" description="暂无公海客户" class="empty-tip" />

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 转移弹窗 -->
    <el-dialog title="转移客户给销售代表" v-model="transferOpen" width="500px" append-to-body>
      <el-card class="transfer-card" shadow="never">
        <template #header>
          <div class="transfer-header">
            <el-icon><Warning /></el-icon>
            <span>客户转移确认</span>
          </div>
        </template>
        <el-form ref="transferRef" :model="transferForm" label-width="100px">
          <el-form-item label="客户名称">
            <el-tag type="warning">{{ transferForm.customerName }}</el-tag>
          </el-form-item>
          <el-form-item label="目标销售代表" prop="toRepId">
            <el-select
              v-model="transferForm.toRepId"
              placeholder="请选择销售代表"
              :disabled="isSalesRep"
              style="width: 100%"
              filterable
              :loading="salesRepLoading"
            >
              <el-option
                v-for="rep in salesRepList"
                :key="rep.id"
                :label="rep.realName + (rep.username ? ` (${rep.username})` : '')"
                :value="rep.id"
              >
                <span style="float: left">{{ rep.realName }}</span>
                <span style="float: right; color: #8492a6; font-size: 12px;">{{ rep.username }}</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="转移原因" prop="reason">
            <el-input v-model="transferForm.reason" type="textarea" placeholder="请输入转移原因（选填）" rows="3" maxlength="200" show-word-limit />
          </el-form-item>
        </el-form>
      </el-card>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="transferOpen = false">取 消</el-button>
          <el-button type="primary" @click="confirmTransfer" :disabled="!transferForm.toRepId">确认转移</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog title="公海客户详情" v-model="detailOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="客户名称">{{ detailForm.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailForm.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ detailForm.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="公司名称">{{ detailForm.companyName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户类型">{{ customerTypeText(detailForm.customerType) }}</el-descriptions-item>
        <el-descriptions-item label="意向等级">
          <el-tag :type="getIntentionTagType(detailForm.intentionLevel)" size="small">
            {{ intentionText(detailForm.intentionLevel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="来源">{{ detailForm.source || '-' }}</el-descriptions-item>
        <el-descriptions-item label="贷款意向金额">{{ formatMoney(detailForm.loanIntentionAmount) }}</el-descriptions-item>
        <el-descriptions-item label="贷款意向产品">{{ detailForm.loanIntentionProduct || '-' }}</el-descriptions-item>
        <el-descriptions-item label="最后联系">{{ formatDate(detailForm.lastContactDate) }}</el-descriptions-item>
        <el-descriptions-item label="公海时间">{{ formatDate(detailForm.publicSeaTime) }}</el-descriptions-item>
        <el-descriptions-item label="公海原因" :span="2">{{ detailForm.publicSeaReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="批注" :span="2">{{ detailForm.annotation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ formatDate(detailForm.createdAt) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
        <el-button type="primary" @click="goTransfer">转 移</el-button>
      </template>
    </el-dialog>

    <!-- 修改弹窗 -->
    <el-dialog title="修改客户信息" v-model="editOpen" width="700px" append-to-body>
      <el-tabs v-model="editActiveTab">
        <el-tab-pane label="基本信息" name="basic">
          <el-form ref="editRef" :model="editForm" :rules="editRules" label-width="100px" style="max-height: 400px; overflow-y: auto">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="客户名称" prop="name">
                  <el-input v-model="editForm.name" placeholder="请输入客户名称" maxlength="50" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="客户类型" prop="customerType">
                  <el-select v-model="editForm.customerType" placeholder="请选择" style="width: 100%">
                    <el-option label="个人" :value="1" />
                    <el-option label="企业" :value="2" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="联系电话" prop="phone">
                  <el-input v-model="editForm.phone" placeholder="请输入手机号" maxlength="11" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="意向等级" prop="intentionLevel">
                  <el-select v-model="editForm.intentionLevel" placeholder="请选择" style="width: 100%">
                    <el-option label="低" :value="1" />
                    <el-option label="中" :value="2" />
                    <el-option label="高" :value="3" />
                    <el-option label="很有意向" :value="4" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="身份证号" prop="idCard">
                  <el-input v-model="editForm.idCard" placeholder="请输入身份证号" maxlength="18" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="来源" prop="source">
                  <el-input v-model="editForm.source" placeholder="请输入来源" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="公司名称" prop="companyName">
              <el-input v-model="editForm.companyName" placeholder="请输入公司名称" />
            </el-form-item>
            <el-form-item label="批注" prop="annotation">
              <el-input v-model="editForm.annotation" type="textarea" :rows="2" placeholder="请输入批注" maxlength="500" show-word-limit />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="贷款意向" name="loan">
          <el-form :model="editForm" label-width="120px" style="max-height: 400px; overflow-y: auto">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="贷款意向金额">
                  <el-input-number v-model="editForm.loanIntentionAmount" :min="0" :precision="2" :controls="false" placeholder="请输入金额" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="贷款意向产品">
                  <el-input v-model="editForm.loanIntentionProduct" placeholder="请输入意向产品" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editOpen = false">取 消</el-button>
          <el-button type="primary" @click="confirmEdit">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { getPublicSeaPage, transferCustomer, listSalesReps } from '@/api/sales/publicSea'
import { getCustomer, updateCustomer, delCustomer } from '@/api/sales/customer'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const publicSeaList = ref([])
const salesRepLoading = ref(false)
const editActiveTab = ref('basic')

const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  name: undefined,
  customerType: undefined,
  intentionLevel: undefined
})

// 统计计算
const personalCount = computed(() => publicSeaList.value.filter(c => c.customerType === 1).length)
const enterpriseCount = computed(() => publicSeaList.value.filter(c => c.customerType === 2).length)
const monthCount = computed(() => {
  const now = new Date()
  const thisMonth = now.getFullYear() + '-' + String(now.getMonth() + 1).padStart(2, '0')
  return publicSeaList.value.filter(c => c.publicSeaTime && c.publicSeaTime.startsWith(thisMonth)).length
})

// 转移弹窗
const transferOpen = ref(false)
const transferForm = ref({
  customerId: null,
  customerName: '',
  toRepId: null,
  reason: ''
})
const salesRepList = ref([])

// 详情弹窗
const detailOpen = ref(false)
const detailForm = ref({})

// 修改弹窗
const editOpen = ref(false)
const editForm = ref({})
const editRules = {
  name: [{ required: true, message: '客户名称不能为空', trigger: 'blur' }],
  phone: [
    { required: true, message: '联系电话不能为空', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 权限判断
const userStore = useUserStore()
const isSalesRep = computed(() => userStore.roles?.includes('sales_rep'))
const isManager = computed(() => userStore.roles?.some(r => ['sales_manager', 'admin'].includes(r)))

/** 客户类型文字 */
function customerTypeText(val) {
  return { 1: '个人', 2: '企业' }[val] || '-'
}

/** 公海客户意向等级文字 */
function intentionText(level) {
  const map = { 1: '低', 2: '中', 3: '高', 4: '很有意向', 5: '已签约' }
  return map[level] || '-'
}

/** 意向等级标签类型 */
function getIntentionTagType(level) {
  const types = { 1: 'info', 2: 'warning', 3: 'success', 4: 'danger' }
  return types[level] || 'info'
}

/** 格式化金额 */
function formatMoney(val) {
  if (val == null || val === '') return '-'
  const num = parseFloat(val)
  if (isNaN(num)) return '-'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' 元'
}

/** 格式化日期 */
function formatDate(val) {
  if (!val) return '-'
  if (typeof val === 'string' && val.includes('T')) {
    return val.split('T')[0]
  }
  return val
}

/** 查询公海客户列表 */
function getList() {
  loading.value = true
  getPublicSeaPage(queryParams).then(res => {
    const data = res.data || {}
    publicSeaList.value = data.records || []
    total.value = data.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

/** 搜索按钮 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 重置按钮 */
function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.name = undefined
  queryParams.customerType = undefined
  queryParams.intentionLevel = undefined
  queryParams.pageNum = 1
  getList()
}

/** 加载销售代表列表 */
function loadSalesReps() {
  salesRepLoading.value = true
  salesRepList.value = []
  listSalesReps().then(res => {
    salesRepList.value = res.data || []
    salesRepLoading.value = false
  }).catch(() => {
    salesRepLoading.value = false
    proxy.$modal.msgError('加载销售代表列表失败，请检查服务连接')
  })
}

/** 转移按钮 */
function handleTransfer(row) {
  transferForm.value = {
    customerId: row.id,
    customerName: row.name,
    toRepId: isSalesRep.value ? userStore.id : null,
    reason: ''
  }
  transferOpen.value = true
  loadSalesReps()
}

/** 确认转移 */
function confirmTransfer() {
  if (!transferForm.value.toRepId) {
    proxy.$modal.msgWarning('请选择目标销售代表')
    return
  }
  transferCustomer({
    customerId: transferForm.value.customerId,
    toRepId: transferForm.value.toRepId,
    reason: transferForm.value.reason,
    operatorId: userStore.id
  }).then(() => {
    proxy.$modal.msgSuccess('转移成功')
    transferOpen.value = false
    getList()
  }).catch(() => {
    proxy.$modal.msgError('转移失败')
  })
}

/** 详情按钮 */
function handleDetail(row) {
  getCustomer(row.id).then(res => {
    detailForm.value = res.data || res
    detailOpen.value = true
  })
}

/** 从详情页跳转转移 */
function goTransfer() {
  detailOpen.value = false
  handleTransfer(detailForm.value)
}

/** 修改按钮 */
function handleUpdate(row) {
  getCustomer(row.id).then(res => {
    editForm.value = { ...(res.data || res) }
    editActiveTab.value = 'basic'
    editOpen.value = true
  })
}

/** 确认修改 */
function confirmEdit() {
  proxy.$refs["editRef"].validate((valid) => {
    if (valid) {
      updateCustomer(editForm.value).then(() => {
        proxy.$modal.msgSuccess('修改成功')
        editOpen.value = false
        getList()
      }).catch(() => {
        proxy.$modal.msgError('修改失败')
      })
    }
  })
}

/** 删除按钮 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除客户"' + row.name + '"？').then(() => {
    return delCustomer(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

getList()
</script>

<style scoped>
.mt16 {
  margin-top: 16px;
}

/* 统计卡片 */
.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-icon .el-icon {
  font-size: 28px;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

/* 表格样式 */
.customer-name {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #409eff;
  font-weight: 500;
}

.customer-name .el-icon {
  font-size: 16px;
}

.phone-text {
  font-family: 'Monaco', 'Menlo', monospace;
  color: #606266;
}

.time-text {
  color: #909399;
  font-size: 13px;
}

.reason-text {
  color: #606266;
  font-size: 13px;
}

/* 空状态 */
.empty-tip {
  margin: 40px 0;
}

/* 转移弹窗 */
.transfer-card {
  border: none;
}

.transfer-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #f56c6c, #e6a23c);
  color: #fff;
  padding: 12px 20px;
}

.transfer-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  font-weight: 500;
}

.transfer-header .el-icon {
  font-size: 18px;
}

/* 对话框 */
.dialog-footer {
  text-align: right;
}

:deep(.el-tabs__content) {
  overflow: hidden;
}
</style>

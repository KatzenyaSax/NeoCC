<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="合同编号" prop="contractNo">
        <el-input v-model="queryParams.contractNo" placeholder="请输入合同编号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 合同表格 -->
    <el-table v-loading="loading" :data="contractList" :row-key="row => row.id" @sort-change="handleSortChange">
      <el-table-column label="ID" align="center" prop="id" width="80" sortable />
      <el-table-column label="合同编号" align="center" prop="contractNo" />
      <el-table-column label="客户ID" align="center" prop="customerId" />
      <el-table-column label="销售代表ID" align="center" prop="salesRepId" />
      <el-table-column label="合同金额" align="center" prop="contractAmount" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createdAt" width="180" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 详情对话框 -->
    <el-dialog title="合同详情" v-model="detailVisible" width="800px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="合同编号">{{ detailForm.contract?.contractNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailForm.contract?.status)">{{ getStatusLabel(detailForm.contract?.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="客户ID">{{ detailForm.customerId }}</el-descriptions-item>
        <el-descriptions-item label="销售代表ID">{{ detailForm.salesRepId }}</el-descriptions-item>
        <el-descriptions-item label="部门ID">{{ detailForm.deptId }}</el-descriptions-item>
        <el-descriptions-item label="战区ID">{{ detailForm.zoneId }}</el-descriptions-item>
        <el-descriptions-item label="合同金额">{{ detailForm.contract?.contractAmount }}</el-descriptions-item>
        <el-descriptions-item label="实际贷款金额">{{ detailForm.contract?.actualLoanAmount }}</el-descriptions-item>
        <el-descriptions-item label="服务费率">{{ detailForm.contract?.serviceFeeRate }}</el-descriptions-item>
        <el-descriptions-item label="服务费1">{{ detailForm.contract?.serviceFee1 }}</el-descriptions-item>
        <el-descriptions-item label="服务费2">{{ detailForm.contract?.serviceFee2 }}</el-descriptions-item>
        <el-descriptions-item label="服务费1已付">{{ detailForm.contract?.serviceFee1Paid === 1 ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="服务费2已付">{{ detailForm.contract?.serviceFee2Paid === 1 ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="签署日期">{{ detailForm.contract?.signDate }}</el-descriptions-item>
        <el-descriptions-item label="纸质合同编号">{{ detailForm.contract?.paperContractNo }}</el-descriptions-item>
        <el-descriptions-item label="贷款用途" :span="2">{{ detailForm.contract?.loanUse }}</el-descriptions-item>
        <el-descriptions-item label="担保信息" :span="2">{{ detailForm.contract?.guaranteeInfo }}</el-descriptions-item>
        <el-descriptions-item label="拒绝原因" :span="2">{{ detailForm.contract?.rejectReason }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailForm.contract?.remark }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="success" @click="handleApprove">通 过</el-button>
          <el-button type="danger" @click="handleReject">拒 绝</el-button>
          <el-button @click="detailVisible = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 拒绝对话框 -->
    <el-dialog title="拒绝合同" v-model="rejectVisible" width="500px" append-to-body>
      <el-form ref="rejectRef" :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因" prop="rejectReason">
          <el-input v-model="rejectForm.rejectReason" type="textarea" placeholder="请输入拒绝原因" rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitReject">确 定</el-button>
          <el-button @click="rejectVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { pageContractAudit, getContractAuditDetail, approveContractAudit, rejectContractAudit } from
      "@/api/finance/contractAudit.js"

const { proxy } = getCurrentInstance()

const contractList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const detailVisible = ref(false)
const rejectVisible = ref(false)
const detailForm = ref({})
const rejectForm = ref({})
const currentContractId = ref(null)

const statusOptions = {
  0: '待签署',
  1: '草稿',
  2: '已签署',
  3: '已付首期',
  4: '审核中',
  5: '已通过',
  6: '已拒绝',
  7: '已放款',
  8: '完成'
}

function getStatusLabel(status) {
  return statusOptions[status] || '未知'
}

function getStatusType(status) {
  const types = { 0: 'info', 1: 'success', 2: 'warning', 3: 'success', 4: 'warning', 5: 'success', 6: 'danger', 7: 'primary', 8: 'info' }
  return types[status] || 'info'
}

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    contractNo: undefined,
    sortField: 'id',
    sortOrder: 'asc'
  }
})

const { queryParams } = toRefs(data)

/** 查询审核中合同列表 */
function getList() {
  loading.value = true
  pageContractAudit(queryParams.value).then(response => {
    contractList.value = response.data?.records || response.records || []
    total.value = response.data?.total || response.total || 0
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function handleSortChange({ prop, order }) {
  queryParams.value.sortField = prop
  queryParams.value.sortOrder = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 查看详情 */
function handleView(row) {
  currentContractId.value = row.id
  getContractAuditDetail(row.id).then(response => {
    detailForm.value = response.data || response
    detailVisible.value = true
  })
}

/** 通过审核 */
function handleApprove() {
  proxy.$modal.confirm('确认通过该合同？').then(() => {
    return approveContractAudit(currentContractId.value, { auditOpinion: '' })
  }).then(() => {
    proxy.$modal.msgSuccess("审核通过")
    detailVisible.value = false
    getList()
  }).catch(() => {})
}

/** 拒绝审核 */
function handleReject() {
  rejectForm.value = { rejectReason: '' }
  rejectVisible.value = true
}

function submitReject() {
  if (!rejectForm.value.rejectReason) {
    proxy.$modal.msgError("请输入拒绝原因")
    return
  }
  rejectContractAudit(currentContractId.value, rejectForm.value).then(() => {
    proxy.$modal.msgSuccess("已拒绝")
    rejectVisible.value = false
    detailVisible.value = false
    getList()
  })
}

getList()
</script>

<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="待接收" :value="0" />
          <el-option label="审核中" :value="1" />
          <el-option label="银行审核" :value="2" />
          <el-option label="已批准" :value="3" />
          <el-option label="已拒绝" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="loanAuditList">
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="合同ID" align="center" prop="contractId" />
      <el-table-column label="申请金额" align="center" prop="loanAmount" />
      <el-table-column label="利率" align="center" prop="interestRate" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createdAt" width="180" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="primary" icon="Check" @click="handleApprove(scope.row)" v-if="scope.row.status === 2">批准</el-button>
          <el-button link type="primary" icon="Close" @click="handleReject(scope.row)" v-if="scope.row.status === 2">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 详情对话框 -->
    <el-dialog title="贷款审核详情" v-model="detailOpen" width="600px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ detailForm.id }}</el-descriptions-item>
        <el-descriptions-item label="合同ID">{{ detailForm.contractId }}</el-descriptions-item>
        <el-descriptions-item label="申请金额">{{ detailForm.loanAmount }}</el-descriptions-item>
        <el-descriptions-item label="利率">{{ detailForm.interestRate }}</el-descriptions-item>
        <el-descriptions-item label="期限（月）">{{ detailForm.termMonths }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusLabel(detailForm.status) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailForm.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detailForm.updatedAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 批准对话框 -->
    <el-dialog title="批准贷款" v-model="approveOpen" width="500px" append-to-body>
      <el-form ref="approveRef" :model="approveForm" label-width="120px">
        <el-form-item label="实际放款金额">
          <el-input v-model="approveForm.actualLoanAmount" placeholder="请输入实际放款金额" />
        </el-form-item>
        <el-form-item label="实际利率">
          <el-input v-model="approveForm.actualInterestRate" placeholder="请输入实际利率" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="approveForm.comment" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitApprove">确 定</el-button>
          <el-button @click="approveOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 拒绝对话框 -->
    <el-dialog title="拒绝贷款" v-model="rejectOpen" width="500px" append-to-body>
      <el-form ref="rejectRef" :model="rejectForm" label-width="80px">
        <el-form-item label="拒绝原因">
          <el-input v-model="rejectForm.comment" type="textarea" placeholder="请输入拒绝原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitReject">确 定</el-button>
          <el-button @click="rejectOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listLoanAudit, getLoanAudit, approveLoanAudit, rejectLoanAudit } from "@/api/finance/loanAudit"
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()

const loanAuditList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const detailOpen = ref(false)
const approveOpen = ref(false)
const rejectOpen = ref(false)
const detailForm = ref({})
const approveForm = ref({})
const rejectForm = ref({})
const currentId = ref(null)

const statusOptions = {
  0: '待接收',
  1: '审核中',
  2: '银行审核',
  3: '已批准',
  4: '已拒绝'
}

function getStatusLabel(status) {
  return statusOptions[status] || '未知'
}

function getStatusType(status) {
  const types = { 0: 'info', 1: 'warning', 2: 'warning', 3: 'success', 4: 'danger' }
  return types[status] || 'info'
}

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    status: undefined
  }
})

const { queryParams } = toRefs(data)

/** 查询贷款审核列表 */
function getList() {
  loading.value = true
  listLoanAudit(queryParams.value).then(response => {
    loanAuditList.value = response.data?.records || response.records || []
    total.value = response.data?.total || response.total || 0
    loading.value = false
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 查看详情 */
function handleView(row) {
  getLoanAudit(row.id).then(response => {
    detailForm.value = response.data || response
    detailOpen.value = true
  })
}

/** 批准 */
function handleApprove(row) {
  currentId.value = row.id
  approveForm.value = {
    actualLoanAmount: row.loanAmount,
    actualInterestRate: row.interestRate,
    comment: '',
    operatorId: userStore.id,
    operatorName: userStore.name,
    operatorRole: 'finance'
  }
  approveOpen.value = true
}

function submitApprove() {
  approveLoanAudit(currentId.value, approveForm.value).then(() => {
    proxy.$modal.msgSuccess("批准成功")
    approveOpen.value = false
    getList()
  })
}

/** 拒绝 */
function handleReject(row) {
  currentId.value = row.id
  rejectForm.value = {
    comment: '',
    operatorId: userStore.id,
    operatorName: userStore.name,
    operatorRole: 'finance'
  }
  rejectOpen.value = true
}

function submitReject() {
  rejectLoanAudit(currentId.value, rejectForm.value).then(() => {
    proxy.$modal.msgSuccess("已拒绝")
    rejectOpen.value = false
    getList()
  })
}

getList()
</script>

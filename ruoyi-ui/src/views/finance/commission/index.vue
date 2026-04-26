<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="待确认" :value="0" />
          <el-option label="已确认" :value="1" />
          <el-option label="已发放" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="commissionList" :row-key="row => row.id" @sort-change="handleSortChange">
      <el-table-column label="ID" align="center" prop="id" width="80" sortable />
      <el-table-column label="销售代表" align="center" prop="salesRepName" />
      <el-table-column label="合同" align="center" prop="contractNo" />
      <el-table-column label="佣金金额" align="center" prop="commissionAmount" />
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
          <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-if="scope.row.status === 0">修改</el-button>
          <el-button link type="primary" icon="Check" @click="handleConfirm(scope.row)" v-if="scope.row.status === 0">确认</el-button>
          <el-button link type="primary" icon="Money" @click="handleGrant(scope.row)" v-if="scope.row.status === 1">发放</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 修改对话框 -->
    <el-dialog title="修改提成记录" v-model="editOpen" width="600px" append-to-body>
      <el-form ref="editRef" :model="editForm" label-width="100px">
        <el-form-item label="销售代表">
          <el-select
            v-model="editForm.salesRepId"
            filterable
            remote
            reserve-keyword
            placeholder="请输入销售代表名称搜索"
            :remote-method="searchSalesReps"
            :loading="salesRepLoading"
            @focus="loadSalesReps"
            style="width: 100%">
            <el-option
              v-for="item in salesRepOptions"
              :key="item.id"
              :label="item.realName"
              :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="合同">
          <el-select
            v-model="editForm.contractId"
            filterable
            remote
            reserve-keyword
            placeholder="请输入合同编号搜索"
            :remote-method="searchContracts"
            :loading="contractLoading"
            @focus="loadContracts"
            style="width: 100%">
            <el-option
              v-for="item in contractOptions"
              :key="item.id"
              :label="item.contractNo"
              :value="item.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitEdit">确 定</el-button>
          <el-button @click="editOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 发放对话框 -->
    <el-dialog title="发放佣金" v-model="grantOpen" width="500px" append-to-body>
      <el-form ref="grantRef" :model="grantForm" label-width="100px">
        <el-form-item label="发放账户">
          <el-input v-model="grantForm.grantAccount" placeholder="请输入发放账户" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="grantForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitGrant">确 定</el-button>
          <el-button @click="grantOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listCommission, getCommission, updateCommission, confirmCommission, grantCommission } from "@/api/finance/commission"
import { listSalesReps } from "@/api/system/user"
import { listContractForSelect } from "@/api/sales/contract"

const { proxy } = getCurrentInstance()

const commissionList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)

// 修改对话框
const editOpen = ref(false)
const editForm = ref({})
const salesRepOptions = ref([])
const contractOptions = ref([])
const salesRepLoading = ref(false)
const contractLoading = ref(false)

// 发放对话框
const grantOpen = ref(false)
const grantForm = ref({})
const currentId = ref(null)

const statusOptions = {
  0: '待确认',
  1: '已确认',
  2: '已发放'
}

function getStatusLabel(status) {
  return statusOptions[status] || '未知'
}

function getStatusType(status) {
  const types = { 0: 'info', 1: 'warning', 2: 'success' }
  return types[status] || 'info'
}

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    status: undefined,
    sortField: 'id',
    sortOrder: 'asc'
  }
})

const { queryParams } = toRefs(data)

/** 查询佣金记录列表 */
function getList() {
  loading.value = true
  listCommission(queryParams.value).then(response => {
    commissionList.value = response.data?.records || response.records || []
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

/** 修改按钮 */
function handleEdit(row) {
  editForm.value = {
    id: row.id,
    salesRepId: row.salesRepId,
    contractId: row.contractId
  }
  salesRepOptions.value = []
  contractOptions.value = []
  editOpen.value = true
}

/** 加载销售代表列表 */
function loadSalesReps() {
  listSalesReps({}).then(response => {
    salesRepOptions.value = response.data || []
  })
}

/** 搜索销售代表 */
function searchSalesReps(query) {
  if (query === '') {
    loadSalesReps()
    return
  }
  salesRepLoading.value = true
  listSalesReps({}).then(response => {
    const all = response.data || []
    if (query) {
      salesRepOptions.value = all.filter(item =>
        item.realName && item.realName.toLowerCase().includes(query.toLowerCase())
      )
    } else {
      salesRepOptions.value = all
    }
    salesRepLoading.value = false
  })
}

/** 加载合同列表 */
function loadContracts() {
  listContractForSelect({}).then(response => {
    contractOptions.value = response.data?.records || response.records || []
  })
}

/** 搜索合同 */
function searchContracts(query) {
  if (query === '') {
    loadContracts()
    return
  }
  contractLoading.value = true
  listContractForSelect({ contractNo: query }).then(response => {
    contractOptions.value = response.data?.records || response.records || []
    contractLoading.value = false
  })
}

/** 提交修改 */
function submitEdit() {
  updateCommission(editForm.value).then(() => {
    proxy.$modal.msgSuccess("修改成功")
    editOpen.value = false
    getList()
  })
}

/** 确认佣金 */
function handleConfirm(row) {
  proxy.$modal.confirm('是否确认该佣金记录？').then(function () {
    return confirmCommission(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("确认成功")
  }).catch(() => {})
}

/** 发放佣金 */
function handleGrant(row) {
  currentId.value = row.id
  grantForm.value = {
    grantAccount: '',
    remark: ''
  }
  grantOpen.value = true
}

function submitGrant() {
  grantCommission(currentId.value, grantForm.value).then(() => {
    proxy.$modal.msgSuccess("发放成功")
    grantOpen.value = false
    getList()
  })
}

getList()
</script>

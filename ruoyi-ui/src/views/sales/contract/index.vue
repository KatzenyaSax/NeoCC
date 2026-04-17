<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="合同编号" prop="contractNo">
        <el-input v-model="queryParams.contractNo" placeholder="请输入合同编号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="contractList">
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="合同编号" align="center" prop="contractNo" />
      <el-table-column label="客户ID" align="center" prop="customerId" />
      <el-table-column label="贷款金额" align="center" prop="loanAmount" />
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
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="primary" icon="Check" @click="handleSign(scope.row)" v-if="scope.row.status === 0">签署</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 添加或修改合同对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="contractRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="合同编号" prop="contractNo">
          <el-input v-model="form.contractNo" placeholder="请输入合同编号" />
        </el-form-item>
        <el-form-item label="客户ID" prop="customerId">
          <el-input v-model="form.customerId" placeholder="请输入客户ID" />
        </el-form-item>
        <el-form-item label="贷款金额" prop="loanAmount">
          <el-input v-model="form.loanAmount" placeholder="请输入贷款金额" />
        </el-form-item>
        <el-form-item label="利率" prop="interestRate">
          <el-input v-model="form.interestRate" placeholder="请输入利率" />
        </el-form-item>
        <el-form-item label="期限（月）" prop="termMonths">
          <el-input v-model="form.termMonths" placeholder="请输入期限" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listContract, getContract, delContract, addContract, updateContract, signContract } from "@/api/sales/contract"

const { proxy } = getCurrentInstance()

const contractList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const open = ref(false)

const statusOptions = {
  0: '待签署',
  1: '已签署',
  2: '审核中',
  3: '已批准',
  4: '已拒绝'
}

function getStatusLabel(status) {
  return statusOptions[status] || '未知'
}

function getStatusType(status) {
  const types = { 0: 'info', 1: 'success', 2: 'warning', 3: 'success', 4: 'danger' }
  return types[status] || 'info'
}

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    contractNo: undefined
  },
  rules: {
    contractNo: [{ required: true, message: "合同编号不能为空", trigger: "blur" }],
    customerId: [{ required: true, message: "客户ID不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询合同列表 */
function getList() {
  loading.value = true
  listContract(queryParams.value).then(response => {
    contractList.value = response.data?.records || response.records || []
    total.value = response.data?.total || response.total || 0
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    id: undefined,
    contractNo: undefined,
    customerId: undefined,
    loanAmount: undefined,
    interestRate: undefined,
    termMonths: undefined
  }
  proxy.resetForm("contractRef")
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

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加合同"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const id = row.id
  getContract(id).then(response => {
    form.value = response.data || response
    open.value = true
    title.value = "修改合同"
  })
}

/** 签署合同 */
function handleSign(row) {
  proxy.$modal.confirm('是否确认签署合同"' + row.contractNo + '"？').then(function () {
    return signContract(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("签署成功")
  }).catch(() => {})
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["contractRef"].validate(valid => {
    if (valid) {
      if (form.value.id != undefined) {
        updateContract(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addContract(form.value).then(response => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除合同"' + row.contractNo + '"？').then(function () {
    return delContract(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>

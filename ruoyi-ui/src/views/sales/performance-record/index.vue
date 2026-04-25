<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="销售代表ID" prop="salesRepId">
        <el-input v-model="queryParams.salesRepId" placeholder="请输入销售代表ID" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="待确认" :value="0" />
          <el-option label="已确认" :value="1" />
          <el-option label="已发放" :value="2" />
          <el-option label="已取消" :value="3" />
        </el-select>
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

    <el-table v-loading="loading" :data="dataList" :row-key="row => row.id" @sort-change="handleSortChange">
      <el-table-column label="ID" align="center" prop="id" width="80" sortable />
      <el-table-column label="合同ID" align="center" prop="contractId" width="90" />
      <el-table-column label="销售代表ID" align="center" prop="salesRepId" width="110" />
      <el-table-column label="部门ID" align="center" prop="deptId" width="90" />
      <el-table-column label="区域ID" align="center" prop="zoneId" width="90" />
      <el-table-column label="合同金额" align="center" prop="contractAmount" width="120" />
      <el-table-column label="佣金比例" align="center" prop="commissionRate" width="100">
        <template #default="scope">{{ scope.row.commissionRate }}%</template>
      </el-table-column>
      <el-table-column label="佣金金额" align="center" prop="commissionAmount" width="120" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag :type="statusType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="计算时间" align="center" prop="calculateTime" width="160" />
      <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="合同ID" prop="contractId">
              <el-input v-model="form.contractId" placeholder="请输入合同ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售代表ID" prop="salesRepId">
              <el-input v-model="form.salesRepId" placeholder="请输入销售代表ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="部门ID" prop="deptId">
              <el-input v-model="form.deptId" placeholder="请输入部门ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="区域ID" prop="zoneId">
              <el-input v-model="form.zoneId" placeholder="请输入区域ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="合同金额" prop="contractAmount">
              <el-input v-model="form.contractAmount" placeholder="请输入合同金额" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="佣金比例(%)" prop="commissionRate">
              <el-input v-model="form.commissionRate" placeholder="请输入佣金比例" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="佣金金额" prop="commissionAmount">
              <el-input v-model="form.commissionAmount" placeholder="请输入佣金金额" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" style="width:100%">
                <el-option label="待确认" :value="0" />
                <el-option label="已确认" :value="1" />
                <el-option label="已发放" :value="2" />
                <el-option label="已取消" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="计算时间" prop="calculateTime">
              <el-date-picker v-model="form.calculateTime" type="datetime" placeholder="请选择计算时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认时间" prop="confirmTime">
              <el-date-picker v-model="form.confirmTime" type="datetime" placeholder="请选择确认时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="取消原因" prop="cancelReason" v-if="form.status === 3">
          <el-input v-model="form.cancelReason" type="textarea" :rows="2" placeholder="请输入取消原因" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listPerformanceRecord, getPerformanceRecord, addPerformanceRecord, updatePerformanceRecord, delPerformanceRecord, getMinUnusedIdPerformanceRecord } from "@/api/sales/performanceRecord"

const { proxy } = getCurrentInstance()
const dataList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const open = ref(false)

const statusMap = { 0: '待确认', 1: '已确认', 2: '已发放', 3: '已取消' }
const statusTypeMap = { 0: 'info', 1: 'warning', 2: 'success', 3: 'danger' }
function statusLabel(val) { return statusMap[val] !== undefined ? statusMap[val] : '-' }
function statusType(val) { return statusTypeMap[val] || 'info' }

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    salesRepId: undefined,
    status: undefined,
    sortField: 'id',
    sortOrder: 'asc'
  },
  rules: {
    contractId: [{ required: true, message: "合同ID不能为空", trigger: "blur" }],
    salesRepId: [{ required: true, message: "销售代表ID不能为空", trigger: "blur" }],
    contractAmount: [{ required: true, message: "合同金额不能为空", trigger: "blur" }],
    commissionRate: [{ required: true, message: "佣金比例不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listPerformanceRecord(queryParams.value).then(response => {
    dataList.value = response.data?.records || response.records || []
    total.value = response.data?.total || response.total || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}

function cancel() { open.value = false; reset() }
function reset() {
  form.value = {
    id: undefined, contractId: undefined, salesRepId: undefined,
    deptId: undefined, zoneId: undefined, contractAmount: undefined,
    commissionRate: undefined, commissionAmount: undefined, status: 0,
    calculateTime: undefined, confirmTime: undefined, cancelReason: undefined, remark: undefined
  }
  proxy.resetForm("formRef")
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }

function handleSortChange({ prop, order }) {
  queryParams.value.sortField = prop
  queryParams.value.sortOrder = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  getList()
}

function handleAdd() { reset(); getMinUnusedIdPerformanceRecord().then(res => { form.value.id = res.data; open.value = true; title.value = "新增业绩记录" }) }

function handleUpdate(row) {
  reset()
  getPerformanceRecord(row.id).then(response => {
    form.value = response.data || response
    open.value = true
    title.value = "修改业绩记录"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (!valid) return
    const fn = form.value.id ? updatePerformanceRecord : addPerformanceRecord
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.id ? "修改成功" : "新增成功")
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除该业绩记录？').then(() => {
    return updatePerformanceRecord({ id: row.id, deleted: 1 })
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>

<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="销售代表ID" prop="salesRepId">
        <el-input v-model="queryParams.salesRepId" placeholder="请输入销售代表ID" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="待确认" :value="1" />
          <el-option label="已确认" :value="2" />
          <el-option label="已发放" :value="3" />
          <el-option label="已取消" :value="4" />
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

    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="合同ID" align="center" prop="contractId" width="90">
        <template #default="scope">
          <span class="contract-id">{{ formatContractId(scope.row.contractId) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="销售代表ID" align="center" prop="salesRepId" width="110" />
      <el-table-column label="部门ID" align="center" prop="deptId" width="90" />
      <el-table-column label="区域ID" align="center" prop="zoneId" width="90" />
      <el-table-column label="合同金额" align="center" prop="contractAmount" width="120">
        <template #default="scope">{{ formatMoney(scope.row.contractAmount) }}</template>
      </el-table-column>
      <el-table-column label="佣金比例" align="center" prop="commissionRate" width="100">
        <template #default="scope">{{ (scope.row.commissionRate * 100).toFixed(2) }}%</template>
      </el-table-column>
      <el-table-column label="佣金金额" align="center" prop="commissionAmount" width="120">
        <template #default="scope">{{ formatMoney(scope.row.commissionAmount) }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag :type="statusType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="计算时间" align="center" prop="calculateTime" width="160" />
      <el-table-column label="备注" align="center" prop="remark" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="220" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-if="scope.row.status === 1">修改</el-button>
          <el-button link type="success" icon="Check" @click="handleConfirm(scope.row)" v-if="scope.row.status === 1">确认</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.status === 1">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="700px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="关联合同" prop="contractId">
              <el-select v-model="form.contractId" placeholder="请选择合同" filterable @change="handleContractChange" style="width: 100%">
                <el-option v-for="c in contractList" :key="c.id" :label="c.contractNo + ' - ' + c.contractAmount + '元'" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="销售代表ID" prop="salesRepId">
              <el-input v-model="form.salesRepId" placeholder="自动填充" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门ID" prop="deptId">
              <el-input v-model="form.deptId" placeholder="自动填充" readonly />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="合同金额" prop="contractAmount">
              <el-input v-model="form.contractAmount" placeholder="自动填充" readonly />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="佣金比例(%)" prop="commissionRate">
              <el-input-number v-model="form.commissionRatePercent" :precision="2" :min="0" :max="100" @change="calcCommissionAmount" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="佣金金额" prop="commissionAmount">
              <el-input-number v-model="form.commissionAmount" :precision="2" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" style="width:100%">
                <el-option label="待确认" :value="1" />
                <el-option label="已确认" :value="2" />
                <el-option label="已发放" :value="3" />
                <el-option label="已取消" :value="4" />
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
              <el-date-picker v-model="form.confirmTime" type="datetime" placeholder="请选择确认时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" readonly />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="取消原因" prop="cancelReason" v-if="form.status === 4">
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
import { listPerformanceRecord, getPerformanceRecord, addPerformanceRecord, updatePerformanceRecord, delPerformanceRecord, confirmPerformanceRecord, getContractDetailWithRate } from "@/api/sales/performanceRecord"
import { listContract } from "@/api/sales/contract"

const { proxy } = getCurrentInstance()
const dataList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const open = ref(false)
const contractList = ref([])

const statusMap = { 1: '待确认', 2: '已确认', 3: '已发放', 4: '已取消' }
const statusTypeMap = { 1: 'info', 2: 'success', 3: 'warning', 4: 'danger' }
function statusLabel(val) { return statusMap[val] !== undefined ? statusMap[val] : '-' }
function statusType(val) { return statusTypeMap[val] || 'info' }

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    salesRepId: undefined,
    status: undefined
  },
  rules: {
    contractId: [{ required: true, message: "合同ID不能为空", trigger: "blur" }],
    salesRepId: [{ required: true, message: "销售代表ID不能为空", trigger: "blur" }],
    contractAmount: [{ required: true, message: "合同金额不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

async function getList() {
  loading.value = true
  try {
    const response = await listPerformanceRecord(queryParams.value)
    dataList.value = response.data?.records || response.records || []
    total.value = response.data?.total || response.total || 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function loadContractList() {
  try {
    const res = await listContract({ pageNum: 1, pageSize: 100 })
    contractList.value = res.data?.records || res.records || []
  } catch (e) {
    console.error("加载合同列表失败", e)
  }
}

async function handleContractChange(contractId) {
  if (!contractId) return
  try {
    const res = await getContractDetailWithRate(contractId)
    if (res.code === 200 && res.data) {
      const d = res.data
      form.value.salesRepId = d.salesRepId
      form.value.deptId = d.deptId
      form.value.contractAmount = d.contractAmount
      // 设置佣金比例（小数转百分比）
      if (d.commissionRate) {
        form.value.commissionRatePercent = parseFloat((d.commissionRate * 100).toFixed(2))
        form.value.commissionRate = d.commissionRate
      }
      // 自动计算佣金金额
      calcCommissionAmount()
    }
  } catch (e) {
    console.error("获取合同详情失败", e)
  }
}

function calcCommissionAmount() {
  if (form.value.contractAmount && form.value.commissionRatePercent) {
    form.value.commissionAmount = parseFloat((form.value.contractAmount * form.value.commissionRatePercent / 100).toFixed(2))
    form.value.commissionRate = parseFloat((form.value.commissionRatePercent / 100).toFixed(6))
  }
}

function cancel() { open.value = false; reset() }

function reset() {
  form.value = {
    id: undefined,
    contractId: undefined,
    salesRepId: undefined,
    deptId: undefined,
    zoneId: undefined,
    contractAmount: undefined,
    commissionRatePercent: 10,
    commissionRate: 0.1,
    commissionAmount: undefined,
    status: 1,
    calculateTime: undefined,
    confirmTime: undefined,
    cancelReason: undefined,
    remark: undefined
  }
  proxy.resetForm("formRef")
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); queryParams.value.status = undefined; handleQuery() }
async function handleAdd() {
  reset()
  await loadContractList()
  form.value.calculateTime = new Date().format('yyyy-MM-dd HH:mm:ss')
  open.value = true
  title.value = "新增业绩记录"
}

function handleUpdate(row) {
  reset()
  getPerformanceRecord(row.id).then(response => {
    const d = response.data || response
    form.value = { ...d }
    // 显示百分比形式
    form.value.commissionRatePercent = parseFloat((d.commissionRate * 100).toFixed(2))
    open.value = true
    title.value = "修改业绩记录"
  })
}

async function handleConfirm(row) {
  try {
    await proxy.$modal.confirm('是否确认该业绩记录？确认后将计入业绩汇总。')
    await confirmPerformanceRecord(row.id)
    proxy.$modal.msgSuccess("确认成功")
    getList()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (!valid) return
    // 恢复 commissionRate（小数形式）
    const submitData = { ...form.value }
    delete submitData.commissionRatePercent
    const fn = submitData.id ? updatePerformanceRecord : addPerformanceRecord
    fn(submitData).then(() => {
      proxy.$modal.msgSuccess(submitData.id ? "修改成功" : "新增成功")
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除该业绩记录？').then(() => delPerformanceRecord(row.id)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

function formatMoney(val) {
  if (val == null) return '-'
  return parseFloat(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatContractId(id) {
  if (!id) return '-'
  return String(id).slice(-6) // 显示后6位
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.mb8 { margin-bottom: 8px; }
.contract-id {
  font-family: monospace;
  color: #409eff;
}
</style>

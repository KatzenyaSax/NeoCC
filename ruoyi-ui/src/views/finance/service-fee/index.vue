<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="合同ID" prop="contractId">
        <el-input v-model="queryParams.contractId" placeholder="请输入合同ID" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="支付状态" prop="paymentStatus">
        <el-select v-model="queryParams.paymentStatus" placeholder="请选择状态" clearable>
          <el-option label="未支付" :value="0" />
          <el-option label="已支付" :value="1" />
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
      <el-table-column label="合同ID" align="center" prop="contractId" width="100" />
      <el-table-column label="费用类型" align="center" prop="feeType" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.feeType === 1 ? 'primary' : 'warning'">
            {{ scope.row.feeType === 1 ? '首期' : '二期' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="应收金额" align="center" prop="shouldAmount" />
      <el-table-column label="实收金额" align="center" prop="amount" />
      <el-table-column label="支付方式" align="center" prop="paymentMethod" />
      <el-table-column label="支付状态" align="center" prop="paymentStatus">
        <template #default="scope">
          <el-tag :type="scope.row.paymentStatus === 1 ? 'success' : 'danger'">
            {{ scope.row.paymentStatus === 1 ? '已支付' : '未支付' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付日期" align="center" prop="paymentDate" width="120" />
      <el-table-column label="收据号" align="center" prop="receiptNo" />
      <el-table-column label="操作" align="center" width="220">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="success" icon="Check" @click="handlePay(scope.row)" v-if="scope.row.paymentStatus === 0">确认收款</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="550px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="合同ID" prop="contractId">
          <el-input v-model="form.contractId" placeholder="请输入合同ID" />
        </el-form-item>
        <el-form-item label="费用类型" prop="feeType">
          <el-select v-model="form.feeType" placeholder="请选择费用类型">
            <el-option label="首期服务费" :value="1" />
            <el-option label="二期服务费" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="应收金额" prop="shouldAmount">
          <el-input v-model="form.shouldAmount" placeholder="请输入应收金额" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 确认收款对话框 -->
    <el-dialog title="确认收款" v-model="payOpen" width="500px" append-to-body>
      <el-form ref="payFormRef" :model="payForm" label-width="100px">
        <el-form-item label="实收金额" prop="amount">
          <el-input v-model="payForm.amount" placeholder="请输入实收金额" />
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
          <el-select v-model="payForm.paymentMethod" placeholder="请选择支付方式">
            <el-option label="现金" value="cash" />
            <el-option label="银行转账" value="bank_transfer" />
            <el-option label="微信" value="wechat" />
            <el-option label="支付宝" value="alipay" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付账户" prop="paymentAccount">
          <el-input v-model="payForm.paymentAccount" placeholder="请输入支付账户" />
        </el-form-item>
        <el-form-item label="收据号" prop="receiptNo">
          <el-input v-model="payForm.receiptNo" placeholder="请输入收据号" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="payForm.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitPay">确 定</el-button>
        <el-button @click="payOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listServiceFeeRecord, getServiceFeeRecord, addServiceFeeRecord, updateServiceFeeRecord, delServiceFeeRecord, confirmPay, getMinUnusedServiceFeeRecordId } from "@/api/finance/serviceFeeRecord"

const { proxy } = getCurrentInstance()
const dataList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const open = ref(false)
const payOpen = ref(false)
const currentPayId = ref(null)

const data = reactive({
  form: {},
  payForm: {},
  queryParams: { pageNum: 1, pageSize: 10, contractId: undefined, paymentStatus: undefined, sortField: 'id', sortOrder: 'asc' },
  rules: {
    contractId: [{ required: true, message: "合同ID不能为空", trigger: "blur" }],
    feeType: [{ required: true, message: "费用类型不能为空", trigger: "change" }],
    shouldAmount: [{ required: true, message: "应收金额不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules, payForm } = toRefs(data)

function getList() {
  loading.value = true
  listServiceFeeRecord(queryParams.value).then(response => {
    dataList.value = response.data?.records || response.records || []
    total.value = response.data?.total || response.total || 0
    loading.value = false
  })
}

function cancel() { open.value = false; reset() }
function reset() {
  form.value = { id: undefined, contractId: undefined, feeType: undefined, shouldAmount: undefined, remark: undefined }
  proxy.resetForm("formRef")
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }

function handleSortChange({ prop, order }) {
  queryParams.value.sortField = prop
  queryParams.value.sortOrder = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  getList()
}

function handleAdd() {
  reset()
  getMinUnusedServiceFeeRecordId().then(response => {
    form.value.id = response.data || response
    open.value = true
    title.value = "新增服务费记录"
  })
}

function handleUpdate(row) {
  reset()
  getServiceFeeRecord(row.id).then(response => {
    form.value = response.data || response
    open.value = true
    title.value = "修改服务费记录"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      const isAdd = title.value.includes('新增')
      const fn = isAdd ? addServiceFeeRecord : updateServiceFeeRecord
      fn(form.value).then(() => {
        proxy.$modal.msgSuccess(isAdd ? "新增成功" : "修改成功")
        open.value = false
        getList()
      })
    }
  })
}

function handlePay(row) {
  currentPayId.value = row.id
  payForm.value = { amount: row.shouldAmount, paymentMethod: '', paymentAccount: '', receiptNo: '', remark: '' }
  payOpen.value = true
}

function submitPay() {
  confirmPay(currentPayId.value, payForm.value).then(() => {
    proxy.$modal.msgSuccess("收款确认成功")
    payOpen.value = false
    getList()
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除该服务费记录？').then(() => updateServiceFeeRecord({ id: row.id, deleted: 1 })).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>

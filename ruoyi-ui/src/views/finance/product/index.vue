<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="产品名称" prop="productName">
        <el-input v-model="queryParams.productName" placeholder="请输入产品名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="上架" :value="1" />
          <el-option label="下架" :value="0" />
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
      <el-table-column label="产品代码" align="center" prop="productCode" width="120" />
      <el-table-column label="产品名称" align="center" prop="productName" />
      <el-table-column label="银行ID" align="center" prop="bankId" width="90" />
      <el-table-column label="金额范围" align="center" width="180">
        <template #default="scope">{{ scope.row.minAmount }} ~ {{ scope.row.maxAmount }}</template>
      </el-table-column>
      <el-table-column label="利率" align="center" prop="interestRate" width="90" />
      <el-table-column label="期限(月)" align="center" width="120">
        <template #default="scope">{{ scope.row.minTerm }} ~ {{ scope.row.maxTerm }}</template>
      </el-table-column>
      <el-table-column label="佣金率" align="center" prop="commissionRate" width="90" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="产品代码" prop="productCode">
          <el-input v-model="form.productCode" placeholder="请输入产品代码" />
        </el-form-item>
        <el-form-item label="产品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="银行ID" prop="bankId">
          <el-input v-model="form.bankId" placeholder="请输入银行ID" />
        </el-form-item>
        <el-form-item label="最低金额" prop="minAmount">
          <el-input v-model="form.minAmount" placeholder="请输入最低金额" />
        </el-form-item>
        <el-form-item label="最高金额" prop="maxAmount">
          <el-input v-model="form.maxAmount" placeholder="请输入最高金额" />
        </el-form-item>
        <el-form-item label="利率" prop="interestRate">
          <el-input v-model="form.interestRate" placeholder="请输入年利率（如0.05表示5%）" />
        </el-form-item>
        <el-form-item label="最短期限(月)" prop="minTerm">
          <el-input-number v-model="form.minTerm" :min="1" />
        </el-form-item>
        <el-form-item label="最长期限(月)" prop="maxTerm">
          <el-input-number v-model="form.maxTerm" :min="1" />
        </el-form-item>
        <el-form-item label="佣金率" prop="commissionRate">
          <el-input v-model="form.commissionRate" placeholder="请输入佣金率" />
        </el-form-item>
        <el-form-item label="产品特点" prop="productFeatures">
          <el-input v-model="form.productFeatures" type="textarea" placeholder="请输入产品特点" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
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
import { listFinanceProduct, getFinanceProduct, addFinanceProduct, updateFinanceProduct, delFinanceProduct } from "@/api/finance/financeProduct"

const { proxy } = getCurrentInstance()
const dataList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const open = ref(false)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, productName: undefined, status: undefined, sortField: 'id', sortOrder: 'asc' },
  rules: {
    productCode: [{ required: true, message: "产品代码不能为空", trigger: "blur" }],
    productName: [{ required: true, message: "产品名称不能为空", trigger: "blur" }],
    bankId: [{ required: true, message: "银行ID不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listFinanceProduct(queryParams.value).then(response => {
    dataList.value = response.data?.records || response.records || []
    total.value = response.data?.total || response.total || 0
    loading.value = false
  })
}

function cancel() { open.value = false; reset() }
function reset() {
  form.value = { id: undefined, productCode: undefined, productName: undefined, bankId: undefined, minAmount: undefined, maxAmount: undefined, interestRate: undefined, minTerm: 1, maxTerm: 60, commissionRate: undefined, productFeatures: undefined, status: 1 }
  proxy.resetForm("formRef")
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }

function handleSortChange({ prop, order }) {
  queryParams.value.sortField = prop
  queryParams.value.sortOrder = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  getList()
}

function handleAdd() { reset(); open.value = true; title.value = "新增金融产品" }

function handleUpdate(row) {
  reset()
  getFinanceProduct(row.id).then(response => {
    form.value = response.data || response
    open.value = true
    title.value = "修改金融产品"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      const fn = form.value.id ? updateFinanceProduct : addFinanceProduct
      fn(form.value).then(() => {
        proxy.$modal.msgSuccess(form.value.id ? "修改成功" : "新增成功")
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除产品"' + row.productName + '"？').then(() => delFinanceProduct(row.id)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>

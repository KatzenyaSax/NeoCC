<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="客户ID" prop="customerId">
        <el-input v-model="queryParams.customerId" placeholder="请输入客户ID" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="跟进方式" prop="contactType">
        <el-select v-model="queryParams.contactType" placeholder="请选择" clearable>
          <el-option label="电话" :value="1" />
          <el-option label="微信" :value="2" />
          <el-option label="面谈" :value="3" />
          <el-option label="其他" :value="4" />
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
      <el-table-column label="客户" align="center" prop="customerName" width="120">
        <template #default="scope">{{ customerNameMap[scope.row.customerId] || scope.row.customerId }}</template>
      </el-table-column>
      <el-table-column label="对接销售代表" align="center" prop="salesRepName" width="120">
        <template #default="scope">{{ salesRepNameMap[scope.row.salesRepId] || scope.row.salesRepId }}</template>
      </el-table-column>
      <el-table-column label="跟进方式" align="center" prop="contactType" width="100">
        <template #default="scope">
          <el-tag>{{ contactTypeLabel(scope.row.contactType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="跟进日期" align="center" prop="contactDate" width="120" />
      <el-table-column label="跟进内容" align="center" prop="content" show-overflow-tooltip />
      <el-table-column label="意向(前)" align="center" prop="intentionBefore" width="90">
        <template #default="scope">{{ intentionLabel(scope.row.intentionBefore) }}</template>
      </el-table-column>
      <el-table-column label="意向(后)" align="center" prop="intentionAfter" width="90">
        <template #default="scope">{{ intentionLabel(scope.row.intentionAfter) }}</template>
      </el-table-column>
      <el-table-column label="下次跟进" align="center" prop="followUpDate" width="120" />
      <el-table-column label="操作" align="center" width="160">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="客户" prop="customerId">
          <el-select
            v-model="form.customerId"
            placeholder="请选择客户"
            filterable
            :loading="customerLoading"
            @focus="loadCustomerOptions('')"
            :remote="true"
            :remote-method="loadCustomerOptions"
            style="width: 100%">
            <el-option
              v-for="item in customerOptions"
              :key="item.id"
              :label="item.realName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="跟进方式" prop="contactType">
          <el-select v-model="form.contactType" placeholder="请选择跟进方式">
            <el-option label="电话" :value="1" />
            <el-option label="微信" :value="2" />
            <el-option label="面谈" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="跟进日期" prop="contactDate">
          <el-date-picker v-model="form.contactDate" type="date" placeholder="请选择跟进日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="跟进内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入跟进内容" />
        </el-form-item>
        <el-form-item label="跟进前意向" prop="intentionBefore">
          <el-select v-model="form.intentionBefore" placeholder="请选择">
            <el-option label="无意向" :value="0" />
            <el-option label="低意向" :value="1" />
            <el-option label="中意向" :value="2" />
            <el-option label="高意向" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="跟进后意向" prop="intentionAfter">
          <el-select v-model="form.intentionAfter" placeholder="请选择">
            <el-option label="无意向" :value="0" />
            <el-option label="低意向" :value="1" />
            <el-option label="中意向" :value="2" />
            <el-option label="高意向" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="下次跟进" prop="followUpDate">
          <el-date-picker v-model="form.followUpDate" type="date" placeholder="请选择下次跟进日期" value-format="YYYY-MM-DD" />
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
import { listContactRecord, getContactRecord, addContactRecord, updateContactRecord, delContactRecord } from "@/api/sales/contactRecord"
import { listCustomer } from "@/api/sales/customer"
import { listSalesReps } from "@/api/sales/publicSea"
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const dataList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const open = ref(false)
const customerOptions = ref([])
const customerLoading = ref(false)

const contactTypeMap = { 1: '电话', 2: '微信', 3: '面谈', 4: '其他' }
const intentionMap = { 0: '无意向', 1: '低意向', 2: '中意向', 3: '高意向' }
function contactTypeLabel(val) { return contactTypeMap[val] || '未知' }
function intentionLabel(val) { return intentionMap[val] || '-' }

// 客户名称映射
const customerNameMap = ref({})
// 销售代表名称映射
const salesRepNameMap = ref({})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, customerId: undefined, contactType: undefined },
  rules: {
    customerId: [{ required: true, message: "客户ID不能为空", trigger: "blur" }],
    contactType: [{ required: true, message: "跟进方式不能为空", trigger: "change" }],
    content: [{ required: true, message: "跟进内容不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listContactRecord(queryParams.value).then(response => {
    dataList.value = response.data?.records || response.records || []
    total.value = response.data?.total || response.total || 0
    loading.value = false
  })
}

/** 加载客户和销售代表名称映射 */
function loadNameMaps() {
  // 加载客户列表
  listCustomer({ pageNum: 1, pageSize: 1000, name: '' }).then(response => {
    const records = response.data?.records || response.records || []
    const map = {}
    records.forEach(c => { map[c.id] = c.name })
    customerNameMap.value = map
  })
  // 加载销售代表列表
  listSalesReps().then(response => {
    const reps = response.data || []
    const map = {}
    reps.forEach(r => { map[r.id] = r.realName })
    salesRepNameMap.value = map
  })
}

function cancel() { open.value = false; reset() }
function reset() {
  form.value = { id: undefined, customerId: undefined, contactType: undefined, contactDate: undefined, content: undefined, intentionBefore: undefined, intentionAfter: undefined, followUpDate: undefined }
  proxy.resetForm("formRef")
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function handleAdd() { reset(); loadCustomerOptions(''); open.value = true; title.value = "新增跟进记录" }

function handleUpdate(row) {
  reset()
  getContactRecord(row.id).then(response => {
    form.value = response.data || response
    loadCustomerOptions('')
    open.value = true
    title.value = "修改跟进记录"
  })
}

function loadCustomerOptions(searchValue) {
  customerLoading.value = true
  listCustomer({ pageNum: 1, pageSize: 100, name: searchValue || '' }).then(response => {
    const records = response.data?.records || response.records || []
    // 过滤掉公海客户(status=5)
    customerOptions.value = records
      .filter(c => c.status !== 5)
      .map(c => ({
        id: c.id,
        realName: c.name
      }))
    customerLoading.value = false
  }).catch(() => {
    customerLoading.value = false
  })
}

function isSalesRepRole() {
  const roles = userStore.roles || []
  return roles.some(r => r === 'ROLE_sales_rep')
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      // 如果是销售代表角色，自动填充当前用户的ID
      if (isSalesRepRole()) {
        form.value.salesRepId = userStore.id
        const fn = form.value.id ? updateContactRecord : addContactRecord
        fn(form.value).then(() => {
          proxy.$modal.msgSuccess(form.value.id ? "修改成功" : "新增成功")
          open.value = false
          getList()
        })
      } else {
        // 非销售代表角色不能提交
        proxy.$modal.msgError("您不是销售代表！")
      }
    }
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除该跟进记录？').then(() => delContactRecord(row.id)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

loadNameMaps()
getList()
</script>

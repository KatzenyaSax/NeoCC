<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="客户ID" prop="customerId">
        <el-input v-model="queryParams.customerId" placeholder="请输入客户ID" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="操作类型" prop="operateType">
        <el-select v-model="queryParams.operateType" placeholder="请选择" clearable>
          <el-option label="主动转移" value="TRANSFER" />
          <el-option label="离职转移" value="RESIGN" />
          <el-option label="调配转移" value="ADJUST" />
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
      <el-table-column label="客户ID" align="center" prop="customerId" width="100" />
      <el-table-column label="转出销售ID" align="center" prop="fromRepId" width="110" />
      <el-table-column label="转入销售ID" align="center" prop="toRepId" width="110" />
      <el-table-column label="操作类型" align="center" prop="operateType" width="110">
        <template #default="scope">
          <el-tag :type="operateTypeTag(scope.row.operateType)">{{ operateTypeLabel(scope.row.operateType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="转移原因" align="center" prop="reason" show-overflow-tooltip />
      <el-table-column label="操作人ID" align="center" prop="operatedBy" width="100" />
      <el-table-column label="操作时间" align="center" prop="operatedAt" width="160" />
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="客户ID" prop="customerId">
          <el-input v-model="form.customerId" placeholder="请输入客户ID" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="转出销售ID" prop="fromRepId">
              <el-input v-model="form.fromRepId" placeholder="请输入转出销售ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="转入销售ID" prop="toRepId">
              <el-input v-model="form.toRepId" placeholder="请输入转入销售ID" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="操作类型" prop="operateType">
          <el-select v-model="form.operateType" placeholder="请选择操作类型" style="width:100%">
            <el-option label="主动转移" value="TRANSFER" />
            <el-option label="离职转移" value="RESIGN" />
            <el-option label="调配转移" value="ADJUST" />
          </el-select>
        </el-form-item>
        <el-form-item label="转移原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入转移原因" />
        </el-form-item>
        <el-form-item label="操作人ID" prop="operatedBy">
          <el-input v-model="form.operatedBy" placeholder="请输入操作人ID" />
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
import { listCustomerTransfer, getCustomerTransfer, addCustomerTransfer, updateCustomerTransfer, delCustomerTransfer } from "@/api/sales/customerTransfer"
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const dataList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const open = ref(false)

const operateTypeLabelMap = { TRANSFER: '主动转移', RESIGN: '离职转移', ADJUST: '调配转移' }
const operateTypeTagMap = { TRANSFER: 'primary', RESIGN: 'danger', ADJUST: 'warning' }
function operateTypeLabel(val) { return operateTypeLabelMap[val] || val }
function operateTypeTag(val) { return operateTypeTagMap[val] || 'info' }

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    customerId: undefined,
    operateType: undefined,
    sortField: 'id',
    sortOrder: 'asc'
  },
  rules: {
    customerId: [{ required: true, message: "客户ID不能为空", trigger: "blur" }],
    fromRepId: [{ required: true, message: "转出销售ID不能为空", trigger: "blur" }],
    toRepId: [{ required: true, message: "转入销售ID不能为空", trigger: "blur" }],
    operateType: [{ required: true, message: "操作类型不能为空", trigger: "change" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listCustomerTransfer(queryParams.value).then(response => {
    let records = response.data?.records || response.records || []
    // 根据角色过滤
    const userId = userStore.id
    const deptId = userStore.deptId
    const zoneId = userStore.zoneId
    const roles = userStore.roles || []
    const isSalesRep = roles.some(r => r === 'ROLE_sales_rep')
    const isDeptManager = roles.some(r => r === 'ROLE_dept_manager')
    const isZoneDirector = roles.some(r => r === 'ROLE_zone_director')
    const isAdmin = roles.some(r => ['ROLE_admin', 'ROLE_super'].includes(r))

    if (isSalesRep && !isDeptManager && !isZoneDirector && !isAdmin) {
      // 销售代表只看自己参与的转移记录(from_rep_id或to_rep_id是当前用户)
      records = records.filter(item => item.fromRepId === userId || item.toRepId === userId)
    } else if (isDeptManager && !isZoneDirector && !isAdmin) {
      // 部门经理看本部门的转移记录（需要关联客户表获取dept_id，暂用from_rep_id的用户dept判断）
      // 这里简化处理：部门经理可见本部门销售代表的转移记录
      records = records // 实际需要后端支持按部门过滤
    } else if (isZoneDirector && !isAdmin) {
      // 战区总监看本战区的转移记录
      records = records // 实际需要后端支持按战区过滤
    }

    dataList.value = records
    total.value = records.length
    loading.value = false
  }).catch(() => { loading.value = false })
}

function cancel() { open.value = false; reset() }
function reset() {
  form.value = {
    id: undefined, customerId: undefined, fromRepId: undefined,
    toRepId: undefined, operateType: undefined, reason: undefined, operatedBy: undefined
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

function handleAdd() { reset(); open.value = true; title.value = "新增客户转移记录" }

function handleUpdate(row) {
  reset()
  getCustomerTransfer(row.id).then(response => {
    form.value = response.data || response
    open.value = true
    title.value = "修改客户转移记录"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (!valid) return
    const fn = form.value.id ? updateCustomerTransfer : addCustomerTransfer
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.id ? "修改成功" : "新增成功")
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除该转移记录？').then(() => delCustomerTransfer(row.id)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>

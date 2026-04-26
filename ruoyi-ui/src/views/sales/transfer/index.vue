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
      <el-table-column label="客户" align="center" prop="customerName" width="120" />
      <el-table-column label="转出销售" align="center" prop="fromRepName" width="120" />
      <el-table-column label="转入销售" align="center" prop="toRepName" width="120" />
      <el-table-column label="操作类型" align="center" prop="operateType" width="110">
        <template #default="scope">
          <el-tag :type="operateTypeTag(scope.row.operateType)">{{ operateTypeLabel(scope.row.operateType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="转移原因" align="center" prop="reason" show-overflow-tooltip />
      <el-table-column label="操作人" align="center" prop="operatedByName" width="100" />
      <el-table-column label="操作时间" align="center" prop="operatedAt" width="160" />
      <el-table-column label="操作" align="center" width="220" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button v-if="!isSalesRepOnly" link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="form.customerId" placeholder="请选择客户" style="width:100%" filterable>
            <el-option v-for="item in customerOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="转出销售" prop="fromRepId">
              <el-select v-model="form.fromRepId" placeholder="请选择转出销售" style="width:100%" filterable>
                <el-option v-for="item in salesRepOptions" :key="item.id" :label="item.realName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="转入销售" prop="toRepId">
              <el-select v-model="form.toRepId" placeholder="请选择转入销售" style="width:100%" filterable>
                <el-option v-for="item in salesRepOptions" :key="item.id" :label="item.realName" :value="item.id" />
              </el-select>
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
        <el-form-item label="操作人" prop="operatedByName">
          <el-input v-model="form.operatedByName" placeholder="操作人" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog :title="detailTitle" v-model="detailOpen" width="600px" append-to-body>
      <el-form ref="detailFormRef" :model="form" label-width="110px" disabled>
        <el-form-item label="客户">
          <el-select v-model="form.customerId" placeholder="请选择客户" style="width:100%">
            <el-option v-for="item in customerOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="转出销售">
              <el-select v-model="form.fromRepId" placeholder="请选择转出销售" style="width:100%">
                <el-option v-for="item in salesRepOptions" :key="item.id" :label="item.realName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="转入销售">
              <el-select v-model="form.toRepId" placeholder="请选择转入销售" style="width:100%">
                <el-option v-for="item in salesRepOptions" :key="item.id" :label="item.realName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="操作类型">
          <el-select v-model="form.operateType" placeholder="请选择操作类型" style="width:100%">
            <el-option label="主动转移" value="TRANSFER" />
            <el-option label="离职转移" value="RESIGN" />
            <el-option label="调配转移" value="ADJUST" />
          </el-select>
        </el-form-item>
        <el-form-item label="转移原因">
          <el-input v-model="form.reason" type="textarea" :rows="3" placeholder="请输入转移原因" />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="form.operatedByName" placeholder="操作人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listCustomerTransfer, getCustomerTransfer, addCustomerTransfer, updateCustomerTransfer, delCustomerTransfer, getMinUnusedTransferId } from "@/api/sales/customerTransfer"
import { getUserNamesByIds, getCustomerNamesByIds, listCustomer } from "@/api/sales/customer"
import { listSalesReps } from "@/api/system/user"
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const dataList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const open = ref(false)
const detailOpen = ref(false)
const detailTitle = ref("")

const customerOptions = ref([])
const salesRepOptions = ref([])

const isSalesRepOnly = computed(() => {
  const roles = userStore.roles || []
  return roles.some(r => r === 'ROLE_SALES_REP') &&
    !roles.some(r => ['ROLE_DEPT_MANAGER', 'ROLE_ZONE_DIRECTOR', 'ROLE_SUPER_ADMIN', 'ROLE_GENERAL_MANAGER'].includes(r))
})

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
    customerId: [{ required: true, message: "客户不能为空", trigger: "change" }],
    fromRepId: [{ required: true, message: "转出销售不能为空", trigger: "change" }],
    toRepId: [{ required: true, message: "转入销售不能为空", trigger: "change" }],
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
    const isSalesRep = roles.some(r => r === 'ROLE_SALES_REP')
    const isDeptManager = roles.some(r => r === 'ROLE_DEPT_MANAGER')
    const isZoneDirector = roles.some(r => r === 'ROLE_ZONE_DIRECTOR')
    const isAdmin = roles.some(r => ['ROLE_SUPER_ADMIN', 'ROLE_GENERAL_MANAGER'].includes(r))

    if (isSalesRep && !isDeptManager && !isZoneDirector && !isAdmin) {
      // 销售代表只看自己参与的转移记录(from_rep_id或to_rep_id是当前用户)
      records = records.filter(item => item.fromRepId === userId || item.toRepId === userId)
    } else if (isDeptManager && !isZoneDirector && !isAdmin) {
      // 部门经理看本部门的转移记录
      records = records
    } else if (isZoneDirector && !isAdmin) {
      // 战区总监看本战区的转移记录
      records = records
    }

    dataList.value = records
    total.value = records.length
    loading.value = false

    // 批量查询名称
    const allUserIds = records
      .flatMap(r => [r.fromRepId, r.toRepId, r.operatedBy])
      .filter(id => id != null)
    const allCustomerIds = records.map(r => r.customerId).filter(id => id != null)

    Promise.all([
      allUserIds.length > 0 ? getUserNamesByIds([...new Set(allUserIds)]) : Promise.resolve({ data: {} }),
      allCustomerIds.length > 0 ? getCustomerNamesByIds([...new Set(allCustomerIds)]) : Promise.resolve({ data: {} })
    ]).then(([userRes, customerRes]) => {
      const userNameMap = userRes.data || {}
      const customerNameMap = customerRes.data || {}
      dataList.value.forEach(r => {
        r.fromRepName = userNameMap[r.fromRepId] || r.fromRepId
        r.toRepName = userNameMap[r.toRepId] || r.toRepId
        r.operatedByName = userNameMap[r.operatedBy] || r.operatedBy
        r.customerName = customerNameMap[r.customerId] || r.customerId
      })
    })
  }).catch(() => { loading.value = false })
}

function cancel() { open.value = false; reset() }
function reset() {
  form.value = {
    id: undefined, customerId: undefined, fromRepId: undefined,
    toRepId: undefined, operateType: undefined, reason: undefined, operatedBy: undefined, operatedByName: undefined
  }
  proxy.resetForm("formRef")
}

function loadCustomerOptions() {
  listCustomer({ pageNum: 1, pageSize: 1000 }).then(res => {
    customerOptions.value = res.data?.records || res.records || []
  })
}

function loadSalesRepOptions() {
  listSalesReps({ pageNum: 1, pageSize: 1000 }).then(res => {
    salesRepOptions.value = res.data?.records || res.records || []
  })
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
  loadCustomerOptions()
  loadSalesRepOptions()
  form.value.operatedBy = userStore.id
  form.value.operatedByName = userStore.nickName
  getMinUnusedTransferId().then(res => {
    form.value.id = res.data
    open.value = true
    title.value = "新增客户转移记录"
  })
}

function handleUpdate(row) {
  reset()
  Promise.all([loadCustomerOptions(), loadSalesRepOptions()]).then(() => {
    return getCustomerTransfer(row.id)
  }).then(response => {
    form.value = response.data || response
    // 设置操作人名称
    const rep = salesRepOptions.value.find(s => s.id === form.value.operatedBy)
    form.value.operatedByName = rep ? rep.realName : (form.value.operatedBy || userStore.realName)
    open.value = true
    title.value = "修改客户转移记录"
  })
}

function handleDetail(row) {
  loadCustomerOptions()
  loadSalesRepOptions()
  getCustomerTransfer(row.id).then(response => {
    form.value = response.data || response
    const rep = salesRepOptions.value.find(s => s.id === form.value.operatedBy)
    form.value.operatedByName = rep ? rep.realName : (form.value.operatedBy || userStore.realName)
    detailOpen.value = true
    detailTitle.value = "客户转移记录详情"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (!valid) return
    const isAdd = title.value.includes('新增')
    const fn = isAdd ? addCustomerTransfer : updateCustomerTransfer
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(isAdd ? "新增成功" : "修改成功")
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除该转移记录？').then(() => {
    // 先检查记录是否存在于数据库
    return getCustomerTransfer(row.id).then(res => {
      if (!res.data) {
        // 记录不存在，说明是前端残留的非法数据，刷新列表
        getList()
        throw new Error('该记录不存在于数据库，请刷新页面')
      }
      return updateCustomerTransfer({ id: row.id, deleted: 1 })
    })
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch((err) => {
    if (err.message !== '该记录不存在于数据库，请刷新页面') {
      proxy.$modal.msgError("删除失败")
    }
  })
}

getList()
</script>

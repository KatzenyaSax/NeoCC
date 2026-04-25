<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="销售代表" prop="salesRepId">
        <el-input v-model="queryParams.salesRepId" placeholder="请输入销售代表ID" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="日志日期" prop="logDate">
        <el-date-picker v-model="queryParams.logDate" type="date" placeholder="请选择日期" value-format="YYYY-MM-DD" clearable />
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
      <el-table-column label="销售代表" align="center" prop="salesRepId" width="110">
        <template #default="scope">
          {{ getSalesRepName(scope.row.salesRepId) }}
        </template>
      </el-table-column>
      <el-table-column label="日志日期" align="center" prop="logDate" width="120" />
      <el-table-column label="拨打电话数" align="center" prop="callsMade" width="110" />
      <el-table-column label="有效通话" align="center" prop="effectiveCalls" width="90" />
      <el-table-column label="新增意向" align="center" prop="newIntentions" width="90" />
      <el-table-column label="意向客户" align="center" prop="intentionClients" width="90" />
      <el-table-column label="面谈客户" align="center" prop="faceToFaceClients" width="90" />
      <el-table-column label="签约合同" align="center" prop="signedContracts" width="90" />
      <el-table-column label="日志内容" align="center" prop="content" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="680px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="销售代表" prop="salesRepId">
              <el-select
                v-model="form.salesRepId"
                placeholder="请选择销售代表"
                filterable
                :loading="salesRepLoading"
                @Focus="loadSalesRepOptions('')"
                :remote="true"
                :remote-method="loadSalesRepOptions"
                style="width:100%">
                <el-option
                  v-for="item in salesRepOptions"
                  :key="item.id"
                  :label="item.realName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="日志日期" prop="logDate">
              <el-date-picker v-model="form.logDate" type="date" placeholder="请选择日志日期" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="拨打电话数" prop="callsMade">
              <el-input-number v-model="form.callsMade" :min="0" placeholder="请输入" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="有效通话" prop="effectiveCalls">
              <el-input-number v-model="form.effectiveCalls" :min="0" placeholder="请输入" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="新增意向" prop="newIntentions">
              <el-input-number v-model="form.newIntentions" :min="0" placeholder="请输入" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="意向客户" prop="intentionClients">
              <el-input-number v-model="form.intentionClients" :min="0" placeholder="请输入" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="面谈客户" prop="faceToFaceClients">
              <el-input-number v-model="form.faceToFaceClients" :min="0" placeholder="请输入" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="签约合同" prop="signedContracts">
              <el-input-number v-model="form.signedContracts" :min="0" placeholder="请输入" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="日志内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入工作日志内容" />
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
import { listWorkLog, getWorkLog, addWorkLog, updateWorkLog, delWorkLog, checkDuplicate, listWorkLogBySalesRepIds, getMinUnusedIdWorkLog } from "@/api/sales/workLog"
import { listSalesReps } from "@/api/system/user"
import { listUserIdsByDeptId, listUserIdsByZoneId } from "@/api/system/user"
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const dataList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const open = ref(false)
const salesRepOptions = ref([])
const salesRepMap = ref({})
const salesRepLoading = ref(false)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    salesRepId: undefined,
    logDate: undefined,
    sortField: 'id',
    sortOrder: 'asc'
  },
  rules: {
    salesRepId: [{ required: true, message: "销售代表不能为空", trigger: "change" }],
    logDate: [{ required: true, message: "日志日期不能为空", trigger: "change" }],
    callsMade: [{ required: true, message: "拨打电话数不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  const roles = userStore.roles || []
  const userId = userStore.id
  const deptId = userStore.deptId
  const zoneId = userStore.zoneId
  const isSalesRep = roles.some(r => r === 'ROLE_SALES_REP')
  const isDeptManager = roles.some(r => r === 'ROLE_DEPT_MANAGER')
  const isZoneDirector = roles.some(r => r === 'ROLE_ZONE_DIRECTOR')
  const isAdmin = roles.some(r => ['ROLE_admin', 'ROLE_super'].includes(r))

  // 根据角色向查询参数追加过滤条件
  let params = { ...queryParams.value }
  if (isZoneDirector) {
    params.zoneId = zoneId
  } else if (isDeptManager) {
    params.deptId = deptId
  } else if (isSalesRep) {
    params.salesRepId = userId
  }

  // 直接调用分页接口，让后端处理过滤逻辑
  listWorkLog(params).then(response => {
    dataList.value = response.data?.records || response.records || []
    total.value = response.data?.total || response.total || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}

function cancel() { open.value = false; reset() }
function reset() {
  form.value = {
    id: undefined,
    salesRepId: undefined,
    logDate: undefined,
    callsMade: 0,
    effectiveCalls: 0,
    newIntentions: 0,
    intentionClients: 0,
    faceToFaceClients: 0,
    signedContracts: 0,
    content: undefined
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

function isSalesRepRole() {
  const roles = userStore.roles || []
  return roles.some(r => r === 'ROLE_SALES_REP' || 'ROLE_SUPER_ADMIN' || 'ROLE_ZONE_DIRECTOR' || 'ROLE_DEPT_MANAGER' || 'ROLE_GENERAL_MANAGER')
}

function isAdminRole() {
  const roles = userStore.roles || []
  return roles.some(r => ['ROLE_admin', 'ROLE_super', 'ROLE_SALES_REP'].includes(r))
}

function loadSalesRepOptions(searchValue) {
  salesRepLoading.value = true
  const roles = userStore.roles || []
  const userId = userStore.id
  const deptId = userStore.deptId
  const zoneId = userStore.zoneId
  const isSalesRep = roles.some(r => r === 'ROLE_SALES_REP')
  const isDeptManager = roles.some(r => r === 'ROLE_DEPT_MANAGER')
  const isZoneDirector = roles.some(r => r === 'ROLE_ZONE_DIRECTOR')

  // 使用 auth 模块的 API，不需要分页参数
  const params = {
    realName: searchValue || '',
    // 根据角色添加过滤条件
    ...(isZoneDirector ? { zoneId } : {}),
    ...(isDeptManager ? { deptId } : {}),
    ...(isSalesRep ? { salesRepId: userId } : {})
  }

  listSalesReps(params).then(response => {
    const records = response.data || response || []
    // 处理 records 可能是对象或者数组的情况
    const list = Array.isArray(records) ? records : records.records || []
    salesRepOptions.value = list.map(c => ({
      id: c.id,
      realName: c.realName
    }))
    // 构建 id 到 name 的映射
    salesRepOptions.value.forEach(c => {
      salesRepMap.value[c.id] = c.realName
    })
    salesRepLoading.value = false
  }).catch(() => {
    salesRepLoading.value = false
  })
}

function getSalesRepName(salesRepId) {
  if (!salesRepId) return ''
  return salesRepMap.value[salesRepId] || salesRepId
}

function handleAdd() {
  if (!isSalesRepRole()) {
    proxy.$modal.msgError("您不是销售代表！")
    return
  }
  reset()
  getMinUnusedIdWorkLog().then(res => {
    form.value.id = res.data
    open.value = true
    title.value = "新增工作日志"
  })
}

function handleUpdate(row) {
  reset()
  getWorkLog(row.id).then(response => {
    form.value = response.data || response
    open.value = true
    title.value = "修改工作日志"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (!valid) return

    // 如果是销售代表角色，新增时自动填充当前用户的ID，修改时保留选择的值
    if (isSalesRepRole()) {
      if (!form.value.id) { // 新增时
        form.value.salesRepId = userStore.id
      }
    } else {
      // 非销售代表角色不能提交
      proxy.$modal.msgError("您不是销售代表！")
      return
    }

    if (form.value.id) {
      // 修改
      updateWorkLog(form.value).then(() => {
        proxy.$modal.msgSuccess("修改成功")
        open.value = false
        getList()
      })
    } else {
      // 新增前检查重复
      checkDuplicate(form.value.salesRepId, form.value.logDate).then(res => {
        const isDuplicate = res.data !== undefined ? res.data : res
        if (isDuplicate) {
          proxy.$modal.msgError("该销售代表当天已存在工作日志，不能重复新增")
          return
        }
        addWorkLog(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      })
    }
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除该工作日志？').then(() => {
    return updateWorkLog({ id: row.id, deleted: 1 })
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

// 初始化时加载销售代表列表
loadSalesRepOptions('')

getList()
</script>

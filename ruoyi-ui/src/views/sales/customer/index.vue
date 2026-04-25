<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="客户名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入客户名称" clearable @keyup.enter="handleQuery" />
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

    <el-table v-loading="loading" :data="customerList" :row-key="row => row.id" @sort-change="handleSortChange">
      <el-table-column label="ID" align="center" prop="id" width="80" sortable />
      <el-table-column label="客户名称" align="center" prop="name" />
      <el-table-column label="联系电话" align="center" prop="phone" />
      <el-table-column label="对接销售" align="center">
        <template #default="scope">
          {{ scope.row.salesRepName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? '有效' : '无效' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createdAt" width="180" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 添加或修改客户对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="customerRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="客户名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="form.companyName" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="客户类型" prop="customerType">
          <el-select v-model="form.customerType" placeholder="请选择">
            <el-option label="个人" :value="1" />
            <el-option label="企业" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="意向等级" prop="intentionLevel">
          <el-select v-model="form.intentionLevel" placeholder="请选择">
            <el-option label="低" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="高" :value="3" />
            <el-option label="很有意向" :value="4" />
            <el-option label="已签约" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option label="有效" :value="1" />
            <el-option label="无效" :value="0" />
            <el-option label="公海" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="对接销售" prop="salesRepId">
          <el-select v-model="form.salesRepId" placeholder="请选择对接销售" clearable>
            <el-option
              v-for="item in salesRepOptions"
              :key="item.id"
              :label="item.realName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="来源" prop="source">
          <el-input v-model="form.source" placeholder="请输入来源" />
        </el-form-item>
        <el-form-item label="贷款意向金额" prop="loanIntentionAmount">
          <el-input-number v-model="form.loanIntentionAmount" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="贷款意向产品" prop="loanIntentionProduct">
          <el-input v-model="form.loanIntentionProduct" placeholder="请输入贷款意向产品" />
        </el-form-item>
        <el-form-item label="最后联系" prop="lastContactDate">
          <el-date-picker v-model="form.lastContactDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="下次跟进" prop="nextFollowUpDate">
          <el-date-picker v-model="form.nextFollowUpDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="公海时间" prop="publicSeaTime">
          <el-date-picker v-model="form.publicSeaTime" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="公海原因" prop="publicSeaReason">
          <el-input v-model="form.publicSeaReason" type="textarea" placeholder="请输入公海原因" />
        </el-form-item>
        <el-form-item label="批注" prop="annotation">
          <el-input v-model="form.annotation" type="textarea" placeholder="请输入批注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 客户详情对话框 -->
    <el-dialog title="客户详情" v-model="detailOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="客户名称">{{ detailForm.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailForm.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ detailForm.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="公司名称">{{ detailForm.companyName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="公司法人">{{ detailForm.companyLegalPerson || '-' }}</el-descriptions-item>
        <el-descriptions-item label="注册资本">{{ formatMoney(detailForm.companyRegCapital) }}</el-descriptions-item>
        <el-descriptions-item label="客户类型">{{ customerTypeText(detailForm.customerType) }}</el-descriptions-item>
        <el-descriptions-item label="意向等级">{{ intentionText(detailForm.intentionLevel) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(detailForm.status) }}</el-descriptions-item>
        <el-descriptions-item label="来源">{{ detailForm.source || '-' }}</el-descriptions-item>
        <el-descriptions-item label="贷款意向金额">{{ formatMoney(detailForm.loanIntentionAmount) }}</el-descriptions-item>
        <el-descriptions-item label="贷款意向产品">{{ detailForm.loanIntentionProduct || '-' }}</el-descriptions-item>
        <el-descriptions-item label="最后联系">{{ detailForm.lastContactDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="下次跟进">{{ detailForm.nextFollowUpDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="公海时间">{{ detailForm.publicSeaTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="公海原因">{{ detailForm.publicSeaReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="批注" :span="2">{{ detailForm.annotation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ detailForm.createdAt || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listCustomer, getCustomer, delCustomer, addCustomer, updateCustomer, getUserNamesByIds } from "@/api/sales/customer"
import { listSalesReps } from "@/api/system/user"
import { addCustomerTransfer } from "@/api/sales/customerTransfer"
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()

const userStore = useUserStore()
computed(() => {
  const roles = userStore.roles || []
  return roles.some(r => ['ROLE_sales_manager', 'ROLE_admin'].includes(r))
});
const customerList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const open = ref(false)
const detailOpen = ref(false)
const detailForm = ref({})
const salesRepOptions = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 20,
    name: undefined,
    sortField: 'id',
    sortOrder: 'asc'
  },
  rules: {
    name: [{ required: true, message: "客户名称不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询客户列表 */
function getList() {
  loading.value = true
  listCustomer(queryParams.value).then(response => {
    let records = response.data?.records || response.records || []
    // 过滤掉公海客户(status=5)
    records = records.filter(item => item.status !== 5)
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
      // 销售代表只看自己的客户
      records = records.filter(item => item.salesRepId === userId)
    } else if (isDeptManager && !isZoneDirector && !isAdmin) {
      // 部门经理看本部门的客户
      records = records.filter(item => item.deptId === deptId)
    } else if (isZoneDirector && !isAdmin) {
      // 战区总监看本战区的客户
      records = records.filter(item => item.zoneId === zoneId)
    }

    customerList.value = records
    total.value = response.data?.total || response.total || 0
    loading.value = false

    // 批量查询销售代表姓名
    const repIds = records
      .map(r => r.salesRepId)
      .filter(id => id != null)
      .filter((v, i, a) => a.indexOf(v) === i)
    if (repIds.length > 0) {
      getUserNamesByIds(repIds).then(res => {
        const nameMap = res.data || {}
        customerList.value.forEach(c => {
          c.salesRepName = nameMap[c.salesRepId] || null
        })
      })
    }
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
    name: undefined,
    phone: undefined,
    idCard: undefined,
    companyName: undefined,
    companyLegalPerson: undefined,
    companyRegCapital: undefined,
    customerType: undefined,
    intentionLevel: undefined,
    status: 1,
    salesRepId: undefined,
    source: undefined,
    loanIntentionAmount: undefined,
    loanIntentionProduct: undefined,
    lastContactDate: undefined,
    nextFollowUpDate: undefined,
    publicSeaTime: undefined,
    publicSeaReason: undefined,
    annotation: undefined
  }
  proxy.resetForm("customerRef")
}

function customerTypeText(val) {
  return { 1: '个人', 2: '企业' }[val] || '-'
}

function intentionText(val) {
  return { 1: '低', 2: '中', 3: '高', 4: '很有意向', 5: '已签约' }[val] || '-'
}

function statusText(val) {
  return { 0: '无效', 1: '有效', 5: '公海' }[val] || '-'
}

function formatMoney(val) {
  if (val == null || val === '') return '-'
  const num = parseFloat(val)
  if (isNaN(num)) return '-'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

/** 加载销售代表下拉选项 */
function loadSalesRepOptions() {
  const roles = userStore.roles || []
  const userId = userStore.id
  const deptId = userStore.deptId
  const zoneId = userStore.zoneId
  const isSalesRepRole = roles.some(r => r === 'ROLE_SALES_REP' || r === 'sales_rep')
  const isDeptManager = roles.some(r => r === 'ROLE_DEPT_MANAGER' || r === 'sales_manager')
  const isZoneDirector = roles.some(r => r === 'ROLE_ZONE_DIRECTOR')

  let params = {}
  if (isSalesRepRole) {
    params = { salesRepId: userId }
  } else if (isDeptManager) {
    params = { deptId: deptId }
  } else if (isZoneDirector) {
    params = { zoneId: zoneId }
  }

  listSalesReps(params).then(res => {
    salesRepOptions.value = res.data || []
  })
}

/** 详情按钮 */
function handleDetail(row) {
  getCustomer(row.id).then(res => {
    detailForm.value = res.data || res
    detailOpen.value = true
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

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = "添加客户"
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const id = row.id
  getCustomer(id).then(response => {
    form.value = response.data || response
    open.value = true
    title.value = "修改客户"
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["customerRef"].validate(valid => {
    if (valid) {
      if (form.value.id != undefined) {
        // 修改前先获取原始客户信息，检查销售代表是否变化
        getCustomer(form.value.id).then(originalRes => {
          const originalCustomer = originalRes.data || originalRes
          // 调用更新接口
          updateCustomer(form.value).then(response => {
            // 如果销售代表发生了变化，创建转移日志
            if (originalCustomer.salesRepId !== form.value.salesRepId) {
              // 创建转移日志
              addCustomerTransfer({
                customerId: form.value.id,
                fromRepId: originalCustomer.salesRepId,
                toRepId: form.value.salesRepId,
                operateType: "ADJUST",
                reason: "客户管理修改",
                operatedBy: userStore.id
              }).then(() => {
                // 转移日志创建成功
              }).catch(() => {
                // 忽略转移日志创建失败的情况
              })
            }
            proxy.$modal.msgSuccess("修改成功")
            open.value = false
            getList()
          })
        })
      } else {
        addCustomer(form.value).then(response => {
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
  proxy.$modal.confirm('是否确认删除客户"' + row.name + '"？').then(function () {
    return delCustomer(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
loadSalesRepOptions()
</script>

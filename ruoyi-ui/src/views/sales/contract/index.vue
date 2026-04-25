<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="合同编号" prop="contractNo">
        <el-input v-model="queryParams.contractNo" placeholder="请输入合同编号" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮区域 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="DocumentSigned" @click="handleSign">签署合同</el-button>
      </el-col>
    </el-row>

    <!-- 合同表格 -->
    <el-table v-loading="loading" :data="contractList" :row-key="row => row.id" @sort-change="handleSortChange">
      <el-table-column label="ID" align="center" prop="id" width="80" sortable />
      <el-table-column label="合同编号" align="center" prop="contractNo" />
      <el-table-column label="客户" align="center" prop="customerId">
        <template #default="scope">{{ customerNameMap[scope.row.customerId] || scope.row.customerId }}</template>
      </el-table-column>
      <el-table-column label="合同金额" align="center" prop="contractAmount" />
      <el-table-column label="实际贷款金额" align="center" prop="actualLoanAmount" />
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
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-if="isSuperAdmin">修改</el-button>
          <el-button link type="success" icon="Check" @click="handleSignRow(scope.row)" v-if="scope.row.status === 1 && (isSuperAdmin || isZoneDirector || isSalesRep)">正式签署</el-button>
          <el-button link type="warning" icon="Money" @click="handlePayFirstInstallment(scope.row)" v-if="scope.row.status === 2 && isSalesRep">已支付首期</el-button>
          <el-button link type="warning" icon="Promotion" @click="handleSubmitToFinance(scope.row)" v-if="scope.row.status === 3 && (isSuperAdmin || isZoneDirector)">提交金融部</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 签署/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="800px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <!-- 销售信息 -->
        <el-divider content-position="left">销售信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="销售代表" prop="salesRepId">
              <el-select
                v-model="form.salesRepName"
                placeholder="请搜索销售代表"
                filterable
                remote
                :remote-method="loadSalesRepOptions"
                :loading="salesRepLoading"
                style="width: 100%">
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
            <el-form-item label="部门" prop="deptId">
              <el-select
                v-model="form.deptName"
                placeholder="请搜索部门"
                filterable
                remote
                :remote-method="loadDeptOptions"
                :loading="deptLoading"
                style="width: 100%">
                <el-option
                  v-for="item in deptOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="战区" prop="zoneId">
              <el-select
                v-model="form.zoneName"
                placeholder="请搜索战区"
                filterable
                remote
                :remote-method="loadZoneOptions"
                :loading="zoneLoading"
                style="width: 100%">
                <el-option
                  v-for="item in zoneOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品" prop="productId">
              <el-select
                v-model="form.productName"
                placeholder="请搜索产品"
                filterable
                remote
                :remote-method="loadProductOptions"
                :loading="productLoading"
                style="width: 100%">
                <el-option
                  v-for="item in productOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 客户信息 -->
        <el-divider content-position="left">客户信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户" prop="customerId">
              <el-select
                v-model="form.customerId"
                placeholder="请搜索客户"
                filterable
                remote
                :remote-method="loadCustomerOptions"
                :loading="customerLoading"
                style="width: 100%">
                <el-option
                  v-for="item in customerOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 合同细则 -->
        <el-divider content-position="left">合同细则</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="合同编号" prop="contractNo">
              <el-input v-model="form.contractNo" placeholder="自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="待签署" :value="1" />
                <el-option label="已签署" :value="2" />
                <el-option label="已付首期" :value="3" />
                <el-option label="审核中" :value="4" />
                <el-option label="已通过" :value="5" />
                <el-option label="已拒绝" :value="6" />
                <el-option label="已放款" :value="7" />
                <el-option label="完成" :value="8" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同金额" prop="contractAmount">
              <el-input-number v-model="form.contractAmount" :min="0" precision="2" placeholder="请输入合同金额" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际贷款金额" prop="actualLoanAmount">
              <el-input-number v-model="form.actualLoanAmount" :min="0" precision="2" placeholder="请输入实际贷款金额" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务费率" prop="serviceFeeRate">
              <el-input-number v-model="form.serviceFeeRate" :min="0" precision="4" placeholder="请输入服务费率" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务费1" prop="serviceFee1">
              <el-input-number v-model="form.serviceFee1" :min="0" precision="2" placeholder="请输入服务费1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务费2" prop="serviceFee2">
              <el-input-number v-model="form.serviceFee2" :min="0" precision="2" placeholder="请输入服务费2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务费1已付" prop="serviceFee1Paid">
              <el-select v-model="form.serviceFee1Paid" placeholder="请选择" style="width: 100%">
                <el-option label="否" :value="0" />
                <el-option label="是" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务费2已付" prop="serviceFee2Paid">
              <el-select v-model="form.serviceFee2Paid" placeholder="请选择" style="width: 100%">
                <el-option label="否" :value="0" />
                <el-option label="是" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="签署日期" prop="signDate">
              <el-date-picker v-model="form.signDate" type="date" placeholder="选择签署日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纸质合同编号" prop="paperContractNo">
              <el-input v-model="form.paperContractNo" placeholder="请输入纸质合同编号" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="贷款用途" prop="loanUse">
              <el-input v-model="form.loanUse" type="textarea" placeholder="请输入贷款用途" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="担保信息" prop="guaranteeInfo">
              <el-input v-model="form.guaranteeInfo" type="textarea" placeholder="请输入担保信息" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情对话框 -->
    <el-dialog title="合同详情" v-model="detailVisible" width="800px" append-to-body>
      <el-form ref="detailFormRef" :model="detailForm" label-width="120px">
        <el-divider content-position="left">销售信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="销售代表">{{ detailForm.salesRepName || detailForm.salesRepId }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门">{{ detailForm.deptName}}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="战区">{{ detailForm.zoneName }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品">{{ detailForm.productName }}</el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">客户信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户">{{ detailForm.customerName || detailForm.customerId }}</el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">合同细则</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="合同编号">{{ detailForm.contractNo }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="detailForm.status" placeholder="请选择状态" style="width: 100%">
                <el-option label="待签署" :value="0" />
                <el-option label="草稿" :value="1" />
                <el-option label="已签署" :value="2" />
                <el-option label="已付首期" :value="3" />
                <el-option label="审核中" :value="4" />
                <el-option label="已通过" :value="5" />
                <el-option label="已拒绝" :value="6" />
                <el-option label="已放款" :value="7" />
                <el-option label="完成" :value="8" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同金额">{{ detailForm.contractAmount }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际贷款金额">{{ detailForm.actualLoanAmount }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务费率">{{ detailForm.serviceFeeRate }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务费1">{{ detailForm.serviceFee1 }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务费2">{{ detailForm.serviceFee2 }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务费1已付">
              <el-select v-model="detailForm.serviceFee1Paid" style="width: 100%">
                <el-option label="否" :value="0" />
                <el-option label="是" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="服务费2已付">
              <el-select v-model="detailForm.serviceFee2Paid" style="width: 100%">
                <el-option label="否" :value="0" />
                <el-option label="是" :value="1" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="签署日期">{{ detailForm.signDate }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纸质合同编号">
              <el-input v-model="detailForm.paperContractNo" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="贷款用途">
              <el-input v-model="detailForm.loanUse" type="textarea" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="担保信息">
              <el-input v-model="detailForm.guaranteeInfo" type="textarea" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="拒绝原因">
              <el-input v-model="detailForm.rejectReason" type="textarea" placeholder="请输入" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="detailForm.remark" type="textarea" placeholder="请输入" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitDetailForm">确认修改</el-button>
          <el-button @click="detailVisible = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listContract, getContract, delContract, addContract, updateContract, signContract, generateNo, getContractDetail, getContractDetailWithNames, payFirstInstallment, submitToFinance } from "@/api/sales/contract"
import { listSalesReps } from "@/api/sales/publicSea"
import { listCustomer } from "@/api/sales/customer"
import { listAllDepartment } from "@/api/system/department"
import { listAllZone } from "@/api/system/zone"
import { listFinanceProduct } from "@/api/finance/financeProduct"
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()

const contractList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dialogTitle = ref("")
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)

// 下拉搜索加载状态
const salesRepLoading = ref(false)
const deptLoading = ref(false)
const zoneLoading = ref(false)
const productLoading = ref(false)
const customerLoading = ref(false)

// 下拉选项数据
const salesRepOptions = ref([])
const deptOptions = ref([])
const zoneOptions = ref([])
const productOptions = ref([])
const customerOptions = ref([])

// 名称映射Map（用于详情和表格显示）
const customerNameMap = ref({})
const salesRepNameMap = ref({})
const deptNameMap = ref({})
const zoneNameMap = ref({})
const productNameMap = ref({})

// 用户角色
const isSuperAdmin = ref(false)
const isDeptManager = ref(false)
const isZoneDirector = ref(false)
const isSalesRep = ref(false)

// 初始化角色
function initRoles() {
  const roles = userStore.roles || []
  isSuperAdmin.value = roles.some(r => ['ROLE_SUPER_ADMIN', 'SUPER_ADMIN'].includes(r))
  isDeptManager.value = roles.some(r => ['ROLE_DEPT_MANAGER', 'DEPT_MANAGER'].includes(r))
  isZoneDirector.value = roles.some(r => ['ROLE_ZONE_DIRECTOR', 'ZONE_DIRECTOR'].includes(r))
  isSalesRep.value = roles.some(r => ['ROLE_SALES_REP', 'SALES_REP'].includes(r))
}

initRoles()

const statusOptions = {
  1: '待签署',
  2: '已签署',
  3: '已付首期',
  4: '审核中',
  5: '已通过',
  6: '已拒绝',
  7: '已放款',
  8: '完成'
}

function getStatusLabel(status) {
  return statusOptions[status] || '未知'
}

function getStatusType(status) {
  const types = { 0: 'info', 1: 'success', 2: 'warning', 3: 'success', 4: 'danger', 5: 'success', 6: 'danger', 7: 'primary', 8: 'info' }
  return types[status] || 'info'
}

const detailForm = ref({})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 20,
    contractNo: undefined,
    sortField: 'id',
    sortOrder: 'asc'
  },
  rules: {
    customerId: [{ required: true, message: "客户不能为空", trigger: "change" }],
    salesRepId: [{ required: true, message: "销售代表不能为空", trigger: "change" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询合同列表 */
function getList() {
  loading.value = true

  // 获取当前用户角色（后端返回的roles带"ROLE_"前缀）
  const roles = userStore.roles || []

  // 根据角色传filterRole参数和对应的ID
  let params = { ...queryParams.value }

  if (roles.includes('ROLE_SUPER_ADMIN')) {
    // 超级管理员：不过滤
  } else if (roles.includes('ROLE_ZONE_DIRECTOR')) {
    params.filterRole = 'ROLE_ZONE_DIRECTOR'
    params.zoneId = userStore.zoneId
  } else if (roles.includes('ROLE_DEPT_MANAGER')) {
    params.filterRole = 'ROLE_DEPT_MANAGER'
    params.deptId = userStore.deptId
  } else if (roles.includes('ROLE_SALES_REP')) {
    params.filterRole = 'ROLE_SALES_REP'
    params.userId = userStore.id
  }

  listContract(params).then(response => {
    contractList.value = response.data?.records || response.records || []
    total.value = response.data?.total || response.total || 0
    loading.value = false
  })
}

/** 取消按钮 */
function cancel() {
  dialogVisible.value = false
  reset()
}

/** 表单重置 */
function reset() {
  form.value = {
    id: undefined,
    contractNo: undefined,
    customerId: undefined,
    salesRepId: undefined,
    deptId: undefined,
    productId: undefined,
    zoneId: undefined,
    contractAmount: undefined,
    actualLoanAmount: undefined,
    serviceFeeRate: undefined,
    serviceFee1: undefined,
    serviceFee2: undefined,
    serviceFee1Paid: 0,
    serviceFee2Paid: 0,
    signDate: undefined,
    paperContractNo: undefined,
    loanUse: undefined,
    guaranteeInfo: undefined,
    remark: undefined,
    status: 0,
    salesRepName: '',
    deptName: '',
    zoneName: '',
    productName: ''
  }
  proxy.resetForm("formRef")
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

/** 签署合同按钮 - 新增签署 */
function handleSign() {
  const roles = userStore.roles || []
  const isSuper = roles.includes('ROLE_SUPER_ADMIN')
  const isSalesRep = roles.includes('SALES_REP')
  if (!isSuper && !isSalesRep) {
    proxy.$modal.msgError("您不是销售代表！")
    return
  }
  reset()
  isEdit.value = false
  dialogTitle.value = "签署合同"
  // 预加载客户下拉选项
  loadCustomerOptions('')
  generateNo().then(response => {
    form.value.contractNo = response.data || response
    dialogVisible.value = true
  })
}

/** 签署行内合同 */
function handleSignRow(row) {
  proxy.$modal.confirm('是否确认签署合同"' + row.contractNo + '"？').then(function () {
    return signContract(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("签署成功")
  }).catch(() => {})
}

/** 详情按钮 */
function handleDetail(row) {
  getContractDetailWithNames(row.id).then(response => {
    detailForm.value = response.data || response
    detailVisible.value = true
  })
}

/** 提交详情修改 */
function submitDetailForm() {
  updateContract(detailForm.value).then(response => {
    proxy.$modal.msgSuccess("修改成功")
    detailVisible.value = false
    getList()
  })
}

/** 编辑按钮 */
function handleEdit(row) {
  isEdit.value = true
  dialogTitle.value = "编辑合同"
  // 预加载客户下拉选项
  loadCustomerOptions('')
  getContractDetailWithNames(row.id).then(response => {
    form.value = response.data || response
    dialogVisible.value = true
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      const roles = userStore.roles || []
      const isSalesRep = roles.includes('ROLE_sales_rep')
      // 新增合同（签署）时，仅销售代表可提交，超级管理员不可提交
      if (form.value.id == undefined && !isSalesRep) {
        proxy.$modal.msgError("您不是销售代表！")
        return
      }
      if (form.value.id != undefined) {
        updateContract(form.value).then(response => {
          proxy.$modal.msgSuccess("修改成功")
          dialogVisible.value = false
          getList()
        })
      } else {
        addContract(form.value).then(response => {
          proxy.$modal.msgSuccess("签署成功")
          dialogVisible.value = false
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

/** 已支付首期按钮 */
function handlePayFirstInstallment(row) {
  proxy.$modal.confirm('是否确认合同"' + row.contractNo + '"已支付首期？').then(function () {
    return payFirstInstallment(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("操作成功")
  }).catch(() => {})
}

/** 提交金融部按钮 */
function handleSubmitToFinance(row) {
  proxy.$modal.confirm('是否确认将合同"' + row.contractNo + '"提交至金融部？').then(function () {
    return submitToFinance(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("操作成功")
  }).catch(() => {})
}

/** 加载名称映射（用于表格和详情显示） */
function loadNameMaps() {
  // 获取当前用户角色（后端返回的roles带"ROLE_"前缀）
  const roles = userStore.roles || []

  // 根据角色传filterRole参数和对应的ID
  let params = { pageNum: 1, pageSize: 1000 }

  if (roles.includes('ROLE_SUPER_ADMIN')) {
    // 超级管理员：不过滤
  } else if (roles.includes('ROLE_ZONE_DIRECTOR')) {
    params.filterRole = 'ROLE_ZONE_DIRECTOR'
    params.zoneId = userStore.zoneId
  } else if (roles.includes('ROLE_DEPT_MANAGER')) {
    params.filterRole = 'ROLE_DEPT_MANAGER'
    params.deptId = userStore.deptId
  } else if (roles.includes('ROLE_SALES_REP')) {
    params.filterRole = 'ROLE_SALES_REP'
    params.userId = userStore.id
  }

  // 加载客户列表
  listCustomer(params).then(response => {
    const records = response.data?.records || response.records || []
    const map = {}
    records.forEach(c => { map[c.id] = c.name || c.realName })
    customerNameMap.value = map
  })
  // 加载销售代表列表
  listSalesReps().then(response => {
    const reps = response.data || []
    const map = {}
    reps.forEach(r => { map[r.id] = r.realName })
    salesRepNameMap.value = map
  })
  // 加载部门列表
  listAllDepartment().then(response => {
    const depts = response.data || []
    const map = {}
    depts.forEach(d => { map[d.id] = d.name })
    deptNameMap.value = map
  })
  // 加载战区列表
  listAllZone().then(response => {
    const zones = response.data || []
    const map = {}
    zones.forEach(z => { map[z.id] = z.name })
    zoneNameMap.value = map
  })
  // 加载产品列表
  listFinanceProduct({ pageNum: 1, pageSize: 1000 }).then(response => {
    const records = response.data?.records || response.records || []
    const map = {}
    records.forEach(p => { map[p.id] = p.productName })
    productNameMap.value = map
  })
}

/** 加载销售代表下拉选项 */
function loadSalesRepOptions(searchValue) {
  salesRepLoading.value = true
  listSalesReps().then(response => {
    const reps = response.data || []
    salesRepOptions.value = reps
      .filter(r => !searchValue || r.realName.includes(searchValue))
      .map(r => ({ id: r.id, realName: r.realName }))
    salesRepLoading.value = false
  }).catch(() => {
    salesRepLoading.value = false
  })
}

/** 加载部门下拉选项 */
function loadDeptOptions(searchValue) {
  deptLoading.value = true
  listAllDepartment().then(response => {
    const depts = response.data || []
    deptOptions.value = depts
      .filter(d => !searchValue || d.deptName.includes(searchValue))
      .map(d => ({ id: d.id, name: d.deptName }))
    deptLoading.value = false
  }).catch(() => {
    deptLoading.value = false
  })
}

/** 加载战区下拉选项 */
function loadZoneOptions(searchValue) {
  zoneLoading.value = true
  listAllZone().then(response => {
    const zones = response.data || []
    zoneOptions.value = zones
      .filter(z => !searchValue || z.zoneName.includes(searchValue))
      .map(z => ({ id: z.id, name: z.zoneName }))
    zoneLoading.value = false
  }).catch(() => {
    zoneLoading.value = false
  })
}

/** 加载产品下拉选项 */
function loadProductOptions(searchValue) {
  productLoading.value = true
  listFinanceProduct({ pageNum: 1, pageSize: 1000 }).then(response => {
    const records = response.data?.records || response.records || []
    productOptions.value = records
      .filter(p => !searchValue || p.productName.includes(searchValue))
      .map(p => ({ id: p.id, name: p.productName }))
    productLoading.value = false
  }).catch(() => {
    productLoading.value = false
  })
}

/** 加载客户下拉选项 */
function loadCustomerOptions(searchValue) {
  customerLoading.value = true
  // 获取当前用户角色（后端返回的roles带"ROLE_"前缀）
  const roles = userStore.roles || []

  // 根据角色传filterRole参数和对应的ID
  let params = {
    pageNum: 1,
    pageSize: 500,
    name: searchValue || ''
  }

  if (roles.includes('ROLE_SUPER_ADMIN')) {
    // 超级管理员：不过滤
  } else if (roles.includes('ROLE_ZONE_DIRECTOR')) {
    params.filterRole = 'ROLE_ZONE_DIRECTOR'
    params.zoneId = userStore.zoneId
  } else if (roles.includes('ROLE_DEPT_MANAGER')) {
    params.filterRole = 'ROLE_DEPT_MANAGER'
    params.deptId = userStore.deptId
  } else if (roles.includes('ROLE_SALES_REP')) {
    params.filterRole = 'ROLE_SALES_REP'
    params.userId = userStore.id
  }

  listCustomer(params).then(response => {
    const records = response.data?.records || response.records || []
    // 过滤掉公海客户(status=5)，后端已完成角色权限过滤
    customerOptions.value = records
      .filter(c => c.status !== 5)
      .map(c => ({ id: c.id, name: c.name || c.realName }))
    customerLoading.value = false
  }).catch(() => {
    customerLoading.value = false
  })
}

loadNameMaps()
getList()
</script>
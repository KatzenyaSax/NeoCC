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
    <el-table v-loading="loading" :data="contractList">
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="合同编号" align="center" prop="contractNo" />
      <el-table-column label="客户ID" align="center" prop="customerId" />
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
          <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)" v-if="scope.row.status === 0 || scope.row.status === 1">编辑</el-button>
          <el-button link type="success" icon="Check" @click="handleSignRow(scope.row)" v-if="scope.row.status === 0">签署</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-if="scope.row.status === 0 || scope.row.status === 1">删除</el-button>
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
              <el-input v-model="form.salesRepId" placeholder="请输入销售代表ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门" prop="deptId">
              <el-input v-model="form.deptId" placeholder="请输入部门ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="战区" prop="zoneId">
              <el-input v-model="form.zoneId" placeholder="请输入战区ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品" prop="productId">
              <el-input v-model="form.productId" placeholder="请输入产品ID" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 客户信息 -->
        <el-divider content-position="left">客户信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户" prop="customerId">
              <el-input v-model="form.customerId" placeholder="请输入客户ID" />
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

    <!-- 详情对话框（可编辑） -->
    <el-dialog title="合同详情" v-model="detailVisible" width="800px" append-to-body>
      <el-form ref="detailFormRef" :model="detailForm" label-width="120px">
        <el-divider content-position="left">销售信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="销售代表">{{ detailForm.salesRepId }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门">{{ detailForm.deptId }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="战区">{{ detailForm.zoneId }}</el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="产品">{{ detailForm.productId }}</el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">客户信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户">{{ detailForm.customerId }}</el-form-item>
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
import { listContract, getContract, delContract, addContract, updateContract, signContract, generateNo, getContractDetail } from "@/api/sales/contract"
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

const statusOptions = {
  0: '待签署',
  1: '草稿',
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
    pageSize: 10,
    contractNo: undefined
  },
  rules: {
    customerId: [{ required: true, message: "客户不能为空", trigger: "blur" }],
    salesRepId: [{ required: true, message: "销售代表不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询合同列表 */
function getList() {
  loading.value = true
  listContract(queryParams.value).then(response => {
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
      // 销售代表只看自己的合同
      records = records.filter(item => item.salesRepId === userId)
    } else if (isDeptManager && !isZoneDirector && !isAdmin) {
      // 部门经理看本部门的合同
      records = records.filter(item => item.deptId === deptId)
    } else if (isZoneDirector && !isAdmin) {
      // 战区总监看本战区的合同
      records = records.filter(item => item.zoneId === zoneId)
    }

    contractList.value = records
    total.value = records.length
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
    status: 0
  }
  proxy.resetForm("formRef")
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")
  handleQuery()
}

/** 签署合同按钮 - 新增签署 */
function handleSign() {
  reset()
  isEdit.value = false
  dialogTitle.value = "签署合同"
  // 自动生成合同编号
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
  getContractDetail(row.id).then(response => {
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
  getContract(row.id).then(response => {
    form.value = response.data || response
    dialogVisible.value = true
  })
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
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

getList()
</script>

<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="客户名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入客户名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px">
          <el-option label="有效" :value="1" />
          <el-option label="无效" :value="0" />
          <el-option label="公海" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item label="客户类型" prop="customerType">
        <el-select v-model="queryParams.customerType" placeholder="客户类型" clearable style="width: 100px">
          <el-option label="个人" :value="1" />
          <el-option label="企业" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="意向等级" prop="intentionLevel">
        <el-select v-model="queryParams.intentionLevel" placeholder="意向等级" clearable style="width: 110px">
          <el-option label="低" :value="1" />
          <el-option label="中" :value="2" />
          <el-option label="高" :value="3" />
          <el-option label="很有意向" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增客户</el-button>
      </el-col>
    </el-row>

    <!-- 客户列表 -->
    <el-table v-loading="loading" :data="customerList">
      <el-table-column label="客户名称" align="center" prop="name" min-width="120" />
      <el-table-column label="联系电话" align="center" prop="phone" width="130" />
      <el-table-column label="客户类型" align="center" prop="customerType" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.customerType === 1 ? 'primary' : 'warning'" size="small">
            {{ customerTypeText(scope.row.customerType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="意向等级" align="center" prop="intentionLevel" width="100">
        <template #default="scope">
          <el-tag :type="getIntentionTagType(scope.row.intentionLevel)" size="small">
            {{ intentionText(scope.row.intentionLevel) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : scope.row.status === 5 ? 'warning' : 'info'" size="small">
            {{ statusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="贷款意向金额" align="center" prop="loanIntentionAmount" width="130">
        <template #default="scope">
          {{ formatMoney(scope.row.loanIntentionAmount) }}
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createdAt" width="160" />
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 重复客户提示对话框 -->
    <el-dialog title="发现重复客户" v-model="duplicateOpen" width="600px" append-to-body>
      <el-alert type="warning" :closable="false" style="margin-bottom: 15px">
        <template #title>
          已存在 <strong>{{ duplicateList.length }}</strong> 个相似客户，是否继续添加？
        </template>
      </el-alert>
      <el-table :data="duplicateList" size="small" border>
        <el-table-column prop="name" label="客户名称" />
        <el-table-column prop="phone" label="联系电话" />
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            {{ statusText(scope.row.status) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="scope">
            <el-button type="primary" link @click="viewDuplicate(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="duplicateOpen = false">放弃新增</el-button>
        <el-button type="primary" @click="confirmAddAnyway">继续添加</el-button>
      </template>
    </el-dialog>

    <!-- 客户详情对话框 -->
    <el-dialog title="客户详情" v-model="detailOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="客户名称">{{ detailForm.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailForm.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ detailForm.idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="公司名称">{{ detailForm.companyName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户类型">{{ customerTypeText(detailForm.customerType) }}</el-descriptions-item>
        <el-descriptions-item label="意向等级">{{ intentionText(detailForm.intentionLevel) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailForm.status === 1 ? 'success' : detailForm.status === 5 ? 'warning' : 'info'" size="small">
            {{ statusText(detailForm.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="来源">{{ detailForm.source || '-' }}</el-descriptions-item>
        <el-descriptions-item label="贷款意向金额">{{ formatMoney(detailForm.loanIntentionAmount) }}</el-descriptions-item>
        <el-descriptions-item label="贷款意向产品">{{ detailForm.loanIntentionProduct || '-' }}</el-descriptions-item>
        <el-descriptions-item label="最后联系">{{ detailForm.lastContactDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="下次跟进">{{ detailForm.nextFollowUpDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="公海时间">{{ detailForm.publicSeaTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="公海原因" :span="2">{{ detailForm.publicSeaReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="批注" :span="2">{{ detailForm.annotation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ detailForm.createdAt || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑客户对话框 -->
    <el-dialog :title="title" v-model="open" width="750px" append-to-body>
      <el-tabs v-model="activeTab">
        <!-- 基本信息 Tab -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form ref="customerRef" :model="form" :rules="rules" label-width="100px" style="max-height: 400px; overflow-y: auto">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="客户名称" prop="name">
                  <el-input v-model="form.name" placeholder="请输入客户名称" maxlength="50" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="客户类型" prop="customerType">
                  <el-select v-model="form.customerType" placeholder="请选择" style="width: 100%">
                    <el-option label="个人" :value="1" />
                    <el-option label="企业" :value="2" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="联系电话" prop="phone">
                  <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="意向等级" prop="intentionLevel">
                  <el-select v-model="form.intentionLevel" placeholder="请选择" style="width: 100%">
                    <el-option label="低" :value="1" />
                    <el-option label="中" :value="2" />
                    <el-option label="高" :value="3" />
                    <el-option label="很有意向" :value="4" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="身份证号" prop="idCard">
                  <el-input v-model="form.idCard" placeholder="请输入身份证号" maxlength="18" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="状态" prop="status">
                  <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                    <el-option label="有效" :value="1" />
                    <el-option label="无效" :value="0" />
                    <el-option label="公海" :value="5" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="公司名称" prop="companyName">
                  <el-input v-model="form.companyName" placeholder="请输入公司名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="来源" prop="source">
                  <el-input v-model="form.source" placeholder="请输入来源" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="部门ID" prop="deptId">
                  <el-input-number v-model="form.deptId" :min="1" placeholder="部门ID" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="战区ID" prop="zoneId">
                  <el-input-number v-model="form.zoneId" :min="1" placeholder="战区ID" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="批注" prop="annotation">
              <el-input v-model="form.annotation" type="textarea" :rows="2" placeholder="请输入批注" maxlength="500" show-word-limit />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 贷款意向 Tab -->
        <el-tab-pane label="贷款意向" name="loan">
          <el-form :model="form" :rules="loanRules" label-width="120px" style="max-height: 400px; overflow-y: auto">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="贷款意向金额" prop="loanIntentionAmount">
                  <el-input-number v-model="form.loanIntentionAmount" :min="0" :precision="2" :controls="false" placeholder="请输入金额" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="贷款意向产品" prop="loanIntentionProduct">
                  <el-input v-model="form.loanIntentionProduct" placeholder="请输入意向产品" />
                </el-form-item>
              </el-col>
            </el-row>

            <el-divider content-position="left">跟进计划</el-divider>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="最后联系时间" prop="lastContactDate">
                  <el-date-picker v-model="form.lastContactDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="下次跟进时间" prop="nextFollowUpDate">
                  <el-date-picker v-model="form.nextFollowUpDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- 公海信息 Tab -->
        <el-tab-pane label="公海信息" name="publicSea" v-if="form.status === 5">
          <el-alert type="info" :closable="false" style="margin-bottom: 15px">
            客户状态设为"公海"后，需填写以下信息
          </el-alert>
          <el-form :model="form" label-width="100px" style="max-height: 400px; overflow-y: auto">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="公海时间" prop="publicSeaTime">
                  <el-date-picker v-model="form.publicSeaTime" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="公海原因" prop="publicSeaReason">
              <el-input v-model="form.publicSeaReason" type="textarea" :rows="3" placeholder="请输入公海原因" maxlength="500" show-word-limit />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="cancel">取 消</el-button>
          <el-button type="primary" @click="submitForm">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listCustomer, getCustomer, delCustomer, addCustomer, updateCustomer } from "@/api/sales/customer"
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()

// ============ 列表相关 ============
const customerList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const activeTab = ref('basic')

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    name: undefined,
    status: undefined,
    customerType: undefined,
    intentionLevel: undefined
  }
})
const { queryParams } = toRefs(data)

// ============ 详情相关 ============
const detailOpen = ref(false)
const detailForm = ref({})

// ============ 表单相关 ============
const open = ref(false)
const title = ref("")
const customerRef = ref(null)
const form = ref({})
const duplicateOpen = ref(false)
const duplicateList = ref([])
const allowDuplicate = ref(false) // 是否允许跳过重复检查

const rules = {
  name: [
    { required: true, message: "客户名称不能为空", trigger: "blur" },
    { min: 2, max: 50, message: "长度在 2 到 50 个字符", trigger: "blur" }
  ],
  phone: [
    { required: true, message: "联系电话不能为空", trigger: "blur" },
    { pattern: /^1[3-9]\d{9}$/, message: "请输入正确的手机号", trigger: "blur" }
  ],
  idCard: [
    { required: false, trigger: "blur" },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: "请输入正确的身份证号", trigger: "blur" }
  ],
  customerType: [{ required: true, message: "请选择客户类型", trigger: "change" }],
  intentionLevel: [{ required: true, message: "请选择意向等级", trigger: "change" }],
  status: [{ required: true, message: "请选择状态", trigger: "change" }]
}

const loanRules = {
  loanIntentionAmount: [
    { required: false, trigger: "blur" },
    { type: 'number', min: 0, message: "金额不能为负数", trigger: "blur" }
  ]
}

// ============ 方法 ============

/** 查询客户列表 */
function getList() {
  loading.value = true
  listCustomer(queryParams.value).then(response => {
    customerList.value = response.data?.records || response.records || []
    total.value = response.data?.total || response.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
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

/** 重置表单 */
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
    status: 1,  // 默认有效
    source: undefined,
    deptId: 1,  // 默认部门ID
    zoneId: 1,  // 默认战区ID
    loanIntentionAmount: undefined,
    loanIntentionProduct: undefined,
    lastContactDate: undefined,
    nextFollowUpDate: undefined,
    publicSeaTime: undefined,
    publicSeaReason: undefined,
    annotation: undefined
  }
  activeTab.value = 'basic'
  allowDuplicate.value = false
  proxy.resetForm("customerRef")
}

/** 状态文本 */
function statusText(val) {
  return { 0: '无效', 1: '有效', 5: '公海' }[val] || '-'
}

/** 客户类型文本 */
function customerTypeText(val) {
  return { 1: '个人', 2: '企业' }[val] || '-'
}

/** 意向等级文本 */
function intentionText(val) {
  return { 1: '低', 2: '中', 3: '高', 4: '很有意向' }[val] || '-'
}

/** 意向等级标签类型 */
function getIntentionTagType(level) {
  const types = { 1: 'info', 2: 'warning', 3: 'success', 4: 'danger' }
  return types[level] || 'info'
}

/** 格式化金额 */
function formatMoney(val) {
  if (val == null || val === '') return '-'
  const num = parseFloat(val)
  if (isNaN(num)) return '-'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' 元'
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
    form.value = { ...response.data || response }
    open.value = true
    title.value = "修改客户"
  })
}

/** 详情按钮操作 */
function handleDetail(row) {
  getCustomer(row.id).then(res => {
    detailForm.value = res.data || res
    detailOpen.value = true
  })
}

/** 查看重复客户 */
function viewDuplicate(row) {
  duplicateOpen.value = false
  handleDetail(row)
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 查重函数 */
function checkDuplicate() {
  return new Promise((resolve) => {
    // 如果已确认跳过查重，直接通过
    if (allowDuplicate.value) {
      resolve(true)
      return
    }

    // 构建查重条件
    const conditions = []
    if (form.value.name) {
      conditions.push({ name: form.value.name })
    }
    if (form.value.phone) {
      conditions.push({ phone: form.value.phone })
    }
    if (form.value.idCard) {
      conditions.push({ idCard: form.value.idCard })
    }

    if (conditions.length === 0) {
      resolve(true)
      return
    }

    // 并行查询所有条件
    Promise.all(conditions.map(c => listCustomer({ ...c, pageSize: 100 }))).then(results => {
      const allDuplicates = new Map()

      results.forEach(res => {
        const records = res.data?.records || res.records || []
        records.forEach(item => {
          // 排除自己（编辑时）
          if (item.id !== form.value.id) {
            allDuplicates.set(item.id, item)
          }
        })
      })

      const duplicates = Array.from(allDuplicates.values())

      if (duplicates.length > 0) {
        duplicateList.value = duplicates
        duplicateOpen.value = true
        resolve(false)  // 暂停提交，等待用户确认
      } else {
        resolve(true)  // 无重复，继续提交
      }
    }).catch(() => {
      resolve(true)  // 查询失败，默认通过
    })
  })
}

/** 确认继续添加（跳过重复检查） */
function confirmAddAnyway() {
  allowDuplicate.value = true
  duplicateOpen.value = false
  // 继续提交
  doSubmit()
}

/** 提交表单 */
async function submitForm() {
  // 先验证表单基本规则
  const valid = await new Promise((resolve) => {
    proxy.$refs["customerRef"].validate((v) => resolve(v))
  })

  if (!valid) return

  // 状态联动：设为公海时自动填当天日期
  if (form.value.status === 5 && !form.value.publicSeaTime) {
    form.value.publicSeaTime = new Date().toISOString().split('T')[0]
  }

  // 新增时设置销售代表ID（从当前登录用户获取）
  if (!form.value.id) {
    const userId = Number(userStore.id) || 1  // 转为数字，默认1
    form.value.salesRepId = userId
    form.value.createdBy = userId  // 创建人
  }

  // 查重检查
  const canSubmit = await checkDuplicate()
  if (!canSubmit) return

  // 执行提交
  doSubmit()
}

/** 执行提交 */
function doSubmit() {
  // 调试：确保 createdBy 一定设置
  if (!form.value.id) {
    form.value.createdBy = Number(userStore.id) || 1
  }
  
  console.log('提交数据:', JSON.stringify(form.value))
  
  if (form.value.id != undefined) {
    updateCustomer(form.value).then(response => {
      proxy.$modal.msgSuccess("修改成功")
      open.value = false
      getList()
    }).catch(() => {
      proxy.$modal.msgError("修改失败")
    })
  } else {
    addCustomer(form.value).then(response => {
      proxy.$modal.msgSuccess("新增成功")
      open.value = false
      getList()
    }).catch(() => {
      proxy.$modal.msgError("新增失败")
    })
  }
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

// ============ 初始化 ============
getList()
</script>

<style scoped>
.mb8 {
  margin-bottom: 8px;
}

.dialog-footer {
  text-align: right;
}

/* Tab内容区域滚动条 */
:deep(.el-tabs__content) {
  overflow: hidden;
}
</style>

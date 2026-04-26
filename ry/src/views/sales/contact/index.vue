<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="客户名称" prop="customerId">
        <el-select v-model="queryParams.customerId" placeholder="选择客户" clearable filterable style="width: 200px">
          <el-option v-for="c in customerOptions" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="跟进方式" prop="contactType">
        <el-select v-model="queryParams.contactType" placeholder="请选择" clearable>
          <el-option label="电话" :value="1" />
          <el-option label="微信" :value="2" />
          <el-option label="面谈" :value="3" />
          <el-option label="其他" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="跟进日期" prop="dateRange">
        <el-date-picker v-model="queryParams.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" style="width: 240px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">新增跟进</el-button>
      </el-col>
    </el-row>

    <!-- 跟进记录列表 -->
    <el-table v-loading="loading" :data="dataList">
      <el-table-column label="客户名称" align="center" prop="customerName" min-width="120" show-overflow-tooltip>
        <template #default="scope">
          <el-link type="primary" @click="handleDetail(scope.row)">{{ scope.row.customerName || '-' }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="销售代表" align="center" prop="salesRepName" width="100" show-overflow-tooltip />
      <el-table-column label="跟进方式" align="center" prop="contactType" width="90">
        <template #default="scope">
          <el-tag :type="contactTypeTag(scope.row.contactType)" size="small">{{ contactTypeLabel(scope.row.contactType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="跟进日期" align="center" prop="contactDate" width="110" />
      <el-table-column label="跟进内容" align="center" prop="content" show-overflow-tooltip min-width="200" />
      <el-table-column label="意向变化" align="center" width="140">
        <template #default="scope">
          <span class="intention-change">
            <el-tag :type="intentionTag(scope.row.intentionBefore)" size="small">{{ intentionLabel(scope.row.intentionBefore) }}</el-tag>
            <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            <el-tag :type="intentionTag(scope.row.intentionAfter)" size="small">{{ intentionLabel(scope.row.intentionAfter) }}</el-tag>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="下次跟进" align="center" prop="followUpDate" width="110">
        <template #default="scope">
          <span :class="{ 'text-warning': isOverdue(scope.row.followUpDate) }">
            {{ scope.row.followUpDate || '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">编辑</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 详情对话框 -->
    <el-dialog title="跟进记录详情" v-model="detailOpen" width="600px" append-to-body>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="客户名称">{{ detailForm.customerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="销售代表">{{ detailForm.salesRepName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="跟进方式">{{ contactTypeLabel(detailForm.contactType) }}</el-descriptions-item>
        <el-descriptions-item label="跟进日期">{{ detailForm.contactDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="跟进前意向">{{ intentionLabel(detailForm.intentionBefore) }}</el-descriptions-item>
        <el-descriptions-item label="跟进后意向">{{ intentionLabel(detailForm.intentionAfter) }}</el-descriptions-item>
        <el-descriptions-item label="下次跟进">{{ detailForm.followUpDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="跟进内容" :span="2">{{ detailForm.content || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ detailForm.createdAt || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailOpen = false">关 闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="title" v-model="open" width="650px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="form.customerId" placeholder="请选择客户" filterable remote reserve-keyword :remote-method="searchCustomer" :loading="customerLoading" @change="onCustomerChange" style="width: 100%">
            <el-option v-for="c in customerOptions" :key="c.id" :label="c.name + ' (' + c.phone + ')'" :value="c.id">
              <span>{{ c.name }}</span>
              <span class="option-phone">{{ c.phone }}</span>
            </el-option>
          </el-select>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="跟进方式" prop="contactType">
              <el-select v-model="form.contactType" placeholder="请选择" style="width: 100%">
                <el-option label="电话" :value="1" />
                <el-option label="微信" :value="2" />
                <el-option label="面谈" :value="3" />
                <el-option label="其他" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="跟进日期" prop="contactDate">
              <el-date-picker v-model="form.contactDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="跟进内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入跟进内容" maxlength="1000" show-word-limit />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="跟进前意向">
              <el-select v-model="form.intentionBefore" placeholder="选择客户后自动填充" disabled style="width: 100%">
                <el-option label="无意向" :value="0" />
                <el-option label="低意向" :value="1" />
                <el-option label="中意向" :value="2" />
                <el-option label="高意向" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="跟进后意向" prop="intentionAfter">
              <el-select v-model="form.intentionAfter" placeholder="请选择" style="width: 100%">
                <el-option label="无意向" :value="0" />
                <el-option label="低意向" :value="1" />
                <el-option label="中意向" :value="2" />
                <el-option label="高意向" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="下次跟进" prop="followUpDate">
          <el-date-picker v-model="form.followUpDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listContactRecord, getContactRecord, addContactRecord, updateContactRecord, delContactRecord } from "@/api/sales/contactRecord"
import { listCustomer } from "@/api/sales/customer"
import { listUser } from "@/api/system/user"
import useUserStore from '@/store/modules/user'
import { ArrowRight } from '@element-plus/icons-vue'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()

// 列表数据
const dataList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const open = ref(false)
const detailOpen = ref(false)
const detailForm = ref({})

// 客户选择相关
const customerOptions = ref([])
const customerLoading = ref(false)
const userOptions = ref([])
const userMap = ref({})  // id -> name 映射

const contactTypeMap = { 1: '电话', 2: '微信', 3: '面谈', 4: '其他' }
const intentionMap = { 0: '无意向', 1: '低意向', 2: '中意向', 3: '高意向' }

function contactTypeLabel(val) { return contactTypeMap[val] || '未知' }
function contactTypeTag(val) { return { 1: '', 2: 'success', 3: 'warning', 4: 'info' }[val] || 'info' }
function intentionLabel(val) { return intentionMap[val] || '-' }
function intentionTag(val) { return { 0: 'info', 1: 'warning', 2: '', 3: 'success' }[val] || 'info' }

// 是否逾期
function isOverdue(date) {
  if (!date) return false
  return new Date(date) < new Date(new Date().toDateString())
}

const data = reactive({
  form: {},
  queryParams: { 
    pageNum: 1, 
    pageSize: 10, 
    customerId: undefined, 
    contactType: undefined,
    dateRange: undefined
  },
  rules: {
    customerId: [{ required: true, message: "请选择客户", trigger: "change" }],
    contactType: [{ required: true, message: "请选择跟进方式", trigger: "change" }],
    contactDate: [{ required: true, message: "请选择跟进日期", trigger: "change" }],
    content: [{ required: true, message: "跟进内容不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

// 获取跟进记录列表
function getList() {
  loading.value = true
  const params = { ...queryParams.value }
  // 处理日期范围
  if (params.dateRange && params.dateRange.length === 2) {
    params.startDate = params.dateRange[0]
    params.endDate = params.dateRange[1]
    delete params.dateRange
  }
  
  listContactRecord(params).then(response => {
    const records = response.data?.records || response.records || []
    // 直接使用后端返回的客户名称和销售代表名称
    dataList.value = records.map(r => ({
      ...r,
      customerName: r.customerName || r.customerId,
      salesRepName: r.salesRepName || userMap.value[r.salesRepId] || r.salesRepId
    }))
    total.value = response.data?.total || response.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

// 获取客户列表
function loadCustomers() {
  listCustomer({ pageSize: 100, status: 1 }).then(res => {
    customerOptions.value = res.data?.records || res.records || []
  })
}

// 搜索客户（远程搜索）
function searchCustomer(query) {
  if (!query) {
    loadCustomers()
    return
  }
  customerLoading.value = true
  listCustomer({ name: query, pageSize: 50 }).then(res => {
    customerOptions.value = res.data?.records || res.records || []
    customerLoading.value = false
  }).catch(() => {
    customerLoading.value = false
  })
}

// 选择客户后自动填充跟进前意向
function onCustomerChange(customerId) {
  if (customerId) {
    const customer = customerOptions.value.find(c => c.id === customerId)
    if (customer) {
      form.value.intentionBefore = customer.intentionLevel
    }
  }
}

// 获取用户列表（用于显示名称）
function loadUsers() {
  listUser({ pageSize: 100 }).then(res => {
    const users = res.data?.records || res.records || []
    userOptions.value = users
    // 构建映射
    users.forEach(u => {
      userMap.value[u.id] = u.realName || u.username || u.userName || u.nickName
    })
  })
}

// 取消
function cancel() { open.value = false; reset() }

// 重置表单
function reset() {
  form.value = { 
    id: undefined, 
    customerId: undefined,
    contactType: undefined, 
    contactDate: new Date().toISOString().split('T')[0],  // 默认今天
    content: undefined, 
    intentionBefore: undefined, 
    intentionAfter: undefined, 
    followUpDate: undefined 
  }
  proxy.resetForm("formRef")
}

// 搜索
function handleQuery() { queryParams.value.pageNum = 1; getList() }

// 重置搜索
function resetQuery() { 
  queryParams.value = { pageNum: 1, pageSize: 10, customerId: undefined, contactType: undefined, dateRange: undefined }
  proxy.resetForm("queryRef")
  handleQuery() 
}

// 新增
function handleAdd() { 
  reset()
  // 自动设置销售代表为当前用户
  form.value.salesRepId = Number(userStore.id) || 1
  open.value = true
  title.value = "新增跟进记录"
}

// 详情 - 直接使用列表行数据（已包含关联查询的名称）
function handleDetail(row) {
  detailForm.value = { ...row }
  detailOpen.value = true
}

// 编辑
function handleUpdate(row) {
  reset()
  getContactRecord(row.id).then(response => {
    form.value = response.data || response
    open.value = true
    title.value = "修改跟进记录"
  })
}

// 提交
function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      // 新增时设置销售代表和创建人
      if (!form.value.id) {
        const userId = Number(userStore.id) || 1
        form.value.salesRepId = userId
        form.value.createdBy = userId
      }
      
      const fn = form.value.id ? updateContactRecord : addContactRecord
      fn(form.value).then(() => {
        proxy.$modal.msgSuccess(form.value.id ? "修改成功" : "新增成功")
        open.value = false
        getList()
      }).catch(error => {
        proxy.$modal.msgError(error.message || "操作失败")
      })
    }
  })
}

// 删除
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除该跟进记录？').then(() => delContactRecord(row.id)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

// 初始化
onMounted(() => {
  loadCustomers()
  loadUsers()
  getList()
})
</script>

<style scoped>
.intention-change {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}
.arrow-icon {
  color: #999;
  font-size: 12px;
}
.option-phone {
  float: right;
  color: #999;
  font-size: 12px;
}
.text-warning {
  color: #E6A23C;
  font-weight: 500;
}
</style>

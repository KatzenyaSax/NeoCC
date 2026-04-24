<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="客户名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入客户名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 公海客户列表 -->
    <el-table v-loading="loading" :data="publicSeaList" border class="mt16">
      <el-table-column label="ID" align="center" prop="id" width="80" />
      <el-table-column label="客户名称" align="center" prop="name" />
      <el-table-column label="联系电话" align="center" prop="phone" />
      <el-table-column label="公司名称" align="center" prop="companyName" />
      <el-table-column label="意向等级" align="center" prop="intentionLevel" width="100">
        <template #default="scope">
          {{ intentionText(scope.row.intentionLevel) }}
        </template>
      </el-table-column>
      <el-table-column label="公海时间" align="center" prop="publicSeaTime" width="120" />
      <el-table-column label="公海原因" align="center" prop="publicSeaReason" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="240">
        <template #default="scope">
          <el-button link type="primary" icon="User" @click="handleTransfer(scope.row)">转移</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button v-if="isManager" link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 转移弹窗 -->
    <el-dialog title="转移客户给销售代表" v-model="transferOpen" width="500px" append-to-body>
      <el-form ref="transferRef" :model="transferForm" label-width="100px">
        <el-form-item label="客户名称">
          <span>{{ transferForm.customerName }}</span>
        </el-form-item>
        <el-form-item label="目标销售代表" prop="toRepId">
          <el-select
            v-model="transferForm.toRepId"
            placeholder="请选择销售代表"
            :disabled="isSalesRep"
            style="width: 100%"
          >
            <el-option
              v-for="rep in salesRepList"
              :key="rep.id"
              :label="rep.realName"
              :value="rep.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="转移原因" prop="reason">
          <el-input v-model="transferForm.reason" type="textarea" placeholder="请输入转移原因" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="transferOpen = false">取 消</el-button>
          <el-button type="primary" @click="confirmTransfer">确认转移</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 修改弹窗 -->
    <el-dialog title="修改客户信息" v-model="editOpen" width="600px" append-to-body>
      <el-form ref="editRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="客户名称" prop="name">
          <el-input v-model="editForm.name" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="editForm.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="公司名称" prop="companyName">
          <el-input v-model="editForm.companyName" placeholder="请输入公司名称" />
        </el-form-item>
        <el-form-item label="客户类型" prop="customerType">
          <el-select v-model="editForm.customerType" placeholder="请选择客户类型">
            <el-option label="个人" :value="1" />
            <el-option label="企业" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="意向等级" prop="intentionLevel">
          <el-select v-model="editForm.intentionLevel" placeholder="请选择意向等级">
            <el-option label="低" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="高" :value="3" />
            <el-option label="很有意向" :value="4" />
            <el-option label="已签约" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源" prop="source">
          <el-input v-model="editForm.source" placeholder="请输入来源" />
        </el-form-item>
        <el-form-item label="贷款意向金额" prop="loanIntentionAmount">
          <el-input-number v-model="editForm.loanIntentionAmount" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="贷款意向产品" prop="loanIntentionProduct">
          <el-input v-model="editForm.loanIntentionProduct" placeholder="请输入贷款意向产品" />
        </el-form-item>
        <el-form-item label="公海原因" prop="publicSeaReason">
          <el-input v-model="editForm.publicSeaReason" type="textarea" placeholder="请输入公海原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editOpen = false">取 消</el-button>
          <el-button type="primary" @click="confirmEdit">确 定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { getPublicSeaPage, transferCustomer, listSalesReps } from '@/api/sales/publicSea'
import { getCustomer, updateCustomer, delCustomer } from '@/api/sales/customer'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()

const loading = ref(false)
const showSearch = ref(true)
const total = ref(0)
const publicSeaList = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  name: ''
})

// 转移弹窗
const transferOpen = ref(false)
const transferForm = ref({
  customerId: null,
  customerName: '',
  toRepId: null,
  reason: ''
})
const salesRepList = ref([])

// 修改弹窗
const editOpen = ref(false)
const editForm = ref({})
const editRules = {
  name: [{ required: true, message: '客户名称不能为空', trigger: 'blur' }]
}

// 权限判断
const userStore = useUserStore()
const isSalesRep = computed(() => userStore.roles?.includes('sales_rep'))
const isManager = computed(() => userStore.roles?.some(r => ['sales_manager', 'admin'].includes(r)))

/** 公海客户意向等级文字 */
function intentionText(level) {
  const map = { 1: '低', 2: '中', 3: '高', 4: '很有意向', 5: '已签约' }
  return map[level] || '-'
}

/** 查询公海客户列表 */
function getList() {
  loading.value = true
  getPublicSeaPage(queryParams.value).then(res => {
    const data = res.data || {}
    publicSeaList.value = data.records || []
    total.value = data.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

/** 搜索按钮 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/** 重置按钮 */
function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.name = ''
  queryParams.pageNum = 1
  getList()
}

/** 转移按钮 */
function handleTransfer(row) {
  transferForm.value = {
    customerId: row.id,
    customerName: row.name,
    toRepId: isSalesRep.value ? userStore.id : null,
    reason: ''
  }
  transferOpen.value = true
  // 加载销售代表列表
  listSalesReps().then(res => {
    salesRepList.value = res.data || []
  })
}

/** 确认转移 */
function confirmTransfer() {
  if (!transferForm.value.toRepId) {
    proxy.$modal.msgWarning('请选择目标销售代表')
    return
  }
  transferCustomer({
    customerId: transferForm.value.customerId,
    toRepId: transferForm.value.toRepId,
    reason: transferForm.value.reason,
    operatorId: userStore.id
  }).then(() => {
    proxy.$modal.msgSuccess('转移成功')
    transferOpen.value = false
    getList()
  })
}

/** 修改按钮 */
function handleUpdate(row) {
  getCustomer(row.id).then(res => {
    editForm.value = { ...(res.data || res) }
    editOpen.value = true
  })
}

/** 确认修改 */
function confirmEdit() {
  updateCustomer(editForm.value).then(() => {
    proxy.$modal.msgSuccess('修改成功')
    editOpen.value = false
    getList()
  })
}

/** 删除按钮 */
function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除客户"' + row.name + '"？').then(() => {
    return delCustomer(row.id)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

getList()
</script>

<style scoped>
.mt16 { margin-top: 16px; }
</style>

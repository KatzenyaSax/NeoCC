<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="区域名称" prop="zoneName">
        <el-input v-model="queryParams.zoneName" placeholder="请输入区域名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
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
      <el-table-column label="区域编码" align="center" prop="zoneCode" width="120" />
      <el-table-column label="区域名称" align="center" prop="zoneName" />
      <el-table-column label="负责人" align="center" prop="directorName" width="120">
        <template #default="scope">
          {{ scope.row.directorName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="排序" align="center" prop="sortOrder" width="80" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createdAt" width="160" />
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 查看详情对话框 -->
    <el-dialog title="区域详情" v-model="viewOpen" width="550px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="区域编码">{{ viewData.zoneCode }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ viewData.directorName || viewData.directorId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="区域名称" :span="2">{{ viewData.zoneName }}</el-descriptions-item>
        <el-descriptions-item label="排序">{{ viewData.sortOrder }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewData.status === 1 ? 'success' : 'danger'">{{ viewData.status === 1 ? '启用' : '禁用' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ viewData.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ viewData.updatedAt }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="550px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="区域编码" prop="zoneCode">
              <el-input v-model="form.zoneCode" placeholder="请输入区域编码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="区域名称" prop="zoneName">
              <el-input v-model="form.zoneName" placeholder="请输入区域名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="负责人" prop="directorId">
              <el-select v-model="form.directorId" placeholder="请选择负责人" clearable filterable style="width: 100%">
                <el-option v-for="user in userOptions" :key="user.id" :label="user.realName + ' (' + user.id + ')'" :value="user.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
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
import { listZone, getZone, addZone, updateZone, delZone } from "@/api/system/zone"
import { listUser } from "@/api/system/user"

const { proxy } = getCurrentInstance()
const dataList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})
const userOptions = ref([])

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    zoneName: undefined,
    status: undefined
  },
  rules: {
    zoneCode: [{ required: true, message: "区域编码不能为空", trigger: "blur" }],
    zoneName: [{ required: true, message: "区域名称不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listZone(queryParams.value).then(response => {
    dataList.value = response.data?.records || response.records || []
    total.value = response.data?.total || response.total || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}

function cancel() { open.value = false; reset() }
function reset() {
  form.value = {
    id: undefined, zoneCode: undefined, zoneName: undefined,
    directorId: undefined, sortOrder: 0, status: 1
  }
  proxy.resetForm("formRef")
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function handleAdd() { reset(); loadOptions(); open.value = true; title.value = "新增区域" }

function handleUpdate(row) {
  reset()
  loadOptions()
  getZone(row.id).then(response => {
    form.value = response.data || response
    open.value = true
    title.value = "修改区域"
  })
}

/** 加载下拉选项数据 */
function loadOptions() {
  listUser({ pageNum: 1, pageSize: 1000 }).then(response => {
    userOptions.value = response.data?.records || response.records || []
  })
}

function handleView(row) {
  viewData.value = row
  viewOpen.value = true
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (!valid) return
    const fn = form.value.id ? updateZone : addZone
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.id ? "修改成功" : "新增成功")
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除区域【' + row.zoneName + '】？删除后将无法恢复！').then(() => delZone(row.id)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>

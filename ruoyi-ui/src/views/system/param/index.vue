<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="参数键" prop="paramKey">
        <el-input v-model="queryParams.paramKey" placeholder="请输入参数键" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="参数分组" prop="paramGroup">
        <el-input v-model="queryParams.paramGroup" placeholder="请输入参数分组" clearable @keyup.enter="handleQuery" />
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
      <el-table-column label="参数键" align="center" prop="paramKey" width="200" />
      <el-table-column label="参数值" align="center" prop="paramValue">
        <template #default="scope">
          <span>{{ scope.row.paramValue && scope.row.paramValue.length > 50 ? scope.row.paramValue.slice(0, 50) + '...' : scope.row.paramValue }}</span>
        </template>
      </el-table-column>
      <el-table-column label="参数类型" align="center" prop="paramType" width="120" />
      <el-table-column label="参数分组" align="center" prop="paramGroup" width="120" />
      <el-table-column label="排序" align="center" prop="sortOrder" width="80" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" width="200">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleView(scope.row)">查看</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 查看对话框 -->
    <el-dialog title="参数详情" v-model="viewOpen" width="550px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="参数键">{{ viewData.paramKey }}</el-descriptions-item>
        <el-descriptions-item label="参数类型">{{ viewData.paramType }}</el-descriptions-item>
        <el-descriptions-item label="参数分组">{{ viewData.paramGroup }}</el-descriptions-item>
        <el-descriptions-item label="排序">{{ viewData.sortOrder }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewData.status === 1 ? 'success' : 'danger'">{{ viewData.status === 1 ? '启用' : '禁用' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="参数值" :span="2">{{ viewData.paramValue }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ viewData.remark }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="title" v-model="open" width="550px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="参数键" prop="paramKey">
          <el-input v-model="form.paramKey" placeholder="请输入参数键" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="参数值" prop="paramValue">
          <el-input v-model="form.paramValue" placeholder="请输入参数值" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="参数类型" prop="paramType">
          <el-select v-model="form.paramType" placeholder="请选择参数类型" clearable style="width:100%">
            <el-option label="字符串" value="string" />
            <el-option label="数字" value="number" />
            <el-option label="布尔值" value="boolean" />
            <el-option label="JSON" value="json" />
          </el-select>
        </el-form-item>
        <el-form-item label="参数分组" prop="paramGroup">
          <el-input v-model="form.paramGroup" placeholder="请输入参数分组" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注" />
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
import { listParam, getParam, addParam, updateParam, delParam } from "@/api/system/param"

const { proxy } = getCurrentInstance()
const dataList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const open = ref(false)
const viewOpen = ref(false)
const viewData = ref({})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, paramKey: undefined, paramGroup: undefined, status: undefined },
  rules: {
    paramKey: [{ required: true, message: "参数键不能为空", trigger: "blur" }],
    paramValue: [{ required: true, message: "参数值不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listParam(queryParams.value).then(response => {
    dataList.value = response.data?.records || response.records || []
    total.value = response.data?.total || response.total || 0
    loading.value = false
  })
}

function cancel() { open.value = false; reset() }
function reset() {
  form.value = { id: undefined, paramKey: undefined, paramValue: undefined, paramType: undefined, paramGroup: undefined, sortOrder: 0, status: 1, remark: undefined }
  proxy.resetForm("formRef")
}
function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function handleAdd() { reset(); open.value = true; title.value = "新增参数" }

function handleUpdate(row) {
  reset()
  getParam(row.id).then(response => {
    form.value = response.data || response
    open.value = true
    title.value = "修改参数"
  })
}

function handleView(row) {
  viewData.value = row
  viewOpen.value = true
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      const fn = form.value.id ? updateParam : addParam
      fn(form.value).then(() => {
        proxy.$modal.msgSuccess(form.value.id ? "修改成功" : "新增成功")
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除参数"' + row.paramKey + '"？').then(() => delParam(row.id)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>

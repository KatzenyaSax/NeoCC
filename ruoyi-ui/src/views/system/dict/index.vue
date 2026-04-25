<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="字典类型" prop="dictType">
        <el-input v-model="queryParams.dictType" placeholder="请输入字典类型" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="字典标签" prop="dictLabel">
        <el-input v-model="queryParams.dictLabel" placeholder="请输入字典标签" clearable @keyup.enter="handleQuery" />
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

    <el-table v-loading="loading" :data="dataList" :row-key="row => row.id" @sort-change="handleSortChange">
      <el-table-column label="ID" align="center" prop="id" width="80" sortable />
      <el-table-column label="字典类型" align="center" prop="dictType" width="150" />
      <el-table-column label="字典代码" align="center" prop="dictCode" width="150" />
      <el-table-column label="字典标签" align="center" prop="dictLabel" />
      <el-table-column label="字典值" align="center" prop="dictValue" />
      <el-table-column label="排序" align="center" prop="sortOrder" width="80" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" width="160">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="550px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="form.dictType" placeholder="请输入字典类型" />
        </el-form-item>
        <el-form-item label="字典代码" prop="dictCode">
          <el-input v-model="form.dictCode" placeholder="请输入字典代码" />
        </el-form-item>
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="form.dictLabel" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典值" prop="dictValue">
          <el-input v-model="form.dictValue" placeholder="请输入字典值" />
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
import { listDict, getDict, addDict, updateDict, delDict, getMinUnusedDictId } from "@/api/system/dict"

const { proxy } = getCurrentInstance()
const dataList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const open = ref(false)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, dictType: undefined, dictLabel: undefined, status: undefined, sortField: 'id', sortOrder: 'asc' },
  rules: {
    dictType: [{ required: true, message: "字典类型不能为空", trigger: "blur" }],
    dictCode: [{ required: true, message: "字典代码不能为空", trigger: "blur" }],
    dictLabel: [{ required: true, message: "字典标签不能为空", trigger: "blur" }],
    dictValue: [{ required: true, message: "字典值不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listDict(queryParams.value).then(response => {
    dataList.value = response.data?.records || response.records || []
    total.value = response.data?.total || response.total || 0
    loading.value = false
  })
}

function cancel() { open.value = false; reset() }
function reset() {
  form.value = { id: undefined, dictType: undefined, dictCode: undefined, dictLabel: undefined, dictValue: undefined, sortOrder: 0, status: 1, remark: undefined }
  proxy.resetForm("formRef")
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
  getMinUnusedDictId().then(res => {
    form.value.id = res.data || res
    open.value = true
    title.value = "新增字典"
  })
}

function handleUpdate(row) {
  reset()
  getDict(row.id).then(response => {
    form.value = response.data || response
    open.value = true
    title.value = "修改字典"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (valid) {
      const isAdd = title.value.includes('新增')
      const fn = isAdd ? addDict : updateDict
      fn(form.value).then(() => {
        proxy.$modal.msgSuccess(isAdd ? "新增成功" : "修改成功")
        open.value = false
        getList()
      })
    }
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除字典"' + row.dictLabel + '"？').then(() => updateDict({ id: row.id, deleted: 1 })).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>

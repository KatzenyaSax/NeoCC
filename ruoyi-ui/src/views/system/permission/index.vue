<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="权限名称" prop="permName">
        <el-input v-model="queryParams.permName" placeholder="请输入权限名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="权限编码" prop="permCode">
        <el-input v-model="queryParams.permCode" placeholder="请输入权限编码" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="permType">
        <el-select v-model="queryParams.permType" placeholder="请选择类型" clearable style="width:120px">
          <el-option label="目录" :value="0" />
          <el-option label="菜单" :value="1" />
          <el-option label="按钮" :value="2" />
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
      <el-table-column label="ID" align="center" prop="id" width="70" />
      <el-table-column label="权限名称" align="center" prop="permName" width="150" />
      <el-table-column label="权限编码" align="center" prop="permCode" width="180" />
      <el-table-column label="类型" align="center" prop="permType" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.permType === 0 ? '' : scope.row.permType === 1 ? 'success' : 'info'">
            {{ ['目录', '菜单', '按钮'][scope.row.permType] || '-' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="路由路径" align="center" prop="path" />
      <el-table-column label="组件" align="center" prop="component" />
      <el-table-column label="排序" align="center" prop="sortOrder" width="70" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="650px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="权限名称" prop="permName">
              <el-input v-model="form.permName" placeholder="请输入权限名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限编码" prop="permCode">
              <el-input v-model="form.permCode" placeholder="如：system:user:list" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="权限类型" prop="permType">
              <el-select v-model="form.permType" placeholder="请选择" style="width:100%">
                <el-option label="目录" :value="0" />
                <el-option label="菜单" :value="1" />
                <el-option label="按钮" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上级ID" prop="parentId">
              <el-input v-model="form.parentId" placeholder="请输入上级ID（选填）" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="路由路径" prop="path">
              <el-input v-model="form.path" placeholder="请输入路由路径" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组件路径" prop="component">
              <el-input v-model="form.component" placeholder="如：system/user/index" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="图标" prop="icon">
              <el-input v-model="form.icon" placeholder="图标名称" />
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
import { listPermission, getPermission, addPermission, updatePermission, delPermission } from "@/api/system/permission"

const { proxy } = getCurrentInstance()
const dataList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const open = ref(false)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, permName: undefined, permCode: undefined, permType: undefined },
  rules: {
    permName: [{ required: true, message: "权限名称不能为空", trigger: "blur" }],
    permCode: [{ required: true, message: "权限编码不能为空", trigger: "blur" }],
    permType: [{ required: true, message: "请选择权限类型", trigger: "change" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listPermission(queryParams.value).then(response => {
    dataList.value = response.data?.records || response.records || []
    total.value = response.data?.total || response.total || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}

function cancel() { open.value = false; reset() }
function reset() {
  form.value = {
    id: undefined, permName: undefined, permCode: undefined, permType: 1,
    parentId: undefined, path: undefined, component: undefined, icon: undefined,
    sortOrder: 0, status: 1
  }
  proxy.resetForm && proxy.resetForm("formRef")
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm && proxy.resetForm("queryRef"); handleQuery() }
function handleAdd() { reset(); open.value = true; title.value = "新增菜单/权限" }

function handleUpdate(row) {
  reset()
  getPermission(row.id).then(response => {
    form.value = response.data || response
    open.value = true
    title.value = "修改菜单/权限"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (!valid) return
    const fn = form.value.id ? updatePermission : addPermission
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.id ? "修改成功" : "新增成功")
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除权限【' + row.permName + '】？').then(() => delPermission(row.id)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>

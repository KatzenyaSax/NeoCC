<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="角色名称" prop="roleName">
        <el-input v-model="queryParams.roleName" placeholder="请输入角色名称" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="角色编码" prop="roleCode">
        <el-input v-model="queryParams.roleCode" placeholder="请输入角色编码" clearable @keyup.enter="handleQuery" />
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
      <el-table-column label="ID" align="center" prop="id" width="70" sortable />
      <el-table-column label="角色编码" align="center" prop="roleCode" width="140" />
      <el-table-column label="角色名称" align="center" prop="roleName" width="140" />
      <el-table-column label="描述" align="center" prop="description" />
      <el-table-column label="排序" align="center" prop="sortOrder" width="80" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createdAt" width="160" />
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="primary" icon="Setting" @click="handlePermission(scope.row)">分配权限</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="550px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="角色编码" prop="roleCode">
              <el-input v-model="form.roleCode" placeholder="请输入角色编码" :disabled="!!form.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色名称" prop="roleName">
              <el-input v-model="form.roleName" placeholder="请输入角色名称" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入角色描述" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog title="分配权限" v-model="permOpen" width="600px" append-to-body>
      <div style="margin-bottom:12px">角色：<strong>{{ currentRole.roleName }}</strong></div>
      <el-tree
        ref="permTreeRef"
        :data="permTree"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedPermIds"
        :props="{ children: 'children', label: 'permName' }"
        style="max-height:400px;overflow-y:auto"
      />
      <template #footer>
        <el-button type="primary" @click="submitPermission">确 定</el-button>
        <el-button @click="permOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listRole, getRole, addRole, updateRole, delRole, getRolePermissions, assignRolePermissions, getMinUnusedRoleId } from "@/api/system/role"
import { treePermission } from "@/api/system/permission"

const { proxy } = getCurrentInstance()
const dataList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const open = ref(false)
const permOpen = ref(false)
const permTree = ref([])
const checkedPermIds = ref([])
const currentRole = ref({})
const permTreeRef = ref(null)

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, sortField: 'id', sortOrder: 'asc', roleName: undefined, roleCode: undefined },
  rules: {
    roleCode: [{ required: true, message: "角色编码不能为空", trigger: "blur" }],
    roleName: [{ required: true, message: "角色名称不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listRole(queryParams.value).then(response => {
    dataList.value = response.data?.records || response.records || []
    total.value = response.data?.total || response.total || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}

function cancel() { open.value = false; reset() }
function reset() {
  form.value = { id: undefined, roleCode: undefined, roleName: undefined, description: undefined, sortOrder: 0, status: 1 }
  proxy.resetForm && proxy.resetForm("formRef")
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function handleSortChange({ prop, order }) {
  queryParams.value.sortField = prop
  queryParams.value.sortOrder = order === 'ascending' ? 'asc' : order === 'descending' ? 'desc' : ''
  getList()
}
function resetQuery() { proxy.resetForm && proxy.resetForm("queryRef"); handleQuery() }
function handleAdd() {
  reset()
  open.value = true
  title.value = "新增角色"
  getMinUnusedRoleId().then(res => {
    form.value.id = res.data || res
  })
}

function handleUpdate(row) {
  reset()
  getRole(row.id).then(response => {
    form.value = response.data || response
    open.value = true
    title.value = "修改角色"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (!valid) return
    const fn = form.value.id ? updateRole : addRole
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.id ? "修改成功" : "新增成功")
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除角色【' + row.roleName + '】？').then(() => updateRole({ id: row.id, deleted: 1 })).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

// 分配权限
function handlePermission(row) {
  currentRole.value = row
  treePermission().then(res => {
    permTree.value = res.data || res || []
    return getRolePermissions(row.id)
  }).then(res => {
    checkedPermIds.value = res.data || res || []
    permOpen.value = true
  })
}

function submitPermission() {
  const checkedIds = permTreeRef.value ? permTreeRef.value.getCheckedKeys() : []
  const halfCheckedIds = permTreeRef.value ? permTreeRef.value.getHalfCheckedKeys() : []
  const allIds = [...checkedIds, ...halfCheckedIds]
  assignRolePermissions(currentRole.value.id, allIds).then(() => {
    proxy.$modal.msgSuccess("权限分配成功")
    permOpen.value = false
  })
}

getList()
</script>

<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="真实姓名" prop="realName">
        <el-input v-model="queryParams.realName" placeholder="请输入真实姓名" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width:120px">
          <el-option label="正常" :value="1" />
          <el-option label="锁定" :value="0" />
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
      <el-table-column label="用户名" align="center" prop="username" width="120" />
      <el-table-column label="真实姓名" align="center" prop="realName" width="100" />
      <el-table-column label="手机号" align="center" prop="phone" width="120" />
      <el-table-column label="邮箱" align="center" prop="email" />
      <el-table-column label="部门ID" align="center" prop="deptId" width="80" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '正常' : '锁定' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最后登录时间" align="center" prop="lastLoginAt" width="160" />
      <el-table-column label="操作" align="center" width="240" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="primary" icon="Key" @click="handleRole(scope.row)">分配角色</el-button>
          <el-button link type="warning" icon="Lock" @click="handlePassword(scope.row)">改密码</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="600px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" :disabled="!!form.id" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row v-if="!form.id">
          <el-col :span="12">
            <el-form-item label="初始密码" prop="password">
              <el-input v-model="form.password" placeholder="请输入初始密码" type="password" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="部门ID" prop="deptId">
              <el-input v-model="form.deptId" placeholder="请输入部门ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">正常</el-radio>
                <el-radio :label="0">锁定</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 分配角色对话框 -->
    <el-dialog title="分配角色" v-model="roleOpen" width="500px" append-to-body>
      <div style="margin-bottom:12px">用户：<strong>{{ currentUser.username }}</strong></div>
      <el-checkbox-group v-model="selectedRoleIds">
        <el-checkbox v-for="role in allRoles" :key="role.id" :label="role.id" style="width:200px;margin-bottom:8px">
          {{ role.roleName }}（{{ role.roleCode }}）
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button type="primary" @click="submitRole">确 定</el-button>
        <el-button @click="roleOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog title="修改密码" v-model="pwdOpen" width="400px" append-to-body>
      <div style="margin-bottom:12px">用户：<strong>{{ currentUser.username }}</strong></div>
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="90px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitPassword">确 定</el-button>
        <el-button @click="pwdOpen = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { listUser, getUser, addUser, updateUser, delUser, getUserRoles, assignUserRoles, changePassword } from "@/api/system/user"
import { listAllRoles } from "@/api/system/role"

const { proxy } = getCurrentInstance()
const dataList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const open = ref(false)
const roleOpen = ref(false)
const pwdOpen = ref(false)
const allRoles = ref([])
const selectedRoleIds = ref([])
const currentUser = ref({})

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, username: undefined, realName: undefined, status: undefined },
  rules: {
    username: [{ required: true, message: "用户名不能为空", trigger: "blur" }],
    realName: [{ required: true, message: "真实姓名不能为空", trigger: "blur" }],
    password: [{ required: true, message: "初始密码不能为空", trigger: "blur" }, { min: 6, message: "密码至少6位", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

const pwdForm = ref({ newPassword: '', confirmPassword: '' })
const pwdRules = {
  newPassword: [{ required: true, message: "新密码不能为空", trigger: "blur" }, { min: 6, message: "密码至少6位", trigger: "blur" }],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.value.newPassword) { callback(new Error("两次密码不一致")) } else { callback() }
      }, trigger: "blur"
    }
  ]
}

function getList() {
  loading.value = true
  listUser(queryParams.value).then(response => {
    dataList.value = response.data?.records || response.records || []
    total.value = response.data?.total || response.total || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}

function cancel() { open.value = false; reset() }
function reset() {
  form.value = { id: undefined, username: undefined, realName: undefined, password: undefined, phone: undefined, email: undefined, deptId: undefined, status: 1 }
  proxy.resetForm && proxy.resetForm("formRef")
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm && proxy.resetForm("queryRef"); handleQuery() }
function handleAdd() { reset(); open.value = true; title.value = "新增用户" }

function handleUpdate(row) {
  reset()
  getUser(row.id).then(response => {
    form.value = response.data || response
    open.value = true
    title.value = "修改用户"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (!valid) return
    const fn = form.value.id ? updateUser : addUser
    fn(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.id ? "修改成功" : "新增成功")
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除用户【' + row.username + '】？').then(() => delUser(row.id)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

// 分配角色
function handleRole(row) {
  currentUser.value = row
  listAllRoles().then(res => {
    allRoles.value = res.data || res || []
    return getUserRoles(row.id)
  }).then(res => {
    selectedRoleIds.value = res.data || res || []
    roleOpen.value = true
  })
}

function submitRole() {
  assignUserRoles(currentUser.value.id, selectedRoleIds.value).then(() => {
    proxy.$modal.msgSuccess("分配角色成功")
    roleOpen.value = false
  })
}

// 修改密码
function handlePassword(row) {
  currentUser.value = row
  pwdForm.value = { newPassword: '', confirmPassword: '' }
  pwdOpen.value = true
}

function submitPassword() {
  proxy.$refs["pwdFormRef"].validate(valid => {
    if (!valid) return
    changePassword(currentUser.value.id, { newPassword: pwdForm.value.newPassword }).then(() => {
      proxy.$modal.msgSuccess("密码修改成功")
      pwdOpen.value = false
    })
  })
}

getList()
</script>

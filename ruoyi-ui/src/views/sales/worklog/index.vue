<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="销售代表ID" prop="salesRepId">
        <el-input v-model="queryParams.salesRepId" placeholder="请输入销售代表ID" clearable @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="日志日期" prop="logDate">
        <el-date-picker v-model="queryParams.logDate" type="date" placeholder="请选择日期" value-format="YYYY-MM-DD" clearable />
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
      <el-table-column label="销售代表ID" align="center" prop="salesRepId" width="110" />
      <el-table-column label="日志日期" align="center" prop="logDate" width="120" />
      <el-table-column label="拨打电话数" align="center" prop="callsMade" width="110" />
      <el-table-column label="有效通话" align="center" prop="effectiveCalls" width="90" />
      <el-table-column label="新增意向" align="center" prop="newIntentions" width="90" />
      <el-table-column label="意向客户" align="center" prop="intentionClients" width="90" />
      <el-table-column label="面谈客户" align="center" prop="faceToFaceClients" width="90" />
      <el-table-column label="签约合同" align="center" prop="signedContracts" width="90" />
      <el-table-column label="日志内容" align="center" prop="content" show-overflow-tooltip />
      <el-table-column label="操作" align="center" width="160" fixed="right">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/修改对话框 -->
    <el-dialog :title="title" v-model="open" width="680px" append-to-body>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="销售代表ID" prop="salesRepId">
              <el-input v-model="form.salesRepId" placeholder="请输入销售代表ID" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="日志日期" prop="logDate">
              <el-date-picker v-model="form.logDate" type="date" placeholder="请选择日志日期" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="拨打电话数" prop="callsMade">
              <el-input-number v-model="form.callsMade" :min="0" placeholder="请输入" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="有效通话" prop="effectiveCalls">
              <el-input-number v-model="form.effectiveCalls" :min="0" placeholder="请输入" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="新增意向" prop="newIntentions">
              <el-input-number v-model="form.newIntentions" :min="0" placeholder="请输入" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="意向客户" prop="intentionClients">
              <el-input-number v-model="form.intentionClients" :min="0" placeholder="请输入" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="面谈客户" prop="faceToFaceClients">
              <el-input-number v-model="form.faceToFaceClients" :min="0" placeholder="请输入" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="签约合同" prop="signedContracts">
              <el-input-number v-model="form.signedContracts" :min="0" placeholder="请输入" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="日志内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入工作日志内容" />
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
import { listWorkLog, getWorkLog, addWorkLog, updateWorkLog, delWorkLog, checkDuplicate } from "@/api/sales/workLog"

const { proxy } = getCurrentInstance()
const dataList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const title = ref("")
const open = ref(false)

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    salesRepId: undefined,
    logDate: undefined
  },
  rules: {
    salesRepId: [{ required: true, message: "销售代表ID不能为空", trigger: "blur" }],
    logDate: [{ required: true, message: "日志日期不能为空", trigger: "change" }],
    callsMade: [{ required: true, message: "拨打电话数不能为空", trigger: "blur" }]
  }
})
const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listWorkLog(queryParams.value).then(response => {
    dataList.value = response.data?.records || response.records || []
    total.value = response.data?.total || response.total || 0
    loading.value = false
  }).catch(() => { loading.value = false })
}

function cancel() { open.value = false; reset() }
function reset() {
  form.value = {
    id: undefined,
    salesRepId: undefined,
    logDate: undefined,
    callsMade: 0,
    effectiveCalls: 0,
    newIntentions: 0,
    intentionClients: 0,
    faceToFaceClients: 0,
    signedContracts: 0,
    content: undefined
  }
  proxy.resetForm("formRef")
}

function handleQuery() { queryParams.value.pageNum = 1; getList() }
function resetQuery() { proxy.resetForm("queryRef"); handleQuery() }
function handleAdd() { reset(); open.value = true; title.value = "新增工作日志" }

function handleUpdate(row) {
  reset()
  getWorkLog(row.id).then(response => {
    form.value = response.data || response
    open.value = true
    title.value = "修改工作日志"
  })
}

function submitForm() {
  proxy.$refs["formRef"].validate(valid => {
    if (!valid) return
    if (form.value.id) {
      // 修改
      updateWorkLog(form.value).then(() => {
        proxy.$modal.msgSuccess("修改成功")
        open.value = false
        getList()
      })
    } else {
      // 新增前检查重复
      checkDuplicate(form.value.salesRepId, form.value.logDate).then(res => {
        const isDuplicate = res.data !== undefined ? res.data : res
        if (isDuplicate) {
          proxy.$modal.msgError("该销售代表当天已存在工作日志，不能重复新增")
          return
        }
        addWorkLog(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      })
    }
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('是否确认删除该工作日志？').then(() => delWorkLog(row.id)).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

getList()
</script>

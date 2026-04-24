# 战区管理-负责人字段改造设计

**日期：** 2026-04-24
**状态：** 已批准

## 目标

将战区管理页面中的"负责人ID"改为"负责人"，实现：
1. 表格列动态显示负责人姓名（而非ID）
2. 修改弹窗中负责人字段改为下拉选择，显示 roleId=3 的用户

## 方案概述

**方案 A：仅在返回时 JOIN 查询用户表获取姓名**

- 后端修改 `listAll()` 和 `pageList()` 方法，JOIN 用户表查询 `real_name`
- 前端表格列改为显示 `directorName`，表单改为下拉选择

---

## 详细设计

### 1. 后端改动

#### 1.1 `SysZoneVo.java`
添加 `directorName` 字段：
```java
private String directorName;  // 负责人姓名
```

#### 1.2 `SysZoneServiceImpl.java`
修改 `listAll()` 和 `pageList()` 方法，JOIN 用户表查询负责人姓名

#### 1.3 `SysZoneDao.xml`
添加 resultMap JOIN 映射

### 2. 前端改动

#### 2.1 `zone/index.vue` 表格列
```html
<!-- 原文 -->
<el-table-column label="负责人ID" align="center" prop="directorId" width="100" />

<!-- 改为 -->
<el-table-column label="负责人" align="center" prop="directorName" width="100" />
```

#### 2.2 `zone/index.vue` 表单下拉
```html
<!-- 原文 -->
<el-form-item label="负责人ID" prop="directorId">
  <el-input v-model="form.directorId" placeholder="请输入负责人ID" />
</el-form-item>

<!-- 改为 -->
<el-form-item label="负责人" prop="directorId">
  <el-select v-model="form.directorId" placeholder="请选择负责人" style="width:100%">
    <el-option v-for="item in directorOptions" :key="item.id" :label="item.name" :value="item.id" />
  </el-select>
</el-form-item>
```

#### 2.3 `zone/index.vue` script
- 导入 `listUsersByRoleIds`
- 添加 `directorOptions` ref
- 添加 `loadDirectorOptions()` 函数
- 在 `handleAdd()` 和 `handleUpdate()` 中调用 `loadDirectorOptions()`

---

## 文件改动清单

| 文件 | 改动内容 |
|------|----------|
| `SysZoneVo.java` | 添加 `directorName` 字段 |
| `SysZoneServiceImpl.java` | JOIN 查询设置 directorName |
| `SysZoneDao.xml` | 添加 resultMap JOIN 映射 |
| `zone/index.vue` | 表格列改 `directorName`、表单改下拉、加载下拉选项 |
| `user.js` | 确认 `listUsersByRoleIds` API 存在 |

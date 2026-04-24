# 战区管理-负责人字段改造实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将战区管理表格的"负责人ID"改为"负责人"（显示姓名），修改弹窗中负责人字段改为下拉选择（roleId=3的用户）

**Architecture:** 后端采用两阶段查询（先查战区列表，再批量查用户姓名）返回包含 directorName 的数据；前端表格列改为 directorName，表单改为 el-select 下拉

**Tech Stack:** Spring Boot + MyBatis Plus + Vue3 + Element Plus

---

## 文件清单

| 文件 | 改动类型 |
|------|----------|
| `system/src/main/java/com/dafuweng/system/service/impl/SysZoneServiceImpl.java` | 修改 |
| `system/src/main/java/com/dafuweng/system/service/SysZoneService.java` | 修改 |
| `system/src/main/resources/system/mapper/SysZoneDao.xml` | 修改 |
| `ruoyi-ui/src/views/system/zone/index.vue` | 修改 |

---

## 实现步骤

### Task 1: 后端 - 修改 SysZoneService 返回 directorName

**Files:**
- Modify: `system/src/main/java/com/dafuweng/system/service/SysZoneService.java`
- Modify: `system/src/main/java/com/dafuweng/system/service/impl/SysZoneServiceImpl.java`

- [ ] **Step 1: 修改 Service 接口**

文件: `system/src/main/java/com/dafuweng/system/service/SysZoneService.java`

在接口末尾添加:
```java
/**
 * 批量设置战区的负责人姓名（通过 Feign 调用 auth 模块）
 */
void fillDirectorNames(List<SysZoneEntity> zones);
```

- [ ] **Step 2: 修改 Service 实现 - 添加 AuthFeignClient**

文件: `system/src/main/java/com/dafuweng/system/service/impl/SysZoneServiceImpl.java`

在类中添加:
```java
@Autowired
private com.dafuweng.system.feign.AuthFeignClient authFeignClient;
```

- [ ] **Step 3: 实现 fillDirectorNames 方法**

在 `SysZoneServiceImpl.java` 末尾添加:

```java
@Override
public void fillDirectorNames(List<SysZoneEntity> zones) {
    if (zones == null || zones.isEmpty()) {
        return;
    }
    // 收集所有 directorId
    List<Long> directorIds = zones.stream()
        .map(SysZoneEntity::getDirectorId)
        .filter(id -> id != null)
        .distinct()
        .collect(Collectors.toList());

    if (directorIds.isEmpty()) {
        return;
    }

    // 通过 Feign 调用 auth 模块查询用户姓名
    Result<Map<Long, String>> result = authFeignClient.listUserNamesByIds(directorIds);
    Map<Long, String> nameMap = result.getData();

    // 设置姓名到对应战区
    zones.forEach(zone -> {
        if (zone.getDirectorId() != null && nameMap != null) {
            zone.setDirectorName(nameMap.get(zone.getDirectorId()));
        }
    });
}
```

注意：需要在文件顶部 import `Result`:
```java
import com.dafuweng.common.entity.Result;
```

- [ ] **Step 4: 修改 pageList 和 listAll 方法调用 fillDirectorNames**

修改 `pageList()` 方法，在返回前调用 `fillDirectorNames`:

```java
@Override
public PageResponse<SysZoneEntity> pageList(PageRequest request) {
    IPage<SysZoneEntity> page = new Page<>(request.getPage(), request.getSize());
    LambdaQueryWrapper<SysZoneEntity> wrapper = new LambdaQueryWrapper<>();
    if (StringUtils.hasText(request.getSortField())) {
        if ("asc".equalsIgnoreCase(request.getSortOrder())) {
            wrapper.orderByAsc(SysZoneEntity::getId);
        } else {
            wrapper.orderByDesc(SysZoneEntity::getId);
        }
    } else {
        wrapper.orderByDesc(SysZoneEntity::getCreatedAt);
    }
    IPage<SysZoneEntity> result = sysZoneDao.selectPage(page, wrapper);
    // 填充负责人姓名
    fillDirectorNames(result.getRecords());
    return PageResponse.of(result.getTotal(), result.getRecords(),
        (int) page.getCurrent(), (int) page.getSize());
}
```

修改 `listAll()` 方法:

```java
@Override
public List<SysZoneEntity> listAll() {
    LambdaQueryWrapper<SysZoneEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.orderByAsc(SysZoneEntity::getSortOrder);
    List<SysZoneEntity> zones = sysZoneDao.selectList(wrapper);
    fillDirectorNames(zones);
    return zones;
}
```

修改 `listByStatus()` 方法:

```java
@Override
public List<SysZoneEntity> listByStatus(Short status) {
    LambdaQueryWrapper<SysZoneEntity> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(SysZoneEntity::getStatus, status);
    wrapper.orderByAsc(SysZoneEntity::getSortOrder);
    List<SysZoneEntity> zones = sysZoneDao.selectList(wrapper);
    fillDirectorNames(zones);
    return zones;
}
```

- [ ] **Step 5: 在 SysZoneEntity 添加 directorName 字段**

文件: `system/src/main/java/com/dafuweng/system/entity/SysZoneEntity.java`

添加字段:
```java
@TableField(exist = false)
private String directorName;
```

注意: 使用 MyBatis Plus 的 `@TableField(exist = false)` 标注，因为数据库表没有这个字段

- [ ] **Step 6: 提交代码**

```bash
git add system/src/main/java/com/dafuweng/system/entity/SysZoneEntity.java
git add system/src/main/java/com/dafuweng/system/service/SysZoneService.java
git add system/src/main/java/com/dafuweng/system/service/impl/SysZoneServiceImpl.java
git commit -m "feat(system): add directorName field and fillDirectorNames to SysZone"
```

---

### Task 2: 前端 - 修改 zone/index.vue

**Files:**
- Modify: `ruoyi-ui/src/views/system/zone/index.vue`

- [ ] **Step 1: 修改表格列**

找到第30行:
```html
<el-table-column label="负责人ID" align="center" prop="directorId" width="100" />
```

改为:
```html
<el-table-column label="负责人" align="center" prop="directorName" width="100" />
```

- [ ] **Step 2: 修改表单 - 负责人输入改为下拉选择**

找到第67-69行:
```html
<el-form-item label="负责人ID" prop="directorId">
  <el-input v-model="form.directorId" placeholder="请输入负责人ID" />
</el-form-item>
```

改为:
```html
<el-form-item label="负责人" prop="directorId">
  <el-select v-model="form.directorId" placeholder="请选择负责人" style="width:100%">
    <el-option v-for="item in directorOptions" :key="item.id" :label="item.name" :value="item.id" />
  </el-select>
</el-form-item>
```

- [ ] **Step 3: 添加 import 和下拉选项数据**

在 `<script setup>` 中，修改 import 行:
```javascript
import { listZone, getZone, addZone, updateZone, delZone } from "@/api/system/zone"
import { listUsersByRoleIds } from "@/api/system/user"
```

在 `const open = ref(false)` 后添加:
```javascript
const directorOptions = ref([])
```

- [ ] **Step 4: 添加 loadDirectorOptions 函数**

在 `function resetQuery()...` 之前添加:
```javascript
/** 加载负责人下拉选项（roleId=3） */
function loadDirectorOptions() {
  listUsersByRoleIds([3]).then(res => {
    directorOptions.value = (res.data || []).map(u => ({ id: u.id, name: u.realName }))
  })
}
```

- [ ] **Step 5: 修改 handleAdd 和 handleUpdate 调用 loadDirectorOptions**

修改 `handleAdd()`:
```javascript
function handleAdd() {
  reset()
  loadDirectorOptions()
  open.value = true
  title.value = "新增区域"
}
```

修改 `handleUpdate()`:
```javascript
function handleUpdate(row) {
  reset()
  getZone(row.id).then(response => {
    form.value = response.data || response
    loadDirectorOptions()
    open.value = true
    title.value = "修改区域"
  })
}
```

- [ ] **Step 6: 提交代码**

```bash
git add ruoyi-ui/src/views/system/zone/index.vue
git commit -m "feat(zone): change directorId to directorName in table and dropdown"
```

---

### Task 3: 验证

- [ ] **Step 1: 启动后端服务**

验证 `system` 和 `auth` 模块正常启动，无编译错误

- [ ] **Step 2: 启动前端**

验证 `ruoyi-ui` 能正常启动，无编译错误

- [ ] **Step 3: 手动测试**

1. 访问战区管理页面，验证表格列显示"负责人"而非"负责人ID"，且显示用户真实姓名
2. 点击新增，验证负责人是下拉选择框，且能正确列出 roleId=3 的用户
3. 点击修改，验证负责人是下拉选择框，且当前负责人被正确选中

---

## 注意事项

1. system 模块已有 `AuthFeignClient`，直接使用 `listUserNamesByIds` 方法获取用户姓名 Map
2. auth 模块需要确保 `/api/sysUser/names/by-ids` 接口存在（用于批量查询用户姓名）
3. 前端的 `listUsersByRoleIds` 接口已存在（user.js 第59-61行），可直接使用

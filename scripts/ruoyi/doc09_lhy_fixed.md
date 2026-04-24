# 前端页面修复记录 (doc09_lhy_fixed)

> 修复日期：2026-04-23
> 修复范围：2.1.1 dept/index.vue (部门管理)
> 修复依据：doc09_lhy.md 审查报告

---

## 一、修复概述

### 1.1 原始问题

| 序号 | 问题 | 代码位置 | 严重程度 |
|------|------|----------|----------|
| 1 | 表格显示纯数字ID (parentId, zoneId, managerId) | 第30-32行 | 🔴 高 |
| 2 | 表单字段使用 el-input 而非 el-select | 第69-82行 | 🔴 高 |
| 3 | 表单验证缺失 (zoneId, managerId) | 第125-128行 | 🟠 中 |
| 4 | 无部门树形展示 | 第26-48行 | 🟠 中 |

### 1.2 修复方案

| 问题 | 修复方案 | 是否需要后端配合 |
|------|----------|------------------|
| 表格显示ID | 后端返回关联名称字段，前端显示 | ✅ 需要 |
| 表单用 el-select | 前端改为下拉选择，加载选项数据 | ❌ 不需要 |
| 表单验证 | 前端添加验证规则 | ❌ 不需要 |
| 树形展示 | 暂不实现，保持表格展示 | - |

---

## 二、后端修改

### 2.1 新增 SysDepartmentVO

**文件**：`system/src/main/java/com/dafuweng/system/entity/SysDepartmentVO.java`

```java
package com.dafuweng.system.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SysDepartmentVO extends SysDepartmentEntity {

    private String parentName;    // 上级部门名称
    private String zoneName;      // 所属区域名称
    private String managerName;    // 负责人姓名
}
```

### 2.2 新增 Feign 客户端

**文件**：`system/src/main/java/com/dafuweng/system/feign/AuthUserFeignClient.java`

```java
package com.dafuweng.system.feign;

import com.dafuweng.auth.entity.SysUserEntity;
import com.dafuweng.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth", url = "${feign.auth.url:http://localhost:8085}")
public interface AuthUserFeignClient {

    @GetMapping("/api/sysUser/{id}")
    Result<SysUserEntity> getUserById(@PathVariable("id") Long id);

    @GetMapping("/api/sysUser/sales-reps")
    Result<List<SysUserEntity>> listSalesReps();
}
```

### 2.3 修改 Service 接口

**文件**：`system/src/main/java/com/dafuweng/system/service/SysDepartmentService.java`

- `pageList()` 返回类型改为 `PageResponse<SysDepartmentVO>`
- 新增 `listAll()` 方法返回所有部门列表

### 2.4 修改 Service 实现

**文件**：`system/src/main/java/com/dafuweng/system/service/impl/SysDepartmentServiceImpl.java`

核心修改 - `pageList()` 方法：

```java
@Override
public PageResponse<SysDepartmentVO> pageList(PageRequest request) {
    // ... 原有分页查询逻辑 ...

    // 转换为 VO 并填充关联名称
    List<SysDepartmentVO> voList = fillRelatedNames(result.getRecords());
    return PageResponse.of(result.getTotal(), voList, (int) page.getCurrent(), (int) page.getSize());
}

/**
 * 填充关联名称
 */
private List<SysDepartmentVO> fillRelatedNames(List<SysDepartmentEntity> records) {
    if (records == null || records.isEmpty()) {
        return Collections.emptyList();
    }

    // 1. 收集所有需要查询的 ID
    Set<Long> parentIds = records.stream()...
    Set<Long> zoneIds = records.stream()...
    Set<Long> managerIds = records.stream()...

    // 2. 批量查询上级部门名称（通过 DepartmentDao）
    Map<Long, String> parentNameMap = ...;

    // 3. 批量查询区域名称（通过 ZoneDao）
    Map<Long, String> zoneNameMap = ...;

    // 4. 批量查询用户名称（通过 Feign 调用 auth 服务）
    Map<Long, String> managerNameMap = ...;

    // 5. 填充名称到 VO
    return records.stream().map(entity -> {
        SysDepartmentVO vo = new SysDepartmentVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setParentName(parentNameMap.get(entity.getParentId()));
        vo.setZoneName(zoneNameMap.get(entity.getZoneId()));
        vo.setManagerName(managerNameMap.get(entity.getManagerId()));
        return vo;
    }).collect(Collectors.toList());
}
```

### 2.5 修改 Controller

**文件**：`system/src/main/java/com/dafuweng/system/controller/SysDepartmentController.java`

- `/page` 返回类型改为 `PageResponse<SysDepartmentVO>`
- 新增 `/listAll` 接口

### 2.6 配置更新

**文件**：`system/src/main/resources/application.yml`

```yaml
# Feign 客户端配置
feign:
  auth:
    url: http://localhost:8085
```

### 2.7 添加 OpenFeign 依赖

**文件**：`system/pom.xml`

```xml
<!-- open feign -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

### 2.8 启用 Feign 客户端

**文件**：`system/src/main/java/com/dafuweng/system/SystemApplication.java`

```java
@SpringBootApplication(scanBasePackages = "com.dafuweng")
@MapperScan("com.dafuweng.system.dao")
@EnableCaching
@EnableFeignClients(basePackages = "com.dafuweng.system.feign")
public class SystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class, args);
    }
}
```

---

## 三、前端修改

### 3.1 API 层修改

**文件**：`ruoyi-ui/src/api/system/department.js`

新增接口：
```javascript
// 获取所有部门列表（用于下拉选择）
export function listAllDepartment() {
  return request({
    url: '/sysDepartment/listAll',
    method: 'get'
  })
}
```

### 3.2 表格列修改

**文件**：`ruoyi-ui/src/views/system/dept/index.vue`

```vue
<!-- 修改前：显示纯数字ID -->
<el-table-column label="上级ID" align="center" prop="parentId" width="90" />
<el-table-column label="区域ID" align="center" prop="zoneId" width="90" />
<el-table-column label="负责人ID" align="center" prop="managerId" width="100" />

<!-- 修改后：显示关联名称 -->
<el-table-column label="上级部门" align="center" prop="parentName" width="120">
  <template #default="scope">
    {{ scope.row.parentName || '-' }}
  </template>
</el-table-column>
<el-table-column label="所属区域" align="center" prop="zoneName" width="120">
  <template #default="scope">
    {{ scope.row.zoneName || '-' }}
  </template>
</el-table-column>
<el-table-column label="负责人" align="center" prop="managerName" width="100">
  <template #default="scope">
    {{ scope.row.managerName || '-' }}
  </template>
</el-table-column>
```

### 3.3 表单改为 el-select

```vue
<!-- 修改前 -->
<el-form-item label="上级部门ID" prop="parentId">
  <el-input v-model="form.parentId" placeholder="请输入上级部门ID" />
</el-form-item>

<!-- 修改后 -->
<el-form-item label="上级部门" prop="parentId">
  <el-select v-model="form.parentId" placeholder="请选择上级部门" clearable filterable style="width: 100%">
    <el-option v-for="dept in deptOptions" :key="dept.id" :label="dept.deptName" :value="dept.id" />
  </el-select>
</el-form-item>
```

新增下拉选项加载逻辑：
```javascript
function loadOptions() {
  // 加载部门列表
  listAllDepartment().then(response => {
    deptOptions.value = response.data || []
  })
  // 加载区域列表
  listAllZone().then(response => {
    zoneOptions.value = response.data || []
  })
  // 加载用户列表
  listUser({ pageNum: 1, pageSize: 1000 }).then(response => {
    userOptions.value = response.data?.records || response.records || []
  })
}
```

### 3.4 添加表单验证

```javascript
rules: {
  deptCode: [{ required: true, message: "部门编码不能为空", trigger: "blur" }],
  deptName: [{ required: true, message: "部门名称不能为空", trigger: "blur" }],
  zoneId: [{ required: true, message: "请选择所属区域", trigger: "change" }],
  managerId: [{ required: true, message: "请选择负责人", trigger: "change" }]
}
```

---

## 四、部署说明

### 4.1 后端部署步骤

1. **重新编译 common 模块**（如果修改了 common）
   ```bash
   cd /Users/liuhongyu/IdeaProjects/dafuweng
   mvn clean install -pl common -am
   ```

2. **重新编译 system 模块**
   ```bash
   mvn clean package -pl system -am
   ```

3. **重启 system 服务**
   ```bash
   # 如果使用 Docker
   docker-compose restart neocc-system
   
   # 如果直接运行
   java -jar system/target/system-1.0-SNAPSHOT.jar
   ```

4. **验证接口**
   ```bash
   curl http://localhost:8082/api/sysDepartment/page
   ```

### 4.2 前端部署

```bash
cd /Users/liuhongyu/IdeaProjects/dafuweng/ruoyi-ui
npm run dev
```

### 4.3 注意事项

- **Feign 客户端依赖**：确保 `system` 模块 pom.xml 中已引入 `spring-cloud-starter-openfeign`
- **auth 服务必须运行**：managerName 通过 Feign 调用 auth 服务获取
- **Nacos 注册中心**：确保 auth 服务已注册到 Nacos

---

## 五、验证清单

| 序号 | 验证项 | 预期结果 | 通过 |
|------|--------|----------|------|
| 1 | 部门列表表格显示上级部门名称 | parentName 列显示部门名称而非ID | ⬜ |
| 2 | 部门列表表格显示区域名称 | zoneName 列显示区域名称而非ID | ⬜ |
| 3 | 部门列表表格显示负责人姓名 | managerName 列显示用户真实姓名 | ⬜ |
| 4 | 新增表单上级部门是下拉选择 | el-select 显示部门列表 | ⬜ |
| 5 | 新增表单区域是下拉选择 | el-select 显示区域列表 | ⬜ |
| 6 | 新增表单负责人是下拉选择 | el-select 显示用户列表 | ⬜ |
| 7 | 区域不选时表单验证提示 | 提示"请选择所属区域" | ⬜ |
| 8 | 负责人不选时表单验证提示 | 提示"请选择负责人" | ⬜ |
| 9 | 新增/修改表单提交成功 | 后端返回成功 | ⬜ |

---

## 六、其他页面修复计划

根据 doc09_lhy.md 报告，以下页面也需要类似修复：

| 序号 | 页面 | 主要问题 | 建议优先级 |
|------|------|----------|-----------|
| 1 | user/index.vue | 部门使用 el-input | 🟠 P1 |
| 2 | contract/index.vue | 所有字段用 el-input | 🔴 P0 |
| 3 | zone/index.vue | 负责人使用 el-input | 🟠 P1 |
| 4 | product/index.vue | 银行ID用 el-input | 🔴 P0 |
| 5 | service-fee/index.vue | 合同ID用 el-input | 🔴 P0 |

---

## 七、相关文件清单

### 后端修改
- ✅ `system/src/main/java/com/dafuweng/system/entity/SysDepartmentVO.java` (新增)
- ✅ `system/src/main/java/com/dafuweng/system/feign/AuthUserFeignClient.java` (新增)
- ✅ `system/src/main/java/com/dafuweng/system/service/SysDepartmentService.java` (修改)
- ✅ `system/src/main/java/com/dafuweng/system/service/impl/SysDepartmentServiceImpl.java` (修改)
- ✅ `system/src/main/java/com/dafuweng/system/controller/SysDepartmentController.java` (修改)
- ✅ `system/src/main/resources/application.yml` (修改)
- ✅ `system/pom.xml` (修改 - 添加 OpenFeign 依赖)
- ✅ `system/src/main/java/com/dafuweng/system/SystemApplication.java` (修改 - 添加 @EnableFeignClients)

### 前端修改
- ✅ `ruoyi-ui/src/api/system/department.js` (修改)
- ✅ `ruoyi-ui/src/views/system/dept/index.vue` (修改)

---

> 本文档由 AI 代码审查工具自动生成
> 生成时间：2026-04-23

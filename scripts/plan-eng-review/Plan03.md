# Plan03: common 模块重构 — 消除 framework 重复

**版本：** v1.0
**日期：** 2026-04-02
**状态：** 待实施
**目标：** 消除 `common` 与 `framework` 的功能重复，将所有共享类统一到 `common` 模块

---

## 一、现状分析

### 1.1 当前模块结构

```
NeoCC (根, <packaging>pom</packaging>)
├── common (<packaging>pom</packaging>) ← 依赖版本管理中心
│   └── src/main/java/com/dafuweng/common/  ← 少量 Java 类（与 framework 重复）
│       ├── entity/Result.java         ⚠️ 缺 error500()，被 GlobalExceptionHandler 引用
│       ├── entity/PageRequest.java
│       ├── entity/PageResponse.java
│       └── exception/GlobalExceptionHandler.java  ⚠️ 缺 error500()，NPE 返回 error400 而非 error500
├── framework (<packaging>jar</packaging>) ← 共享类 jar（用户要求消除）
│   └── src/main/java/com/dafuweng/framework/
│       ├── entity/Result.java         ✅ 完整（含 error500）
│       ├── entity/PageRequest.java
│       ├── entity/PageResponse.java
│       └── exception/GlobalExceptionHandler.java  ✅ 完整（引用 error500）
├── sales (<packaging>jar</packaging>)
│   └── parent=common, dep=framework  ← 当前依赖路径
├── finance (<packaging>jar</packaging>)
│   └── parent=common, dep=framework
└── system (<packaging>jar</packaging>)
    └── parent=common, dep=framework
```

### 1.2 核心矛盾

**Maven 的硬约束：**
- `<packaging>pom</packaging>` 的模块 → 不能作为 `<dependency>` 引用，只能作为 `<parent>` 继承
- `<packaging>jar</packaging>` 的模块 → 不能作为 `<parent>`，但可以作为 `<dependency>` 被引用

**Phase 1 实施后的实际引用关系：**
- `sales/finance/system` 的 `pom.xml` 中：`<parent>common (pom)</parent>` + `<dep>framework (jar)</dep>`
- 所有 Java 代码的 import：`com.dafuweng.framework.entity.Result`（framework 包）

### 1.3 矛盾根源

| 问题 | 描述 |
|------|------|
| `common` 定位不清 | 原设计为"依赖版本管理中心（pom）"，但 Check02 指出现有 `common/src/main/java` 下已有部分共享类，与 framework 重复 |
| 两套包名并存 | `com.dafuweng.common.*`（common 模块）和 `com.dafuweng.framework.*`（framework 模块）并存，实际代码只用 framework 包 |
| Java 类不完整 | common 的 Result.java 缺 `error500()`，GlobalExceptionHandler.java 的 NPE 也返回 `error400`（有 bug） |

---

## 二、消除方案

### 2.1 方案选择

| 方案 | 描述 | 可行性 |
|------|------|--------|
| **方案 A（推荐）：重构 parent 关系** | 将 `sales/finance/system` 的 parent 从 `common` 改为 `spring-boot-starter-parent`，将 `common` 改为 `<packaging>jar</packaging>` 作为共享类 jar | ✅ 可行 |
| 方案 B：保留两层结构但重命名 | 将 `framework` 改名为 `common-jar`，将现有 `common` 改名为 `common-dep` | ❌ 改名为根本性重构，风险大 |
| 方案 C：拆分 common | 拆分 `common` 为 `common-dep`（纯 dep）+ `common-jar`（共享类） | ❌ 仍然存在两个 common 相关模块 |

**推荐方案 A** 的核心思路：
- `common` 转换为 `<packaging>jar</packaging>`，承载所有共享 Java 类
- `sales/finance/system` 不再继承 `common`，改为直接继承 `spring-boot-starter-parent`
- `common` 仍通过 `<dependencyManagement>` 提供版本控制（通过直接声明管理依赖版本）
- 删除 `framework` 模块
- 所有业务模块依赖 `common` jar

### 2.2 方案 A 详细设计

```
NeoCC (根, <packaging>pom</packaging>)
├── common (<packaging>jar</packaging>) ✅ 合并后：版本管理 + 共享类
│   ├── <dependencyManagement> — 版本控制（mybatis-plus, mysql-connector-j）
│   └── src/main/java/com/dafuweng/common/
│       ├── entity/Result.java         ✅（完整版，含 error500）
│       ├── entity/PageRequest.java
│       ├── entity/PageResponse.java
│       └── exception/GlobalExceptionHandler.java  ✅（完整版）
├── sales (<packaging>jar</packaging>)
│   ├── parent=spring-boot-starter-parent
│   └── dep=common
├── finance (<packaging>jar</packaging>)
│   ├── parent=spring-boot-starter-parent
│   └── dep=common
└── system (<packaging>jar</packaging>)
    ├── parent=spring-boot-starter-parent
    └── dep=common

删除: framework/ 模块
```

---

## 三、依赖关系重构（关键）

### 3.1 Maven 继承 vs 依赖的区分

```
继承（parent）：传递 <dependencyManagement> 里的版本控制
             传递 <properties>
             不能传递实际依赖（dependencies 块）

依赖（dependency）：直接引用 jar，包含 class 文件
```

**关键规则：**
- 子模块的 `<parent>spring-boot-starter-parent</parent>` → 自动获得 Spring Boot 所有依赖的版本管理
- 子模块直接 `<dep>common</dep>` → common.jar 被加入 classpath，共享类可用
- common 的 `<dependencyManagement>` → 可控地提供 MyBatis-Plus 等第三方库的版本

### 3.2 common 的新 pom.xml 结构

```xml
<project>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
    </parent>

    <groupId>com.dafuweng</groupId>
    <artifactId>common</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>  <!-- 关键变更：pom → jar -->

    <properties>
        <java.version>21</java.version>
        <mybatis-plus.version>3.5.7</mybatis-plus.version>
        <mysql.version>8.3.0</mysql.version>
    </properties>

    <!-- 版本控制：供直接依赖 common 的模块使用 -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>
            <dependency>
                <groupId>com.mysql</groupId>
                <artifactId>mysql-connector-j</artifactId>
                <version>${mysql.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <!-- 实际依赖：common 自身需要这些来编译 GlobalExceptionHandler 等 -->
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

### 3.3 业务模块的新 pom.xml 模板（sales 为例）

```xml
<project>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
    </parent>

    <artifactId>sales</artifactId>
    <groupId>com.dafuweng</groupId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <!-- 1. Spring Boot Web（parent 提供，无需声明版本）-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- 2. MyBatis-Plus（版本来自 common 的 dependencyManagement） -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        </dependency>

        <!-- 3. MySQL 驱动 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- 4. 内部共享类：common jar -->
        <dependency>
            <groupId>com.dafuweng</groupId>
            <artifactId>common</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- 5. DevTools -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 四、Java 类重构

### 4.1 common 的 Java 类合并策略

**目标：** 消除 `com.dafuweng.common.*` 和 `com.dafuweng.framework.*` 两套重复类，统一到 `com.dafuweng.common.*`

**策略：**
1. 删除 `common/src/main/java/com/dafuweng/common/entity/` 和 `exception/` 下所有旧文件（它们是 Phase 1 实施过程中遗留的、不完整的版本）
2. 将 `framework/src/main/java/com/dafuweng/framework/` 下的所有文件迁移到 `common/src/main/java/com/dafuweng/common/`
3. 将所有 `package com.dafuweng.framework.*` 声明改为 `package com.dafuweng.common.*`
4. 将所有 import 中的 `com.dafuweng.framework.*` 引用改为 `com.dafuweng.common.*`

### 4.2 需要迁移的 Java 文件（4个）

| 源路径 | 目标路径 | 变更 |
|--------|---------|------|
| `framework/src/main/java/com/dafuweng/framework/entity/Result.java` | `common/src/main/java/com/dafuweng/common/entity/Result.java` | `package` 改 `common` |
| `framework/src/main/java/com/dafuweng/framework/entity/PageRequest.java` | `common/src/main/java/com/dafuweng/common/entity/PageRequest.java` | `package` 改 `common` |
| `framework/src/main/java/com/dafuweng/framework/entity/PageResponse.java` | `common/src/main/java/com/dafuweng/common/entity/PageResponse.java` | `package` 改 `common` |
| `framework/src/main/java/com/dafuweng/framework/exception/GlobalExceptionHandler.java` | `common/src/main/java/com/dafuweng/common/exception/GlobalExceptionHandler.java` | `package` 改 `common`，import 同步改 |

**迁移后 Result.java 完整内容：**
```java
package com.dafuweng.common.entity;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) { ... }
    public static <T> Result<T> success() { ... }
    public static <T> Result<T> error(String message) { ... }
    public static <T> Result<T> error(Integer code, String message) { ... }
    public static <T> Result<T> error400(String message) { ... }
    public static <T> Result<T> error401(String message) { ... }
    public static <T> Result<T> error403(String message) { ... }
    public static <T> Result<T> error500(String message) { ... }  // 注意：这个方法原来在 framework 里才有
}
```

### 4.3 需要更新 import 的业务代码

所有业务模块（`sales`、`finance`、`system`）中以下 import 需要全局替换：

```
com.dafuweng.framework.entity.Result
  → com.dafuweng.common.entity.Result
com.dafuweng.framework.entity.PageRequest
  → com.dafuweng.common.entity.PageRequest
com.dafuweng.framework.entity.PageResponse
  → com.dafuweng.common.entity.PageResponse
com.dafuweng.framework.exception.GlobalExceptionHandler
  → com.dafuweng.common.exception.GlobalExceptionHandler
```

**涉及文件数：** 约 54 个 Java 文件（全部 Controller + Service + ServiceImpl）

---

## 五、实施步骤

### Step 1：更新根 pom.xml

**文件：** `pom.xml`

**变更：**
1. `<modules>` 中移除 `framework`
2. 根 pom 保留为 `<packaging>pom</packaging>`，作为全局聚合器

```xml
<modules>
    <module>common</module>
    <module>sales</module>
    <module>finance</module>
    <module>system</module>
</modules>
<!-- 删除 <dependencies> stray block（Plan02 违规） -->
```

### Step 2：重写 common/pom.xml

**文件：** `common/pom.xml`

**变更：**
1. `<packaging>pom</packaging>` → `<packaging>jar</packaging>`
2. parent 改为 `spring-boot-starter-parent`
3. 添加 `<dependencyManagement>` 提供 mybatis-plus 和 mysql-connector-j 版本
4. 添加 `<dependencies>` 块（spring-boot-starter-web、mybatis-plus-spring-boot3-starter、lombok）
5. 删除原有的 `<build>` 中的 `spring-boot-maven-plugin`（common 作为 lib 不需要 fat jar）

### Step 3：迁移 Java 类文件

**操作：**
1. 删除 `common/src/main/java/com/dafuweng/common/` 下所有旧文件（不完整的版本）
2. 创建目录 `common/src/main/java/com/dafuweng/common/entity/` 和 `common/src/main/java/com/dafuweng/common/exception/`
3. 迁移 framework 的 4 个 Java 文件，逐一修改 package 声明

**注意：** framework 中的 Result.java（`com.dafuweng.framework.entity.Result`）是完整版（包含 `error500`），必须以此为准。

### Step 4：更新所有业务模块 pom.xml

**文件：** `sales/pom.xml`、`finance/pom.xml`、`system/pom.xml`

**变更（三个模块一致）：**
1. `<parent>` 从 `common` 改为 `spring-boot-starter-parent`
2. 删除 `<relativePath>../common/pom.xml</relativePath>`
3. 将 `framework` 依赖改为 `common` 依赖

### Step 5：全局替换 import 路径

**操作：** 在所有 `.java` 文件中将 `com.dafuweng.framework` 替换为 `com.dafuweng.common`

**预计影响文件数：** ~54 个（sales: 21 service/controller，finance: 12，system: 9，加上 ServiceImpl 的 18 个）

### Step 6：删除 framework 模块

**操作：**
1. 删除 `framework/` 整个目录
2. 从根 pom `<modules>` 中确认已移除 `framework`

### Step 7：编译验证

```bash
mvn clean compile -DskipTests
```

预期结果：所有模块编译通过，`common.jar` 被正确引用。

---

## 六、文件变更清单

| 操作 | 文件路径 | 说明 |
|------|---------|------|
| 修改 | `pom.xml` | 移除 framework 模块，删除 stray dependencies |
| 重写 | `common/pom.xml` | pom→jar，parent 改为 spring-boot-starter-parent |
| 新建 | `common/src/main/java/com/dafuweng/common/entity/Result.java` | 完整版（含 error500） |
| 新建 | `common/src/main/java/com/dafuweng/common/entity/PageRequest.java` | 迁移自 framework |
| 新建 | `common/src/main/java/com/dafuweng/common/entity/PageResponse.java` | 迁移自 framework |
| 新建 | `common/src/main/java/com/dafuweng/common/exception/GlobalExceptionHandler.java` | 迁移自 framework |
| 删除 | `common/src/main/java/com/dafuweng/common/entity/Result.java` | 旧文件（不完整） |
| 删除 | `common/src/main/java/com/dafuweng/common/entity/PageRequest.java` | 旧文件 |
| 删除 | `common/src/main/java/com/dafuweng/common/entity/PageResponse.java` | 旧文件 |
| 删除 | `common/src/main/java/com/dafuweng/common/exception/GlobalExceptionHandler.java` | 旧文件 |
| 修改 | `sales/pom.xml` | parent 改 spring-boot-starter-parent，dep common |
| 修改 | `finance/pom.xml` | 同上 |
| 修改 | `system/pom.xml` | 同上 |
| 修改 | `sales/src/main/java/com/dafuweng/sales/**/*.java` | ~21 文件 import 路径替换 |
| 修改 | `finance/src/main/java/com/dafuweng/finance/**/*.java` | ~12 文件 import 路径替换 |
| 修改 | `system/src/main/java/com/dafuweng/system/**/*.java` | ~9 文件 import 路径替换 |
| 删除 | `framework/` | 整个目录删除 |

**预计 net 文件变更：**
- 新增：4 个（迁移后的 common Java 类）
- 删除：4 个（旧 common Java 类）+ framework 整个目录
- 修改：3 个 pom.xml + ~54 个 Java 文件
- 总计约 61 个文件变更

---

## 七、关键风险与注意事项

### 7.1 Maven 多继承问题（不存在）

**误解：** 业务模块不能同时继承 spring-boot-starter-parent 和 common

**澄清：** 这不是"多继承"。业务模块的 `<parent>` 是 `spring-boot-starter-parent`（提供依赖版本），`<dep>common</dep>` 是常规依赖（提供编译期 class）。两者职责不同，不冲突。

### 7.2 common 中 spring-boot-starter-web 的传递性

**风险：** common 引入 `spring-boot-starter-web` 是否会传递到业务模块导致冲突？

**结论：** 不会。`spring-boot-starter-parent` 本身是通过 `<parent>` 继承获得版本的，不占用依赖声明。common 中的 `spring-boot-starter-web` 依赖只是 common 模块自身的编译依赖，不影响业务模块的依赖树。

### 7.3 根 pom 的 stray `<dependencies>` 块

**现状：** 根 pom.xml 有 5 个 `<dependency>` 直接声明（违反 Plan02）

**建议：** 本次 Plan03 实施时一并清理（移到 common 的 `<dependencyManagement>` 或直接删除，因为 spring-boot-starter-parent 已覆盖）

### 7.4 编译验证必须覆盖所有模块

```bash
mvn clean compile -pl common,sales,finance,system -DskipTests
```

必须四个模块全部通过才能视为实施完成。

---

## 八、验收标准

Plan03 实施完成后，以下条件必须满足：

1. `mvn clean compile -pl common,sales,finance,system -DskipTests` 全部通过
2. 根 pom `<modules>` 中不再包含 `framework`
3. `common` 的 `<packaging>` 为 `jar`（不再是 `pom`）
4. `sales/finance/system` 的 import 语句全部为 `com.dafuweng.common.*`
5. `Result.java` 包含 `error500()` 方法
6. `GlobalExceptionHandler` 的 NullPointerException handler 返回 `Result.error500()`
7. `framework/` 目录已删除
8. `mvn clean compile -DskipTests` 全项目通过

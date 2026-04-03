# Spring Boot 引入方案 — sales 模块

## 1. 当前状态

```
NeoCC (root pom)
├── common/        (暂无依赖，仅 parent 引用)
└── sales/         (依赖 common，但无 Spring Boot / MyBatis-Plus 依赖)
    ├── entity/     7 个 MyBatis-Plus Entity (带 @TableName/@TableId/@TableLogic)
    ├── dao/        7 个 MyBatis-Plus BaseMapper 接口
    └── resources/sales/mapper/  7 个 MyBatis XML Mapper
```

Java 版本: **21**，Maven 多模块结构。

---

## 2. 目标

在 `sales` 模块引入 Spring Boot 3.x，使 MyBatis-Plus Entity / BaseMapper / XML Mapper 全部正常工作，并整理好依赖层次关系。

---

## 3. 架构决策

### 3.1 Spring Boot 版本

| 版本 | Java 要求 | MyBatis-Plus 支持 | 推荐 |
|------|-----------|-------------------|------|
| Spring Boot 2.7.x | Java 8-17 | 支持 (MP 3.5.x) | 不选 — Java 21 浪费 |
| **Spring Boot 3.2.x** | Java 17+ | 支持 (MP 3.5.x+) | **选这个** |

**推荐 Spring Boot 3.2.x + MyBatis-Plus 3.5.7**，与 Java 21 匹配。

### 3.2 依赖管理策略

两种方案：

**方案 A — 继承 spring-boot-starter-parent（推荐)**

```xml
<!-- root pom.xml -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.5</version>
</parent>
```

**优点**: 版本统一管理，无需手动声明每个依赖的版本。
**缺点**: `dependencyManagement` 会被 spring-boot-starter-parent 锁定版本，若有特殊版本需求需要覆盖。

**方案 B — 仅用 `dependencyManagement` 管理版本**

保留 `groupId:artifactId:version` 的自由声明，用 `dependencyManagement` 统一版本表。

**推荐方案 A**，原因：项目初期不需要细粒度版本控制，`spring-boot-starter-parent` 大幅减少 `pom.xml` 噪音。

### 3.3 common 模块定位

目前 `common` 模块仅作为空壳存在。两种处理方式：

| 方案 | 描述 | 适用场景 |
|------|------|----------|
| **A — 纯依赖中介** | common 仅作为 `<dependency>` 被 sales 引用，不承载代码 | common 未来会有通用代码 |
| **B — 共享模块** | common 放置跨模块共享的 Entity/Mapper/Util | 多模块共用同一套数据模型 |

**推荐方案 B 的变体**：common 预留为共享模块，但当前阶段不移动 sales 下的 entity/dao，保持 sales 模块自包含，未来有共享需求再拆分。

### 3.4 MyBatis-Plus 集成方式

**核心依赖**（在 sales/pom.xml 中声明）：

```xml
<dependencies>
    <!-- 1. Spring Boot Starter（自动配置）-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>

    <!-- 2. MyBatis-Plus Spring Boot Starter -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        <version>3.5.7</version>
    </dependency>

    <!-- 3. MySQL 驱动（根据实际数据库替换）-->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- 4. Lombok（可选，Entity 已使用 @Data）-->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- 5. 已有依赖：common -->
    <dependency>
        <groupId>com.dafuweng</groupId>
        <artifactId>common</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

**不需要额外引入** `mybatis-plus-core` 或 `mybatis`，`mybatis-plus-spring-boot3-starter` 已包含全部传递依赖。

### 3.5 依赖层次图

```
root pom.xml
└── spring-boot-starter-parent:3.2.5  (版本管理 + 插件管理)
    ├── common/pom.xml
    │   └── (空壳，未来放共享代码)
    └── sales/pom.xml
        ├── common  (compile scope)
        ├── spring-boot-starter
        ├── mybatis-plus-spring-boot3-starter:3.5.7
        └── mysql-connector-j (runtime)
```

---

## 4. 实施步骤

### Step 1 — 修改 root pom.xml

在 `pom.xml` 中添加 `spring-boot-starter-parent` 作为 parent，并迁移 properties 到 `dependencyManagement` 体系。

**文件**: `pom.xml`

主要变更：
- 将 `<parent>` 替换为空壳 `project` 的 parent 声明，改为继承 `spring-boot-starter-parent`
- 将 properties 中的 Java 版本号改为 `21` 并通过 `spring-boot` 的属性覆盖机制管理
- 保留 `<modules>` 结构

> 注意：`<packaging>pom</packaging>` 的 module 项目不能直接继承 `spring-boot-starter-parent`，需要调整结构。有两种处理方式：
> 1. **保留 root pom 为 `pom` 类型**，在 sales 模块中单独继承 spring-boot-starter-parent
> 2. **将 root pom 改为 jar 类型**，sales 通过 `<relativePath>` 引用 parent
>
> **推荐方式 1**（见 Step 2），因为保持 root pom 的 `pom` 类型符合多模块规范。

### Step 2 — sales 模块独立继承 Spring Boot Parent

**文件**: `sales/pom.xml`

sales 模块将 `spring-boot-starter-parent` 作为 parent，而 root pom 仍为 `pom` 类型（这是 Maven 多模块的常见合法模式）。

```xml
<project>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>

    <groupId>com.dafuweng</groupId>
    <artifactId>sales</artifactId>
    <version>1.0-SNAPSHOT</version>

    <!-- Spring Boot 3.x 要求 Java 17+ -->
    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <!-- MyBatis-Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>3.5.7</version>
        </dependency>

        <!-- MySQL Driver -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- 内部依赖 -->
        <dependency>
            <groupId>com.dafuweng</groupId>
            <artifactId>common</artifactId>
            <version>1.0-SNAPSHOT</version>
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

**说明**: `relativePath` 为空表示从 Maven 中央仓库查找 parent pom，不从本地层级向上查找。

### Step 3 — 创建 Spring Boot Application 启动类

**文件**: `sales/src/main/java/com/dafuweng/sales/SalesApplication.java`

```java
package com.dafuweng.sales;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.dafuweng.sales.dao")   // 扫描 BaseMapper 子接口
public class SalesApplication {
    public static void main(String[] args) {
        SpringApplication.run(SalesApplication.class, args);
    }
}
```

**关键点**:
- `@MapperScan("com.dafuweng.sales.dao")` 让 Spring 自动注册所有 `BaseMapper` 子接口为 MyBatis Mapper
- 传统 XML Mapper（`resources/sales/mapper/*.xml`）需要额外配置路径

### Step 4 — 创建 application.yml

**文件**: `sales/src/main/resources/application.yml`

```yaml
spring:
  application:
    name: sales
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/neodb?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD:}

mybatis-plus:
  mapper-locations: classpath:sales/mapper/*.xml   # XML Mapper 路径
  type-aliases-package: com.dafuweng.sales.entity  # Entity 包扫描
  configuration:
    map-underscore-to-camel-case: true             # 下划线→驼峰自动映射
    log-impl: org.apache.ibatis.logging.stdout.StdOut  # 开发期打印 SQL
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

### Step 5 — 处理 XML Mapper 路径兼容性问题

现有的 XML Mapper 使用 MyBatis 原生格式（`namespace="com.dafuweng.sales.dao.CustomerDao"`），且放在 `resources/sales/mapper/` 下。

`mybatis-plus-spring-boot3-starter` 可自动加载这些 XML，但需要确保：

1. XML 文件的 `<mapper namespace>` 与 DAO 接口全限定名一致（已满足）
2. `mybatis-plus.mapper-locations` 指向正确路径（已在 Step 4 配置）
3. DAO 接口不要用 `@Mapper` 注解（已有但不冲突，`@MapperScan` 会处理）

### Step 6 — common 模块调整（可选）

如果 common 未来会有共享代码（如通用 Entity 基类、工具类），当前 `common/pom.xml` 无需任何变更。保持空壳是可接受的，直到有实际共享内容再填充。

---

## 5. 关键配置汇总

| 配置项 | 值 | 说明 |
|--------|-----|------|
| Spring Boot | 3.2.5 | Java 21 兼容 |
| MyBatis-Plus | 3.5.7 | Spring Boot 3 支持 |
| Java 版本 | 21 | 与现有配置一致 |
| `@MapperScan` | `com.dafuweng.sales.dao` | 扫描所有 BaseMapper 子接口 |
| XML Mapper 路径 | `classpath:sales/mapper/*.xml` | 匹配现有 XML 文件位置 |
| 逻辑删除字段 | `deleted` | Entity 已用 `@TableLogic` 标注 |
| 下划线→驼峰 | `true` | MP 默认行为，与 Entity 注解匹配 |

---

## 6. 潜在问题与风险

### 6.1 XML Mapper 与 MP 混用

现有代码混用了 MyBatis-Plus 的 `BaseMapper` 自动 CRUD 和手写 XML 的自定义查询。

**风险**: MP 的自动 CRUD 和 XML 中的自定义查询使用同一个 Mapper 接口，这是标准做法，无冲突。

### 6.2 数据库驱动

`application.yml` 中的 `driver-class-name` 使用 `com.mysql.cj.jdbc.Driver`（MySQL 8.x+），需确认实际数据库版本。如果使用 MySQL 5.x，改为 `com.mysql.jdbc.Driver`。

### 6.3 common 模块为空

如果 `mvn install` 报错 `Could not resolve artifact common:jar`，需要先 `mvn install -pl common` 安装空壳到本地仓库。

---

## 7. 验证步骤

```bash
# 1. 编译（跳过测试）
cd sales
mvn clean compile -DskipTests

# 2. 如果成功，会看到 MyBatis 加载日志
# Expected: o.a.i.l.slf4j.Slf4jImpl: SLF4J logger found

# 3. 启动验证
mvn spring-boot:run
# Expected: Tomcat started on port(s) and SalesApplication main method called

# 4. 数据库连接验证（通过 logs）
# Expected: HikariPool - Start completed
```

---

## 8. NOT in scope

以下内容不在本方案范围内，后续按需处理：

| 事项 | 原因 |
|------|------|
| common 模块填充共享代码 | 当前无共享需求，保持空壳 |
| 多数据源配置 | 当前单一模块单一数据源 |
| 数据库迁移工具（Flyway/Liquibase） | 数据库 schema 已定义在 `database.sql` |
| 单元测试 / 集成测试 | 引入 Spring Boot 后按需添加 |
| Actuator / 健康检查端点 | 按需添加 |
| 配置加密 | 按需 |
| Docker 部署 | 按需 |
| API 层（Controller） | sales 模块目前仅有数据访问层 |

---

## 9. What Already Exists

| 组件 | 状态 | 是否复用 |
|------|------|----------|
| MyBatis-Plus Entity 注解 | `CustomerEntity` 等 7 个实体已定义完整 | **直接复用**，无需修改 |
| MyBatis-Plus BaseMapper 接口 | `CustomerDao` 等 7 个接口已定义 | **直接复用**，通过 `@MapperScan` 注册 |
| XML Mapper 文件 | `CustomerDao.xml` 等 7 个 XML 已定义 | **直接复用**，需配置路径 |
| Maven 多模块结构 | root + common + sales 已搭建 | **直接复用**，调整 parent |
| Java 21 环境 | 已配置 | **直接复用** |

---

## 10. 实施顺序

```
Step 1: 修改 root pom.xml              (引入 spring-boot-starter-parent 作为 child)
Step 2: 修改 sales/pom.xml             (独立 parent + 依赖声明)
Step 3: 创建 SalesApplication.java     (启动类 + @MapperScan)
Step 4: 创建 application.yml            (数据源 + MP 配置)
Step 5: 安装 common 模块                (mvn install -pl common)
Step 6: 编译验证                        (mvn clean compile -DskipTests)
Step 7: 启动验证                        (mvn spring-boot:run)
```

**关键路径**: Step 1 → Step 2 可并行进行，Step 3/4 可并行进行，Step 5 在 Step 6 前必须完成。

# Spring Boot 引入方案 — sales 模块（Plan02）

## 1. 当前状态

```
NeoCC (root pom — 仅标识子模块，不参与依赖管理)
├── common/        (空壳，仅 parent 引用)
└── sales/         (依赖 common，但无任何实际依赖)
    ├── entity/     7 个 MyBatis-Plus Entity (带 @TableName/@TableId/@TableLogic)
    ├── dao/        7 个 MyBatis-Plus BaseMapper 接口
    └── resources/sales/mapper/  7 个 MyBatis XML Mapper
```

Java 版本: **21**，Maven 多模块结构。

---

## 2. 目标

- Root pom **仅包含 `<modules>` 列表**，不参与任何依赖管理
- 所有依赖下沉到 `common` 和各子服务
- `common` 模块作为**共享依赖中心**，统一管理版本
- `sales` 模块在 common 基础上引人 Spring Boot 3.x + MyBatis-Plus 3.5.7

---

## 3. 架构决策

### 3.1 依赖管理层次

```
root pom (pure aggregator)
└── modules: [common, sales]
     │
     ├── common (dependency management center)
     │    └── parent: spring-boot-starter-parent:3.2.5
     │         ├── dependencyManagement: mybatis-plus, mysql-connector-j, lombok
     │         └── dependencies: lombok (provided)
     │
     └── sales (actual service)
          └── parent: common
               ├── spring-boot-starter
               ├── mybatis-plus-spring-boot3-starter
               ├── mysql-connector-j
               └── dependency: common
```

**Root pom 完全不管理依赖版本**，只负责 `mvn install` 时找到 submodules。

### 3.2 common 模块定位

`common` 承担两个职责：
1. **版本管理中心** — 通过 `spring-boot-starter-parent` 管理版本，子模块无需声明版本号
2. **共享依赖提供方** — 所有模块共用的依赖（如 Lombok）在 common 中声明

**为什么不直接在 sales 中管理？**
若未来有 `order`、`warehouse` 等新模块，每个模块都要重复 `dependencyManagement` 的版本声明。通过 common 集中管理，新模块引人依赖时**无需声明版本**。

### 3.3 Spring Boot 版本选择

| 版本 | Java 要求 | MyBatis-Plus 支持 |
|------|-----------|-------------------|
| Spring Boot 2.7.x | Java 8-17 | MP 3.5.x | 不选 — Java 21 浪费 |
| **Spring Boot 3.2.x** | Java 17+ | MP 3.5.x+ | **选这个** |

选择 **Spring Boot 3.2.5 + MyBatis-Plus 3.5.7**，与 Java 21 匹配。

### 3.4 Entity / DAO / XML 复用策略

现有代码**全部无需修改**：

| 组件 | 数量 | 处理方式 |
|------|------|----------|
| Entity 类 | 7 个 | **直接使用**，注解已完整 |
| DAO 接口 | 7 个 | **直接使用**，通过 `@MapperScan` 注册 |
| XML Mapper | 7 个 | **直接使用**，配置 `mapper-locations` 即可 |

---

## 4. 实施步骤

### Step 1 — 简化 root pom.xml

**文件**: `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.dafuweng</groupId>
    <artifactId>NeoCC</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <module>common</module>
        <module>sales</module>
    </modules>

</project>
```

**变更说明**: 删除所有 `<properties>`、`<dependencyManagement>`、`<parent>`，仅保留 `modules` 列表。

### Step 2 — 重构 common/pom.xml

**文件**: `common/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>

    <groupId>com.dafuweng</groupId>
    <artifactId>common</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>21</java.version>
        <mybatis-plus.version>3.5.7</mybatis-plus.version>
        <mysql.version>8.3.0</mysql.version>
    </properties>

    <!--
        所有子模块共用的依赖在此声明版本，
        子模块引用时无需再声明版本号。
        若某依赖不希望传递，可放在 <dependencies> 中用 <optional>true</optional>。
    -->
    <dependencyManagement>
        <dependencies>

            <!-- MyBatis-Plus -->
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>

            <!-- MySQL 驱动（子模块用 runtime scope，不需要顶层声明） -->
            <dependency>
                <groupId>com.mysql</groupId>
                <artifactId>mysql-connector-j</artifactId>
                <version>${mysql.version}</version>
            </dependency>

        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- Lombok — 所有子模块都用到，且是 provided scope，不打包 -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>

</project>
```

**关键点**:
- `relativePath=""` 表示从 Maven 中央仓库获取 parent，不走本地层级
- `dependencyManagement` 中的版本对**所有子模块生效**
- `common` 本身 `dependencies` 中的 `lombok` 会**传递**给所有子模块（`optional:true` 表示可选传递）

### Step 3 — 修改 sales/pom.xml

**文件**: `sales/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 继承 common，由 common 提供版本管理 -->
    <parent>
        <groupId>com.dafuweng</groupId>
        <artifactId>common</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>sales</artifactId>
    <packaging>jar</packaging>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>

        <!-- 1. Spring Boot Web（包含内嵌 Tomcat）-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- 2. MyBatis-Plus — 版本由 common 的 dependencyManagement 提供 -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        </dependency>

        <!-- 3. MySQL 驱动 — runtime 即可 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- 4. 内部依赖：common（即使 common 是空壳，也保持声明）-->
        <dependency>
            <groupId>com.dafuweng</groupId>
            <artifactId>common</artifactId>
            <version>${project.version}</version>
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

**注意**: 没有声明 `mybatis-plus-spring-boot3-starter` 的 `version`，因为版本由 common 的 `dependencyManagement` 统一管理。

### Step 4 — 创建 Spring Boot 启动类

**文件**: `sales/src/main/java/com/dafuweng/sales/SalesApplication.java`

```java
package com.dafuweng.sales;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.dafuweng.sales.dao")
public class SalesApplication {
    public static void main(String[] args) {
        SpringApplication.run(SalesApplication.class, args);
    }
}
```

**关键点**:
- `@MapperScan("com.dafuweng.sales.dao")` — 扫描所有继承 `BaseMapper` 的接口，自动注册为 MyBatis Mapper
- 现有 DAO 接口上的 `@Mapper` 注解可**保留也可删除**，`@MapperScan` 会统一处理

### Step 5 — 创建 application.yml

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
  mapper-locations: classpath:sales/mapper/*.xml
  type-aliases-package: com.dafuweng.sales.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOut
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

**说明**:
- `mapper-locations` 指向现有 XML 文件所在路径
- `map-underscore-to-camel-case: true` 确保 `created_at` → `createdAt` 自动映射
- `logic-delete-field` 与 Entity 中的 `@TableLogic` 注解配合

### Step 6 — 确认 XML Mapper 兼容性

现有 XML Mapper 使用 MyBatis 原生格式（`namespace` 对应 DAO 全限定名），与 MyBatis-Plus 完全兼容，无需修改。

---

## 5. 依赖传递关系图

```
spring-boot-starter-parent:3.2.5  (common 的 parent)
│
└── common (jar)
     ├── dependencyManagement: mybatis-plus:3.5.7, mysql-connector-j:8.3.0
     └── dependencies: lombok (optional, provided)
          │
          └── sales (jar)
               ├── spring-boot-starter-web  → 传递: spring-boot-starter, tomcat, spring-webmvc
               ├── mybatis-plus-spring-boot3-starter → 传递: mybatis, mybatis-plus-core
               ├── mysql-connector-j (runtime)  → 传递: 无
               └── common  → 传递: lombok
```

---

## 6. 新增文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `sales/src/main/java/.../SalesApplication.java` | 新建 | Spring Boot 启动类 |
| `sales/src/main/resources/application.yml` | 新建 | 数据源 + MyBatis-Plus 配置 |
| `pom.xml` | 修改 | 删除依赖管理，仅保留 modules |
| `common/pom.xml` | 修改 | 引入 spring-boot-starter-parent + dependencyManagement |
| `sales/pom.xml` | 修改 | 引入依赖，parent 指向 common |

修改文件: **4 个**，新建文件: **2 个**。

---

## 7. 关键配置速查

| 配置项 | 值 | 说明 |
|--------|-----|------|
| Spring Boot | 3.2.5 | common 的 parent |
| MyBatis-Plus | 3.5.7 | common 的 dependencyManagement |
| Java | 21 | common 和 sales 的 properties |
| `@MapperScan` | `com.dafuweng.sales.dao` | 启动类注解 |
| XML 路径 | `classpath:sales/mapper/*.xml` | 匹配现有 XML 位置 |
| 逻辑删除字段 | `deleted` | Entity `@TableLogic` 已标注 |
| DB Driver | `com.mysql.cj.jdbc.Driver` | MySQL 8.x 使用此驱动类 |

---

## 8. 验证步骤

```bash
# 1. 先安装 common 模块（空壳 jar 到本地仓库）
mvn install -pl common -DskipTests

# 2. 编译 sales 模块
mvn clean compile -DskipTests -pl sales

# 3. 启动验证
mvn spring-boot:run -pl sales
# 期望: Tomcat 启动 + HikariPool 初始化 + SalesApplication main 执行

# 4. 若有数据库，可验证 Mapper 加载日志
# 期望看到: MybatisPlusInterceptor loaded 和各 Mapper Bean 注册
```

---

## 9. 扩展：未来新模块接入方式

当新增 `order` 模块时，只需：

```xml
<!-- order/pom.xml -->
<parent>
    <groupId>com.dafuweng</groupId>
    <artifactId>common</artifactId>
    <version>1.0-SNAPSHOT</version>
</parent>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    </dependency>
    <!-- 无需声明版本！由 common 统一管理 -->
</dependencies>
```

---

## 10. NOT in scope

| 事项 | 原因 |
|------|------|
| common 模块填充共享代码 | 当前为空壳，仅承担依赖管理职责 |
| 数据库迁移工具（Flyway/Liquibase） | database.sql 已定义 schema |
| API Controller 层 | sales 目前仅数据访问层 |
| 单元测试 / 集成测试 | 引入框架后按需添加 |
| Actuator 健康检查端点 | 按需添加 |
| 多数据源配置 | 当前单一数据源 |
| Docker / 部署配置 | 按需 |
| common 中引入数据库连接池（如 HikariCP） | Spring Boot starter-web 已自动带入 HikariCP |

---

## 11. What Already Exists

| 组件 | 状态 | 是否复用 |
|------|------|----------|
| MyBatis-Plus Entity 注解 | 7 个 Entity 已完整定义 | **直接复用** |
| MyBatis-Plus BaseMapper 接口 | 7 个 DAO 接口已完整定义 | **直接复用** |
| XML Mapper 文件 | 7 个 XML 已完整定义 | **直接复用** |
| Maven 多模块结构 | root + common + sales 已搭建 | **直接复用** |
| Java 21 环境 | 已配置 | **直接复用** |

---

## 12. 实施顺序

```
Step 1: 修改 root pom.xml              (删除所有管理，仅保留 modules)
Step 2: 修改 common/pom.xml             (引入 spring-boot-starter-parent + dependencyManagement)
Step 3: 修改 sales/pom.xml             (parent=common + 引入依赖)
Step 4: 创建 SalesApplication.java      (启动类 + @MapperScan)
Step 5: 创建 application.yml            (数据源 + MyBatis-Plus 配置)
Step 6: mvn install -pl common         (安装 common 到本地仓库)
Step 7: mvn compile -pl sales          (验证编译)
Step 8: mvn spring-boot:run -pl sales (验证启动)
```

**可并行**: Step 1、Step 2、Step 3 可并行修改；Step 4、Step 5 可并行创建。

**前置依赖**: Step 7 必须在 Step 6 完成后执行（因为 sales 依赖 common 的 jar）。

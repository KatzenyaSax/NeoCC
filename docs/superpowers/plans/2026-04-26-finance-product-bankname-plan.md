# 金融产品列表返回银行名称 — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修改金融产品后端，使其在列表查询时通过JOIN返回银行名称，前端无需额外请求即可显示银行名称。

**Architecture:** 在 `FinanceProductDao` 新增带 LEFT JOIN 的分页查询方法，Service 层调用新方法替代原有查询。Entity 层增加非数据库字段 `bankName` 接收联查结果。

**Tech Stack:** MyBatis-Plus, MyBatis XML, Spring Boot

---

### 任务 1: FinanceProductEntity 新增 bankName 字段

**Files:**
- Modify: `finance/src/main/java/com/dafuweng/finance/entity/FinanceProductEntity.java`

- [ ] **Step 1: 添加非数据库字段**

在 `FinanceProductEntity.java` 的 `bankId` 字段后添加：

```java
@TableField(exist = false)
private String bankName;
```

- [ ] **Step 2: 提交**

```bash
git add finance/src/main/java/com/dafuweng/finance/entity/FinanceProductEntity.java
git commit -m "feat(finance): FinanceProductEntity新增bankName非数据库字段"
```

---

### 任务 2: FinanceProductDao 新增 selectPageWithBank 方法

**Files:**
- Modify: `finance/src/main/java/com/dafuweng/finance/dao/FinanceProductDao.java`

- [ ] **Step 1: 添加 JOIN 查询方法**

在 `FinanceProductDao.java` 中新增方法：

```java
@Select("SELECT fp.*, b.bank_name AS bankName " +
        "FROM finance_product fp " +
        "LEFT JOIN bank b ON fp.bank_id = b.id " +
        "WHERE fp.deleted = 0 " +
        "ORDER BY fp.created_at DESC")
IPage<FinanceProductEntity> selectPageWithBank(@Param("page") IPage<FinanceProductEntity> page);
```

注意需要导入：
```java
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
```

- [ ] **Step 2: 提交**

```bash
git add finance/src/main/java/com/dafuweng/finance/dao/FinanceProductDao.java
git commit -m "feat(finance): FinanceProductDao新增selectPageWithBank联查方法"
```

---

### 任务 3: FinanceProductServiceImpl 改用新查询方法

**Files:**
- Modify: `finance/src/main/java/com/dafuweng/finance/service/impl/FinanceProductServiceImpl.java`

- [ ] **Step 1: 修改 pageList 方法，调用 selectPageWithBank**

将 `pageList` 方法中的：
```java
IPage<FinanceProductEntity> result = financeProductDao.selectPage(page, wrapper);
```

改为：
```java
IPage<FinanceProductEntity> result = financeProductDao.selectPageWithBank(page);
```

注意：由于使用了 LEFT JOIN 且联查条件固定（deleted=0），原有的 LambdaQueryWrapper 条件不再适用，新方法独立实现分页查询逻辑。

- [ ] **Step 2: 提交**

```bash
git add finance/src/main/java/com/dafuweng/finance/service/impl/FinanceProductServiceImpl.java
git commit -m "feat(finance): FinanceProductServiceImpl.pageList改用selectPageWithBank"
```

---

### 验证

1. 启动后端服务
2. 访问金融产品列表 API（如 `GET /financeProduct/page`）
3. 检查返回每条记录的 `bankName` 字段是否有值
4. 访问前端金融产品页面，表格"银行"列应显示银行名称

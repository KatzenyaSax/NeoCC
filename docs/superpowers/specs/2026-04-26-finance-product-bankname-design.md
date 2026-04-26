# 金融产品列表返回银行名称 — 设计方案

## 背景

金融产品列表页面（`ruoyi-ui/src/views/finance/product/index.vue`）表格列"银行"需要显示银行名称，但后端API `listFinanceProduct`（即 `pageList`）仅返回 `bankId`，不返回 `bankName`。

## 方案

在 `FinanceProductDao` 新增 JOIN 查询方法，列表返回时带上 `bankName`。

### 1. Entity 层

在 `FinanceProductEntity` 添加非数据库字段 `bankName`：

```java
@TableField(exist = false)
private String bankName;
```

### 2. Dao 层

在 `FinanceProductDao.java` 新增方法 `selectPageWithBank`：

```java
@Select("SELECT fp.*, b.bank_name AS bankName " +
        "FROM finance_product fp " +
        "LEFT JOIN bank b ON fp.bank_id = b.id " +
        "WHERE fp.deleted = 0 " +
        "ORDER BY fp.created_at DESC")
IPage<FinanceProductEntity> selectPageWithBank(@Param("page") IPage<FinanceProductEntity> page);
```

### 3. Service 层

修改 `FinanceProductServiceImpl.pageList`，调用新方法 `selectPageWithBank` 替代原来的 `selectPage`。

### 4. 前端

前端 `product/index.vue` 无需改动，直接使用响应中的 `bankName`。

## 影响范围

- `FinanceProductEntity.java` — 新增字段
- `FinanceProductDao.java` — 新增方法
- `FinanceProductServiceImpl.java` — 改一处方法调用

## 验证

启动后端后，访问金融产品列表页，表格"银行"列应显示银行名称而非ID。

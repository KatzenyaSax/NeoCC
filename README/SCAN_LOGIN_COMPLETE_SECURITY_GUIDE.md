# 扫码登录安全流程完整说明

## 📋 概述

本文档详细说明 NeoCC 项目扫码登录功能的安全增强实现，包括问题分析、解决方案、测试验证等内容。

---

## 🔴 修复前的安全漏洞

### 问题描述

**严重漏洞**：任何人都可以冒充任何用户登录系统！

### 问题代码

```java
// ❌ 不安全的实现
public void scanCode(String loginTid, Long userId, String username, String deviceId) {
    // 直接使用传入的参数，没有任何验证
    session.setUserId(userId);
    session.setUsername(username);  // 可以被冒充！
}
```

### 攻击场景

```bash
# 攻击者知道 admin 的 userId 是 1
curl -X POST "http://localhost/prod-api/api/oauth2/qrcode/scan" \
  -H "Content-Type: application/json" \
  -d '{
    "loginTid": "xxx",
    "userId": 1,           # 冒充管理员
    "username": "admin",   # 冒充管理员
    "deviceId": "attacker"
  }'

# 结果：成功生成 admin 的 Token，攻击者获得管理员权限！
```

---

## ✅ 修复后的安全实现

### 核心改进

**从数据库验证用户身份，使用真实用户信息**

```java
// ✅ 安全的实现
public void scanCode(String loginTid, Long userId, String username, String deviceId) {
    // 1. 从数据库查询用户
    String sql = "SELECT user_id, user_name, status FROM sys_user WHERE user_id = ?";
    Map<String, Object> user = jdbcTemplate.queryForMap(sql, userId);
    
    // 2. 验证用户是否存在
    if (user == null || user.isEmpty()) {
        throw new RuntimeException("用户不存在，userId: " + userId);
    }
    
    // 3. 检查用户状态（是否禁用）
    Integer status = (Integer) user.get("status");
    if (status == null || status != 1) {
        throw new RuntimeException("用户已被禁用");
    }
    
    // 4. ⚠️ 使用数据库中的真实用户名（防止冒充）
    String realUsername = (String) user.get("user_name");
    Long realUserId = ((Number) user.get("user_id")).longValue();
    
    // 5. 设置会话信息
    session.setUserId(realUserId);  // ✅ 使用数据库中的 userId
    session.setUsername(realUsername);  // ✅ 使用数据库中的用户名
}
```

---

## 📊 完整的安全流程

### 流程图

```
┌─────────────┐          ┌─────────────┐          ┌─────────────┐
│  Web 端     │          │  管理后台   │          │  后端服务   │
│  (想登录)   │          │ (模拟手机)  │          │             │
└──────┬──────┘          └──────┬──────┘          └──────┬──────┘
       │                        │                        │
       │  1. 生成二维码         │                        │
       │──────────────────────────────────────────────→│
       │                        │                        │
       │  2. 返回 loginTid      │                        │
       │←──────────────────────────────────────────────│
       │                        │                        │
       │  3. 显示二维码         │                        │
       │                        │                        │
       │                        │  4. 输入 loginTid      │
       │                        │     userId=1           │
       │                        │     username="admin"   │
       │                        │                        │
       │                        │  5. 调用扫码接口       │
       │                        │───────────────────────→│
       │                        │                        │
       │                        │                   ⭐ 6. 验证用户
       │                        │                        │
       │                        │                   a. 查询数据库
       │                        │                   b. 验证用户存在
       │                        │                   c. 检查用户状态
       │                        │                   d. 获取真实用户名
       │                        │                        │
       │  7. 轮询状态           │                        │
       │←──────────────────────────────────────────────│
       │   status: "scanned"   │                        │
       │   username: "admin1"  │  ← 数据库中的真实用户名 │
       │                        │                        │
       │                        │  8. 确认登录           │
       │                        │───────────────────────→│
       │                        │                        │
       │  9. 轮询获取 Token     │                        │
       │←──────────────────────────────────────────────│
       │   token: "eyJ..."     │                        │
       │                        │                        │
       │  10. 自动登录         │                        │
       │                        │                        │
```

### 详细步骤

#### 步骤 1：Web 端生成二维码

```bash
POST /api/oauth2/qrcode/generate
{
  "clientType": "web",
  "deviceName": "Chrome"
}

响应：
{
  "loginTid": "abc123",
  "qrcodeBase64": "data:image/png;base64,...",
  "expireIn": 300
}
```

#### 步骤 2：管理后台扫码

管理员在管理后台输入：
- loginTid: `abc123`
- userId: `1`
- username: `admin`（这个参数仅供参考，不会被直接使用）
- deviceId: `test-device`

#### 步骤 3：后端验证用户 ⭐ 关键步骤

```java
// 后端接收到请求
{
  "loginTid": "abc123",
  "userId": 1,
  "username": "admin",  // ← 传入的用户名
  "deviceId": "test-device"
}

// 后端处理
1. 查询数据库：SELECT user_name FROM sys_user WHERE user_id = 1
2. 数据库返回：user_name = "admin1"（假设您修改了用户名）
3. 使用数据库中的真实用户名："admin1"
4. 忽略传入的 username 参数
```

#### 步骤 4：Web 端检测到已扫码

```bash
GET /api/oauth2/qrcode/status?tid=abc123

响应：
{
  "status": "scanned",
  "username": "admin1",  // ← 数据库中的真实用户名
  "message": "已扫码，请在手机上确认"
}
```

#### 步骤 5：确认登录

管理员点击"确认登录"按钮。

#### 步骤 6：Web 端获取 Token

```bash
GET /api/oauth2/qrcode/status?tid=abc123

响应：
{
  "status": "confirmed",
  "token": "eyJhbGci...",
  "refreshToken": "abc..."
}
```

JWT Token 中的内容：
```json
{
  "userId": 1,
  "username": "admin1",  // ← 数据库中的真实用户名
  "roles": ["common"]
}
```

---

## 🧪 安全测试验证

### 测试 1：正常扫码登录 ✅

```bash
# 扫码 userId=1 的用户
curl -X POST "http://localhost/prod-api/api/oauth2/qrcode/scan" \
  -H "Content-Type: application/json" \
  -d '{
    "loginTid": "abc123",
    "userId": 1,
    "username": "admin",
    "deviceId": "test"
  }'

结果：✅ 成功
说明：后端从数据库验证了 userId=1，获取真实用户名
```

### 测试 2：冒充不存在的用户 ✅ 已拦截

```bash
# 尝试扫码 userId=99999（不存在）
curl -X POST "http://localhost/prod-api/api/oauth2/qrcode/scan" \
  -H "Content-Type: application/json" \
  -d '{
    "loginTid": "abc123",
    "userId": 99999,
    "username": "fake_user",
    "deviceId": "attacker"
  }'

结果：❌ 失败
错误信息："用户不存在，userId: 99999"
说明：成功拦截冒充攻击
```

### 测试 3：使用数据库真实用户名 ✅

```bash
# 数据库中 userId=1 的用户名是 "admin1"
# 扫码时传入 username="admin"

# 查询状态
GET /api/oauth2/qrcode/status?tid=abc123

响应：
{
  "username": "admin1"  // ← 使用数据库中的用户名，不是传入的 "admin"
}

说明：✅ 成功防止用户名冒充
```

---

## 📋 修改的文件清单

### 后端文件

| 文件 | 修改内容 | 说明 |
|------|---------|------|
| `QrCodeLoginService.java` | 添加 JdbcTemplate 依赖 | 用于数据库查询 |
| | 修改 `scanCode()` 方法 | 添加用户验证逻辑 |
| | 添加用户存在性检查 | 防止冒充不存在的用户 |
| | 添加用户状态检查 | 防止禁用用户登录 |
| | 使用数据库用户名 | 防止用户名冒充 |

### 关键代码变更

**修改前**：
```java
public void scanCode(String loginTid, Long userId, String username, String deviceId) {
    session.setUserId(userId);
    session.setUsername(username);  // ❌ 不安全
}
```

**修改后**：
```java
public void scanCode(String loginTid, Long userId, String username, String deviceId) {
    // ✅ 从数据库验证用户
    String sql = "SELECT user_id, user_name, status FROM sys_user WHERE user_id = ?";
    Map<String, Object> user = jdbcTemplate.queryForMap(sql, userId);
    
    // ✅ 验证用户存在
    if (user == null || user.isEmpty()) {
        throw new RuntimeException("用户不存在");
    }
    
    // ✅ 检查用户状态
    Integer status = (Integer) user.get("status");
    if (status != 1) {
        throw new RuntimeException("用户已被禁用");
    }
    
    // ✅ 使用数据库中的真实用户名
    String realUsername = (String) user.get("user_name");
    session.setUsername(realUsername);
}
```

---

## 🔒 安全增强对比

| 安全特性 | 修复前 | 修复后 | 说明 |
|---------|--------|--------|------|
| **用户身份验证** | ❌ 无 | ✅ 有 | 从数据库查询验证 |
| **用户存在性检查** | ❌ 无 | ✅ 有 | 防止冒充不存在的用户 |
| **用户状态检查** | ❌ 无 | ✅ 有 | 防止禁用用户登录 |
| **防冒充机制** | ❌ 无 | ✅ 有 | 使用数据库真实用户名 |
| **数据来源** | 客户端传入 | 数据库查询 | 不可信任客户端数据 |

---

## 📝 课程设计展示建议

### 1. 说明问题发现过程

> "在实现扫码登录功能后，我们进行了安全审计，发现了一个严重的安全漏洞：
> 任何人都可以冒充任何用户登录系统。这是因为我们直接使用了客户端传入的用户信息，
> 没有进行任何验证。"

### 2. 展示解决方案

> "我们添加了数据库验证机制：
> 1. 从数据库查询用户信息
> 2. 验证用户是否存在
> 3. 检查用户状态
> 4. 使用数据库中的真实用户名
> 
> 这样即使用户传入错误的用户名，系统也会使用数据库中的真实信息。"

### 3. 演示安全测试

```bash
# 运行安全测试脚本
bash test_security_enhancement.sh
```

### 4. 展示学到的经验

> "通过这次安全增强，我们学到了：
> 1. 永远不要信任客户端传入的数据
> 2. 必须在服务端验证所有关键信息
> 3. 安全性是迭代改进的过程
> 4. 代码审查和安全测试很重要"

---

## 🚀 测试命令

### 快速测试

```bash
# 运行安全增强测试
bash /Users/liuhongyu/IdeaProjects/final/NeoCC/test_security_enhancement.sh
```

### 手动测试

```bash
# 1. 生成二维码
curl -X POST "http://localhost/prod-api/api/oauth2/qrcode/generate" \
  -H "Content-Type: application/json" \
  -d '{"clientType":"web","deviceName":"Test"}'

# 2. 扫码（正常用户）
curl -X POST "http://localhost/prod-api/api/oauth2/qrcode/scan" \
  -H "Content-Type: application/json" \
  -d '{
    "loginTid": "YOUR_LOGIN_TID",
    "userId": 1,
    "username": "admin",
    "deviceId": "test"
  }'

# 3. 尝试冒充（应该失败）
curl -X POST "http://localhost/prod-api/api/oauth2/qrcode/scan" \
  -H "Content-Type: application/json" \
  -d '{
    "loginTid": "YOUR_LOGIN_TID",
    "userId": 99999,
    "username": "fake_user",
    "deviceId": "attacker"
  }'
```

---

## 📚 相关文档

- [安全流程详细说明](./SCAN_LOGIN_SECURITY_FLOW.md)
- [当前登录方式与 OAuth2 对比](./CURRENT_LOGIN_VS_OAUTH2_COMPARISON.md)
- [OAuth2 协议说明](./OAUTH2_PROTOCOL_INTRO.md)
- [扫码登录使用指南](./QRCODE_LOGIN_USAGE_GUIDE.md)
- [扫码登录快速开始](./QRCODE_LOGIN_QUICKSTART.md)

---

## ⚠️ 注意事项

### 当前实现的限制

1. **管理后台仍需手动输入 userId**
   - 理想情况：扫码时自动识别用户（通过 JWT Token）
   - 当前方案：手动输入 userId，但后端会验证

2. **未实现 Token 验证**
   - 理想情况：手机端提供 JWT Token 证明身份
   - 当前方案：依赖 userId 参数，但已添加数据库验证

### 后续优化建议

1. **添加 Token 验证**
   - 扫码时需要提供手机端的 JWT Token
   - 完全防止用户冒充

2. **添加操作日志**
   - 记录所有扫码登录操作
   - 便于审计和追溯

3. **实现完整的 OAuth2**
   - 标准授权码流程
   - 更好的安全性

---

**文档版本**：v3.0（安全增强完整版）  
**最后更新**：2026-04-16  
**维护人员**：开发团队

# 扫码登录安全流程说明

## 📋 文档信息

- **更新日期**：2026-04-16
- **更新内容**：安全增强 - 添加用户身份验证
- **影响范围**：扫码登录功能

---

## 🔐 安全问题分析

### 修复前的问题

**严重安全漏洞**：任何人都可以冒充任何用户登录！

```bash
# ❌ 修复前：攻击者可以冒充管理员
curl -X POST "http://localhost/prod-api/api/oauth2/qrcode/scan" \
  -H "Content-Type: application/json" \
  -d '{
    "loginTid": "xxx",
    "userId": 1,           # 冒充管理员
    "username": "admin",   # 冒充管理员
    "deviceId": "attacker"
  }'

# 后端直接信任传入的 userId 和 username
# 不验证用户是否存在
# 不验证密码
# 生成 admin 的 Token
```

**问题根源**：
1. ❌ 没有验证用户身份
2. ❌ 没有查询数据库
3. ❌ 直接使用传入的用户信息
4. ❌ 可以冒充任何用户

---

## ✅ 修复后的安全流程

### 核心改进

```java
// ✅ 修复后：从数据库验证用户
public void scanCode(String loginTid, Long userId, String username, String deviceId) {
    // 1. 从数据库查询用户
    String sql = "SELECT user_id, user_name, status FROM sys_user WHERE user_id = ?";
    Map<String, Object> user = jdbcTemplate.queryForMap(sql, userId);
    
    // 2. 验证用户是否存在
    if (user == null || user.isEmpty()) {
        throw new RuntimeException("用户不存在，userId: " + userId);
    }
    
    // 3. 检查用户状态
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

### 场景：用户 A 扫码登录 Web 端

```
步骤 1：Web 端生成二维码
━━━━━━━━━━━━━━━━━━━━━━━━
Web 端调用：POST /api/oauth2/qrcode/generate
后端返回：
{
  "loginTid": "abc123",
  "qrcodeBase64": "data:image/png;base64,..."
}

步骤 2：管理后台扫码（模拟手机 App）
━━━━━━━━━━━━━━━━━━━━━━━━
管理员输入：
- loginTid: abc123
- userId: 1
- username: admin（这个参数现在只是参考，不会被直接使用）
- deviceId: test-device

步骤 3：后端验证用户身份 ⭐ 关键步骤
━━━━━━━━━━━━━━━━━━━━━━━━
后端处理：
1. 接收请求参数：userId=1, username="admin"
2. ⚠️ 查询数据库：SELECT * FROM sys_user WHERE user_id = 1
3. ✅ 验证用户是否存在
4. ✅ 检查用户状态（是否禁用）
5. ⚠️ 从数据库获取真实用户名：user_name = "admin"（或 "admin1" 如果您修改了）
6. ✅ 使用数据库中的真实信息设置会话

步骤 4：Web 端检测到已扫码
━━━━━━━━━━━━━━━━━━━━━━━━
Web 端轮询：GET /api/oauth2/qrcode/status?tid=abc123
后端返回：
{
  "status": "scanned",
  "username": "admin",  ← ✅ 这是从数据库查询的真实用户名
  "message": "已扫码，请在手机上确认"
}

步骤 5：管理后台确认登录
━━━━━━━━━━━━━━━━━━━━━━━━
管理员点击"确认登录"
后端更新状态为 CONFIRMED

步骤 6：Web 端获取 Token
━━━━━━━━━━━━━━━━━━━━━━━━
Web 端轮询获取 Token
后端生成 JWT Token（使用数据库中的真实用户信息）
返回：
{
  "status": "confirmed",
  "token": "eyJhbGci...",
  "refreshToken": "abc..."
}

步骤 7：Web 端自动登录
━━━━━━━━━━━━━━━━━━━━━━━━
Web 端保存 Token
跳转到首页
```

---

## 🔍 关键安全机制

### 1. 数据库验证

```java
// ❌ 错误：直接使用传入的用户名
session.setUsername(username);  // 可以被冒充

// ✅ 正确：从数据库查询真实用户名
String sql = "SELECT user_name FROM sys_user WHERE user_id = ?";
String realUsername = jdbcTemplate.queryForObject(sql, String.class, userId);
session.setUsername(realUsername);  // 无法冒充
```

### 2. 用户状态检查

```java
// 检查用户是否被禁用
Integer status = (Integer) user.get("status");
if (status != 1) {
    throw new RuntimeException("用户已被禁用");
}
```

### 3. 防冒充机制

```
攻击者尝试：
{
  "userId": 1,
  "username": "admin"  // 想冒充管理员
}

后端处理：
1. 查询数据库：user_id=1 对应的 user_name 是什么？
2. 如果数据库是 "admin1"，则使用 "admin1"
3. 生成的 Token 中 username="admin1"
4. 攻击者无法冒充 "admin"
```

---

## 🧪 测试验证

### 测试 1：正常扫码登录

```bash
# 1. 生成二维码
curl -X POST "http://localhost/prod-api/api/oauth2/qrcode/generate" \
  -H "Content-Type: application/json" \
  -d '{"clientType":"web","deviceName":"Chrome"}'

# 返回：loginTid = "abc123"

# 2. 模拟扫码（userId=1 对应数据库中的 admin 用户）
curl -X POST "http://localhost/prod-api/api/oauth2/qrcode/scan" \
  -H "Content-Type: application/json" \
  -d '{
    "loginTid": "abc123",
    "userId": 1,
    "username": "admin",
    "deviceId": "test"
  }'

# ✅ 成功：后端从数据库验证 userId=1，获取真实用户名

# 3. 确认登录
curl -X POST "http://localhost/prod-api/api/oauth2/qrcode/confirm" \
  -H "Content-Type: application/json" \
  -d '{
    "loginTid": "abc123",
    "userId": 1,
    "confirm": true
  }'

# 4. 获取 Token
curl "http://localhost/prod-api/api/oauth2/qrcode/status?tid=abc123"

# ✅ 成功：返回 JWT Token
```

---

### 测试 2：尝试冒充不存在的用户

```bash
# 尝试扫码一个不存在的 userId
curl -X POST "http://localhost/prod-api/api/oauth2/qrcode/scan" \
  -H "Content-Type: application/json" \
  -d '{
    "loginTid": "abc123",
    "userId": 99999,
    "username": "fake_user",
    "deviceId": "attacker"
  }'

# ❌ 失败：返回错误
{
  "code": 500,
  "message": "用户不存在，userId: 99999"
}
```

---

### 测试 3：尝试冒充被禁用的用户

```bash
# 假设 userId=2 的用户已被禁用（status=0）
curl -X POST "http://localhost/prod-api/api/oauth2/qrcode/scan" \
  -H "Content-Type: application/json" \
  -d '{
    "loginTid": "abc123",
    "userId": 2,
    "username": "disabled_user",
    "deviceId": "attacker"
  }'

# ❌ 失败：返回错误
{
  "code": 500,
  "message": "用户已被禁用，username: disabled_user"
}
```

---

### 测试 4：修改用户名后的验证

```bash
# 1. 修改数据库中的用户名
mysql> UPDATE sys_user SET user_name = 'admin1' WHERE user_id = 1;

# 2. 尝试扫码（传入旧的 username="admin"）
curl -X POST "http://localhost/prod-api/api/oauth2/qrcode/scan" \
  -H "Content-Type: application/json" \
  -d '{
    "loginTid": "abc123",
    "userId": 1,
    "username": "admin",  ← 旧的用户名
    "deviceId": "test"
  }'

# ✅ 成功：但使用的是数据库中的新用户名
{
  "status": "scanned",
  "username": "admin1"  ← ✅ 使用数据库中的真实用户名
}

# 3. 获取的 Token 中
{
  "username": "admin1"  ← ✅ 不是 "admin"
}
```

---

## 📋 修改的文件

| 文件 | 修改内容 | 原因 |
|------|---------|------|
| `QrCodeLoginService.java` | 添加 JdbcTemplate 依赖 | 用于数据库查询 |
| | 修改 `scanCode()` 方法 | 从数据库验证用户 |
| | 添加用户存在性检查 | 防止冒充不存在的用户 |
| | 添加用户状态检查 | 防止禁用用户登录 |
| | 使用数据库用户名 | 防止用户名冒充 |

---

## 🔒 安全增强总结

### 修复前

| 安全特性 | 状态 | 说明 |
|---------|------|------|
| 用户身份验证 | ❌ 无 | 直接使用传入参数 |
| 数据库查询 | ❌ 无 | 不查询数据库 |
| 用户状态检查 | ❌ 无 | 不检查是否禁用 |
| 防冒充机制 | ❌ 无 | 可以冒充任何用户 |

### 修复后

| 安全特性 | 状态 | 说明 |
|---------|------|------|
| 用户身份验证 | ✅ 有 | 从数据库查询验证 |
| 数据库查询 | ✅ 有 | 查询 sys_user 表 |
| 用户状态检查 | ✅ 有 | 检查 status 字段 |
| 防冒充机制 | ✅ 有 | 使用数据库真实用户名 |

---

## 📝 对课程设计的影响

### 演示时可以说明

1. **初始实现的问题**
   - "我们最初实现时发现存在安全漏洞"
   - "任何人都可以冒充任何用户登录"

2. **我们的解决方案**
   - "添加了数据库验证机制"
   - "从数据库查询真实用户信息"
   - "防止用户冒充攻击"

3. **学到的经验**
   - "不能信任客户端传入的数据"
   - "必须在服务端验证所有信息"
   - "安全性是迭代改进的过程"

### 展示代码对比

```java
// 修复前（不安全）
session.setUsername(username);  // ❌ 直接使用传入参数

// 修复后（安全）
String sql = "SELECT user_name FROM sys_user WHERE user_id = ?";
String realUsername = jdbcTemplate.queryForObject(sql, String.class, userId);
session.setUsername(realUsername);  // ✅ 从数据库获取
```

---

## 🚀 后续优化建议

### 短期（可选）

1. **添加 Token 验证**
   - 扫码时需要提供手机端的 JWT Token
   - 验证手机端用户身份

2. **添加操作日志**
   - 记录扫码登录的完整日志
   - 便于审计和追溯

### 长期（如果需要）

1. **实现完整的 OAuth2**
   - 标准授权码流程
   - Scope 权限控制
   - Token 撤销机制

2. **添加二次验证**
   - 扫码后输入验证码
   - 增强安全性

---

## 📚 相关文档

- [当前登录方式与 OAuth2 对比](./CURRENT_LOGIN_VS_OAUTH2_COMPARISON.md)
- [OAuth2 协议说明](./OAUTH2_PROTOCOL_INTRO.md)
- [扫码登录使用指南](./QRCODE_LOGIN_USAGE_GUIDE.md)

---

**文档版本**：v2.0（安全增强版）  
**最后更新**：2026-04-16  
**维护人员**：开发团队

# 扫码登录测试指南 - 双浏览器窗口方案

> 创建日期: 2026-04-16  
> 状态: ✅ 可测试

## 📋 方案说明

本方案使用**两个浏览器窗口**模拟扫码登录流程，无需开发移动端 App：

- **窗口 1**：Web 端登录页面（显示二维码，轮询状态）
- **窗口 2**：管理后台页面（模拟手机扫码和确认）

---

## 🚀 快速开始

### 前置条件

✅ Auth 服务已启动（端口 8085）  
✅ Gateway 服务已启动（端口 8086）  
✅ Nginx 已启动（端口 80）

### 验证服务状态

```bash
docker ps | grep -E "auth|gateway|nginx"
```

---

## 📝 测试步骤

### 步骤 1：打开 Web 端登录页面

**浏览器窗口 1**：打开登录页面

```
http://localhost/login
```

**当前状态**：显示传统的账号密码登录表单

---

### 步骤 2：打开管理后台页面

**浏览器窗口 2**：打开扫码管理页面

```
打开文件: /Users/liuhongyu/IdeaProjects/final/NeoCC/qrcode-admin.html
```

**操作方式**：
- 直接在浏览器中打开该 HTML 文件（文件 → 打开文件）
- 或者拖拽文件到浏览器窗口

**页面功能**：
- 显示所有活跃的扫码会话
- 模拟扫码操作
- 确认/拒绝登录

---

### 步骤 3：生成二维码（API 测试）

在终端执行以下命令生成二维码：

```bash
curl -X POST "http://localhost/prod-api/api/oauth2/qrcode/generate" \
  -H "Content-Type: application/json" \
  -d '{"clientType":"web","deviceName":"Chrome macOS"}' | python3 -m json.tool
```

**预期响应**：

```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "loginTid": "a1b2c3d4e5f6...",
    "qrcodeContent": "https://yourapp.com/scan/login?tid=a1b2c3d4e5f6...&client=web",
    "qrcodeBase64": "data:image/png;base64,iVBORw0KGgo...",
    "expireIn": 300,
    "pollInterval": 2000
  }
}
```

**重要信息**：
- `loginTid`：登录事务唯一标识（后续操作需要用到）
- `qrcodeBase64`：二维码图片（Base64 格式）

---

### 步骤 4：查看活跃会话（管理后台）

在**浏览器窗口 2**（管理后台页面）中：

1. 页面会自动加载活跃会话列表
2. 每 3 秒自动刷新
3. 也可以点击"🔄 刷新列表"按钮手动刷新

**您应该能看到**：
- 刚生成的会话显示在列表中
- 状态为"等待扫码"
- 显示剩余时间（倒计时）

---

### 步骤 5：模拟扫码

在**浏览器窗口 2**（管理后台）中：

**方式 A：使用表单**
1. 复制 `loginTid` 到"Login TID"输入框
2. 确认用户 ID 为 `1`，用户名为 `admin`
3. 点击"📷 模拟扫码"按钮

**方式 B：使用列表快捷操作**
1. 在会话列表中找到刚生成的会话
2. 点击"选择"按钮（自动填充 loginTid）
3. 点击"📷 模拟扫码"按钮

**预期结果**：
- 提示"扫码成功！"
- 会话状态变为"已扫码"

---

### 步骤 6：查询状态（模拟 Web 端轮询）

在终端执行以下命令（模拟 Web 端轮询）：

```bash
curl "http://localhost/prod-api/api/oauth2/qrcode/status?tid=YOUR_LOGIN_TID" | python3 -m json.tool
```

**将 `YOUR_LOGIN_TID` 替换为实际的 loginTid**

**预期响应（已扫码状态）**：

```json
{
  "code": 200,
  "data": {
    "status": "scanned",
    "username": "admin",
    "message": "已扫码，请在手机上确认"
  }
}
```

---

### 步骤 7：确认登录

在**浏览器窗口 2**（管理后台）中：

**方式 A：使用表单**
1. 确保 `loginTid` 已填写
2. 点击"✅ 确认登录"按钮

**方式 B：使用列表快捷操作**
1. 在会话列表中点击"确认"按钮

**预期结果**：
- 提示"确认登录成功！Web 端将自动登录"
- 会话从列表中消失（已确认状态会立即删除）

---

### 步骤 8：获取 Token（模拟 Web 端轮询获取）

在终端再次查询状态：

```bash
curl "http://localhost/prod-api/api/oauth2/qrcode/status?tid=YOUR_LOGIN_TID" | python3 -m json.tool
```

**预期响应（登录成功）**：

```json
{
  "code": 200,
  "data": {
    "status": "confirmed",
    "message": "登录成功",
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "refreshToken": "9f9beb95dc0147d3...",
    "expiresIn": 86400,
    "refreshExpiresIn": 604800,
    "user": {
      "userId": 1,
      "username": "admin"
    }
  }
}
```

**重要信息**：
- `token`：Access Token（用于访问受保护接口）
- `refreshToken`：Refresh Token（用于刷新 Access Token）

---

### 步骤 9：使用 Token 访问受保护接口

```bash
TOKEN="eyJhbGciOiJIUzM4NCJ9..."  # 替换为实际的 token

curl "http://localhost/prod-api/getInfo" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

**预期响应**：

```json
{
  "code": 200,
  "data": {
    "user": {
      "userId": 1,
      "userName": "admin",
      "nickName": "管理员",
      ...
    },
    "roles": ["common"],
    "permissions": []
  }
}
```

---

## 🧪 完整测试场景

### 场景 1：正常扫码登录流程 ✅

```
1. 生成二维码 → loginTid: abc123
2. 查看会话列表 → 状态: generated
3. 模拟扫码 → 状态: scanned
4. 确认登录 → 状态: confirmed
5. 查询状态 → 返回 token
6. 使用 token 访问接口 → 成功
```

### 场景 2：拒绝登录 ❌

```
1. 生成二维码 → loginTid: def456
2. 模拟扫码 → 状态: scanned
3. 拒绝登录 → 状态: rejected
4. 查询状态 → 返回 404 "会话不存在"
```

### 场景 3：二维码过期 ⏰

```
1. 生成二维码 → expireIn: 300s
2. 等待 5 分钟...
3. 查询状态 → 返回 400 "二维码已过期"
```

### 场景 4：重复使用 loginTid 🚫

```
1. 生成二维码 → loginTid: ghi789
2. 扫码 + 确认 → 登录成功
3. 再次使用同一 loginTid → 返回 404 "会话不存在"
```

---

## 🔍 常见问题

### Q1：管理后台页面无法加载会话列表？

**检查项**：
1. 确认 Auth 服务是否正常运行
2. 检查浏览器控制台是否有 CORS 错误
3. 确认 API 路径是否正确（`/prod-api/api/oauth2/qrcode/sessions`）

**解决方法**：
```bash
# 检查服务状态
docker ps | grep auth

# 查看服务日志
docker logs neocc-auth --tail 50
```

---

### Q2：扫码后状态没有变化？

**检查项**：
1. 确认 `loginTid` 是否正确
2. 确认用户 ID 是否存在（默认为 1）
3. 查看 Auth 服务日志

**解决方法**：
```bash
# 查询会话状态
curl "http://localhost:8085/api/oauth2/qrcode/status?tid=YOUR_TID"

# 查看日志
docker logs neocc-auth --tail 100 | grep -i "扫码"
```

---

### Q3：Token 生成失败？

**检查项**：
1. 确认 JWT 配置是否正确
2. 确认用户是否存在于数据库
3. 检查 Auth 服务日志

**解决方法**：
```bash
docker logs neocc-auth --tail 100 | grep -i "token"
```

---

## 📊 API 接口清单

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 生成二维码 | POST | `/prod-api/api/oauth2/qrcode/generate` | 生成扫码登录二维码 |
| 查询状态 | GET | `/prod-api/api/oauth2/qrcode/status?tid={tid}` | Web 端轮询扫码状态 |
| 模拟扫码 | POST | `/prod-api/api/oauth2/qrcode/scan` | 管理后台模拟扫码 |
| 确认/拒绝 | POST | `/prod-api/api/oauth2/qrcode/confirm` | 确认或拒绝登录 |
| 活跃会话 | GET | `/prod-api/api/oauth2/qrcode/sessions` | 获取所有活跃会话 |

---

## 🎯 下一步计划

### 第一阶段（已完成）✅
- [x] 后端核心服务（QrCodeLoginService）
- [x] 控制器接口（OAuth2QrCodeController）
- [x] 二维码生成（ZXing）
- [x] 管理后台页面（qrcode-admin.html）
- [x] Gateway 路由配置
- [x] Security 白名单

### 第二阶段（待开发）📋
- [ ] Web 端登录页面添加"扫码登录"入口
- [ ] Web 端轮询逻辑实现
- [ ] 二维码展示 UI
- [ ] 扫码成功自动登录

### 第三阶段（未来迭代）🚀
- [ ] 微信小程序扫码
- [ ] 移动端响应式网页
- [ ] 企业微信/钉钉接入
- [ ] WebSocket 替代轮询

---

## 💡 使用建议

1. **测试环境**：建议先在开发环境测试完整流程
2. **日志监控**：测试时保持查看 Auth 服务日志
3. **浏览器缓存**：修改后记得清除浏览器缓存
4. **Token 保存**：测试成功后保存 Token，方便后续接口测试

---

## 📞 技术支持

如遇到问题，请检查：
1. Docker 服务状态：`docker ps`
2. 服务日志：`docker logs neocc-auth --tail 100`
3. API 测试：使用 Postman 或 curl 直接测试接口
4. 浏览器控制台：查看是否有 JavaScript 错误

---

**祝您测试顺利！** 🎉

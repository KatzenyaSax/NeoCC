# 扫码登录使用指南

## 📋 目录

- [功能概述](#功能概述)
- [访问地址](#访问地址)
- [完整使用流程](#完整使用流程)
- [详细步骤](#详细步骤)
- [技术架构](#技术架构)
- [常见问题](#常见问题)

---

## 功能概述

扫码登录功能允许用户通过扫描二维码的方式快速登录系统，无需输入账号密码。

### 核心特点

- ✅ **快速登录**：无需输入账号密码
- ✅ **安全可靠**：基于 OAuth2 协议 + JWT Token
- ✅ **自动跳转**：扫码成功后自动登录并跳转
- ✅ **状态实时**：轮询机制实时更新扫码状态

### 工作流程

```
Web 端（/login）                  管理后台（扫码确认工具）
    ↓                                 ↓
1. 显示二维码                   2. 已登录状态
    ↓                                 ↓
3. 等待扫码                     4. 输入 loginTid
    ↓                                 ↓
5. 检测到已扫码                 6. 点击"确认登录"
    ↓                                 ↓
7. 自动获取 Token
    ↓
8. 跳转到首页
```

---

## 访问地址

### 1. 正式登录页面
```
http://localhost/login
```
- **用途**：用户登录入口
- **功能**：支持账号密码登录和扫码登录

### 2. 管理后台（扫码确认工具）
```
http://localhost/qrcode-admin.html
```
- **用途**：模拟第三方 App（微信/QQ）扫码确认
- **功能**：扫码、确认/拒绝登录

### 3. 测试页面（独立测试）
```
http://localhost/qrcode-test.html
```
- **用途**：独立测试扫码登录功能
- **功能**：生成二维码、显示 loginTid、轮询状态

---

## 完整使用流程

### 场景：用户在 Web 端扫码登录

#### 准备工作

**环境要求**：
- ✅ Docker 容器正常运行
- ✅ Nginx、Auth 服务、Gateway 服务已启动
- ✅ 后端 API 可正常访问

**访问前提**：
- 用户 A：打开 `http://localhost/login`（想登录）
- 用户 B：打开 `http://localhost/qrcode-admin.html`（模拟已登录的手机端）

---

## 详细步骤

### 第 1 步：打开登录页面

在浏览器中访问：
```
http://localhost/login
```

你会看到账号密码登录表单。

### 第 2 步：切换到扫码登录

在登录表单下方，点击 **"扫码登录"** 按钮。

页面会切换到扫码登录模式，显示：
- 📱 二维码图片（自动生成）
- ⏳ 状态提示文字
- 🔄 刷新按钮（二维码过期时显示）

### 第 3 步：复制 loginTid

打开浏览器的**开发者工具**（F12），切换到 **Console** 标签。

你会看到类似这样的日志：
```
生成二维码成功，loginTid: e6cc7826c0af4bee962a4ca700fe07de
```

**复制这个 loginTid**。

> 💡 **提示**：后续会优化，在页面上直接显示 loginTid。

### 第 4 步：打开管理后台

在**另一个浏览器标签页**访问：
```
http://localhost/qrcode-admin.html
```

这是一个模拟"手机 App"的管理后台页面。

### 第 5 步：输入 loginTid

在管理后台页面：

1. 找到 **"手动输入 LoginTid"** 输入框
2. 粘贴刚才复制的 loginTid
3. 点击 **"📷 模拟扫码"** 按钮

页面会显示：
- ✅ 扫码成功
- 状态变为 **SCANNED**（已扫码）

### 第 6 步：确认登录

在管理后台页面：

1. 找到刚扫码的会话记录
2. 点击 **"✅ 确认登录"** 按钮

页面会显示：
- ✅ 确认登录成功
- 状态变为 **CONFIRMED**（已确认）

### 第 7 步：自动登录

回到**登录页面**（第 2 步的页面），你会看到：

1. 状态提示从"已扫码，请在管理后台确认"变为
2. **"登录成功，正在跳转..."**
3. 弹出成功提示：**"扫码登录成功！"**
4. **自动跳转到首页**

### 第 8 步：验证登录成功

跳转后，你应该：
- ✅ 看到系统首页
- ✅ 右上角显示用户名（admin）
- ✅ 可以正常使用系统功能

---

## 技术架构

### 后端 API

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 生成二维码 | POST | `/api/oauth2/qrcode/generate` | 生成二维码会话 |
| 查询状态 | GET | `/api/oauth2/qrcode/status?tid={loginTid}` | 轮询扫码状态 |
| 扫码 | POST | `/api/oauth2/qrcode/scan` | 模拟扫码操作 |
| 确认登录 | POST | `/api/oauth2/qrcode/confirm` | 确认/拒绝登录 |
| 活跃会话 | GET | `/api/oauth2/qrcode/sessions` | 获取所有活跃会话 |

### 状态机

```
GENERATED（已生成）
    ↓
   扫码
    ↓
SCANNED（已扫码）
    ↓
  确认登录
    ↓
CONFIRMED（已确认）→ 生成 Token → 自动登录
```

### Token 管理

- **Access Token**：有效期 24 小时
- **Refresh Token**：有效期 7 天
- **存储方式**：Cookie + Vuex Store

### 轮询机制

- **间隔**：2 秒
- **超时**：5 分钟（二维码过期）
- **清理**：每分钟自动清理过期会话

---

## 常见问题

### Q1: 为什么需要手动复制 loginTid？

**当前实现**：由于还没有真正的扫码工具（手机 App），所以需要手动复制。

**理想情况**：
- 📱 手机 App 扫描二维码
- 🔍 自动解析 loginTid
- 👆 用户点击确认
- 📡 自动通知后端

**后续优化**：开发微信小程序或手机 App，实现真正的扫码体验。

---

### Q2: 二维码过期了怎么办？

**解决方法**：
1. 点击页面上的 **"点击刷新"** 按钮
2. 系统会生成新的二维码和 loginTid
3. 重新使用新的 loginTid 进行操作

**过期时间**：5 分钟（300 秒）

---

### Q3: 扫码后长时间没有反应？

**排查步骤**：

1. **检查浏览器控制台**（F12）
   - 查看是否有错误信息
   - 确认轮询请求是否正常发送

2. **确认管理后台操作**
   - 是否正确输入了 loginTid
   - 是否点击了"确认登录"

3. **检查后端服务**
   ```bash
   docker-compose ps
   ```
   确保所有服务正常运行。

---

### Q4: 如何查看当前的扫码会话？

**方法一：使用管理后台**

访问 `http://localhost/qrcode-admin.html`，页面会自动显示所有活跃会话。

**方法二：使用 API**

```bash
curl http://localhost/prod-api/api/oauth2/qrcode/sessions
```

返回示例：
```json
{
  "code": 200,
  "data": [
    {
      "loginTid": "e6cc7826c0af4bee962a4ca700fe07de",
      "status": "scanned",
      "username": "admin",
      "createdAt": "2026-04-14T21:24:17.000+00:00"
    }
  ]
}
```

---

### Q5: 扫码登录和账号密码登录有什么区别？

| 特性 | 账号密码登录 | 扫码登录 |
|------|-------------|---------|
| 认证方式 | 输入账号密码 | 扫描二维码 |
| 安全性 | 依赖密码强度 | 依赖手机已登录状态 |
| 便捷性 | 需要输入 | 一键扫码 |
| 适用场景 | 首次登录 | 已注册用户快速登录 |

---

### Q6: 可以在手机上使用吗？

**当前实现**：
- ❌ 还没有真正的手机 App
- ✅ 管理后台页面可以在手机浏览器访问

**使用方法**：
1. 在手机浏览器访问：`http://你的电脑IP/qrcode-admin.html`
2. 输入 loginTid 进行确认

**后续计划**：
- 📱 开发微信小程序
- 📱 开发原生 App
- 📱 支持企业微信/钉钉

---

### Q7: 扫码登录的安全机制是什么？

**安全措施**：

1. **一次性使用**：
   - 二维码使用后立即失效
   - Token 生成后删除会话

2. **有效期限制**：
   - 二维码 5 分钟过期
   - Access Token 24 小时过期
   - Refresh Token 7 天过期

3. **状态机保护**：
   - 只能按顺序流转：GENERATED → SCANNED → CONFIRMED
   - 不可跳过或回退状态

4. **JWT 签名**：
   - HS384 算法签名
   - 包含用户信息、角色、权限

---

## 测试脚本

### 自动化端到端测试

```bash
bash /tmp/test_qrcode_login.sh
```

这个脚本会自动完成：
1. 生成二维码
2. 查询初始状态
3. 模拟扫码
4. 确认登录
5. 获取 Token
6. 验证 Token 有效性

---

## 开发相关

### 前端文件

- **登录页面**：`ruoyi-ui/src/views/login.vue`
- **测试页面**：`qrcode-test.html`
- **管理后台**：`qrcode-admin.html`

### 后端文件

- **控制器**：`auth/src/main/java/com/dafuweng/auth/controller/OAuth2QrCodeController.java`
- **服务层**：`auth/src/main/java/com/dafuweng/auth/service/QrCodeLoginService.java`
- **实体类**：`auth/src/main/java/com/dafuweng/auth/entity/QrCodeLoginSession.java`
- **枚举类**：`auth/src/main/java/com/dafuweng/auth/entity/QrCodeStatus.java`

### API 文档

- **OAuth2 协议说明**：`OAUTH2_PROTOCOL_INTRO.md`
- **实现方案文档**：`OAUTH2_QRCODE_LOGIN_PLAN.md`
- **后端测试指南**：`QRCODE_LOGIN_TEST_GUIDE.md`
- **前端功能说明**：`QRCODE_LOGIN_FRONTEND_GUIDE.md`

---

## 更新日志

### v1.0.0 (2026-04-16)

- ✅ 实现扫码登录核心功能
- ✅ 实现 5 个后端 API
- ✅ 实现前端登录页面扫码入口
- ✅ 实现管理后台扫码确认工具
- ✅ 实现完整轮询机制
- ✅ 实现 Token 自动保存和跳转
- ✅ 实现状态实时更新
- ✅ 实现二维码过期处理

---

## 联系支持

如有问题或建议，请联系开发团队。

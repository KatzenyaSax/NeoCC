# OAuth2 协议详解

> 创建日期: 2026-04-16  
> 状态: 📖 学习文档

## 📋 什么是 OAuth2？

**OAuth2（Open Authorization 2.0）** 是一个开放标准授权协议，允许用户授权第三方应用访问他们存储在另一个服务提供商上的资源，而无需将用户名和密码提供给第三方应用。

### 核心思想

OAuth2 的核心思想是：**"授权"而非"认证"**

- ❌ OAuth2 不是认证协议（虽然常用于登录场景）
- ✅ OAuth2 是授权协议，解决"如何安全地委托访问权限"的问题

### 典型应用场景

1. **第三方登录**：使用微信/支付宝/GitHub 账号登录其他网站
2. **API 授权访问**：允许应用访问你的 Google Drive、GitHub 仓库等
3. **扫码登录**：使用手机 App 扫码登录 Web 端（本项目要实现的功能）

---

## 🎭 OAuth2 的四个角色

```
┌─────────────┐         ┌──────────────┐         ┌──────────────┐
│   Resource  │         │              │         │    Client    │
│    Owner    │◄───────►│ Authorization│◄───────►│  Application │
│  (用户)     │         │   Server     │         │  (第三方应用) │
└─────────────┘         │  (认证服务器) │         └──────────────┘
                        │              │◄────────►┌──────────────┐
                        └──────────────┘         │   Resource   │
                                                 │    Server    │
                                                 │  (资源服务器) │
                                                 └──────────────┘
```

| 角色 | 说明 | 本系统对应 |
|------|------|-----------|
| **Resource Owner** | 资源所有者（用户） | 使用扫码登录的用户 |
| **Client** | 客户端应用 | Web 前端、手机 App |
| **Authorization Server** | 认证服务器 | Auth 服务 |
| **Resource Server** | 资源服务器 | Auth 服务 + 其他业务服务 |

---

## 🔄 OAuth2 的四种授权模式

### 1. 授权码模式（Authorization Code Grant）⭐ 最常用

**适用场景**：有后端服务器的 Web 应用

**流程**：
```
1. 用户访问客户端，点击"使用微信登录"
2. 客户端重定向到认证服务器
   https://auth.example.com/authorize?
     response_type=code
     &client_id=YOUR_CLIENT_ID
     &redirect_uri=https://yourapp.com/callback
     &state=random_state_string

3. 用户在认证服务器登录并授权
4. 认证服务器重定向回客户端，带上授权码
   https://yourapp.com/callback?code=AUTH_CODE&state=random_state_string

5. 客户端后端用授权码换取 Access Token（后端到后端）
   POST /oauth/token
   grant_type=authorization_code
   &code=AUTH_CODE
   &client_id=YOUR_CLIENT_ID
   &client_secret=YOUR_CLIENT_SECRET
   &redirect_uri=https://yourapp.com/callback

6. 认证服务器返回 Token
   {
     "access_token": "eyJhbGci...",
     "token_type": "Bearer",
     "expires_in": 3600,
     "refresh_token": "dGhpcyBp..."
   }

7. 客户端使用 Access Token 访问资源
```

**安全性**：⭐⭐⭐⭐⭐
- Token 不经过浏览器
- 需要 client_secret（后端保密）

---

### 2. 简化模式（Implicit Grant）

**适用场景**：纯前端应用（无后端）

**流程**：
```
1. 用户访问客户端，点击登录
2. 重定向到认证服务器
3. 用户授权
4. 认证服务器直接返回 Token（在 URL fragment 中）
   https://yourapp.com/callback#
     access_token=eyJhbGci...
     &token_type=Bearer
     &expires_in=3600
```

**安全性**：⭐⭐
- Token 暴露在浏览器 URL 中
- 不推荐使用（已被 OAuth 2.1 废弃）

---

### 3. 密码模式（Resource Owner Password Credentials）

**适用场景**：高度信任的官方应用

**流程**：
```
1. 用户在前端输入用户名密码
2. 客户端直接将密码发送给认证服务器
   POST /oauth/token
   grant_type=password
   &username=admin
   &password=admin123
   &client_id=YOUR_CLIENT_ID

3. 认证服务器验证密码，返回 Token
```

**安全性**：⭐⭐⭐
- 客户端直接处理用户密码
- 仅适用于官方应用

**注意**：本项目当前的登录方式类似于这种模式

---

### 4. 客户端凭证模式（Client Credentials Grant）

**适用场景**：服务器到服务器的认证（无用户参与）

**流程**：
```
POST /oauth/token
grant_type=client_credentials
&client_id=YOUR_CLIENT_ID
&client_secret=YOUR_CLIENT_SECRET
```

**安全性**：⭐⭐⭐⭐
- 用于服务间通信
- 不涉及用户授权

---

## 📱 扫码登录的 OAuth2 实现方案

### 扫码登录属于哪种模式？

扫码登录**不是标准的 OAuth2 授权模式**，而是基于 OAuth2 思想自定义的流程。它最接近**授权码模式**，但有特殊性。

### 扫码登录流程（自定义 OAuth2 变种）

```
┌─────────┐         ┌──────────┐         ┌──────────┐
│  Web端  │         │  服务器   │         │ 手机App  │
└────┬────┘         └────┬─────┘         └────┬─────┘
     │                   │                    │
     │ 1.请求生成二维码   │                    │
     │──────────────────►│                    │
     │   (含 login_tid)  │                    │
     │                   │                    │
     │ 2.返回二维码+tid   │                    │
     │◄──────────────────│                    │
     │                   │                    │
     │ 3.轮询扫码状态     │                    │
     │──────────────────►│                    │
     │   (wait...)       │                    │
     │                   │                    │
     │                   │ 4.用户扫码+授权     │
     │                   │◄───────────────────│
     │                   │   (POST /scan/login)│
     │                   │                    │
     │ 5.通知Web端已扫码  │                    │
     │◄──────────────────│                    │
     │   (status=scanned)│                    │
     │                   │                    │
     │ 6.轮询确认登录     │                    │
     │──────────────────►│                    │
     │                   │                    │
     │ 7.返回Token        │                    │
     │◄──────────────────│                    │
     │   (token+refresh) │                    │
     │                   │                    │
```

### 详细步骤

#### Step 1: Web 端请求生成二维码

```http
POST /api/oauth2/qrcode/generate
Content-Type: application/json

Response:
{
  "code": 200,
  "data": {
    "loginTid": "qr_8f3a2b1c9d4e5f6a",
    "qrcodeUrl": "data:image/png;base64,...",
    "expireIn": 300,
    "pollInterval": 2000
  }
}
```

**说明**：
- `loginTid`：登录事务唯一标识（Login Transaction ID）
- `qrcodeUrl`：Base64 编码的二维码图片
- `expireIn`：二维码有效期（秒）
- `pollInterval`：前端轮询间隔（毫秒）

**二维码内容**：
```
https://yourapp.com/scan/login?tid=qr_8f3a2b1c9d4e5f6a&client=web
```

---

#### Step 2: Web 端展示二维码并轮询状态

```javascript
// 前端伪代码
const loginTid = response.data.loginTid;

const pollTimer = setInterval(async () => {
  const res = await fetch(`/api/oauth2/qrcode/status?tid=${loginTid}`);
  const { status, token } = res.data;
  
  if (status === 'scanned') {
    // 已扫码，等待确认
    showConfirmHint();
  } else if (status === 'confirmed') {
    // 已确认登录
    clearInterval(pollTimer);
    saveToken(token);
    redirectToHome();
  } else if (status === 'expired') {
    // 二维码过期
    clearInterval(pollTimer);
    showExpiredHint();
  }
}, 2000);
```

---

#### Step 3: 手机 App 扫描二维码

用户打开手机 App（已登录状态），扫描 Web 端二维码。

App 解析二维码中的 `loginTid`。

---

#### Step 4: 手机 App 确认授权

```http
POST /api/oauth2/qrcode/confirm
Content-Type: application/json
Authorization: Bearer {mobile_app_token}

{
  "loginTid": "qr_8f3a2b1c9d4e5f6a",
  "action": "confirm",  // confirm 或 reject
  "deviceId": "iphone_13_pro"
}

Response:
{
  "code": 200,
  "message": "授权成功"
}
```

**说明**：
- 手机 App 必须已登录（持有有效 Token）
- 用户可以选择"确认登录"或"拒绝登录"

---

#### Step 5: Web 端收到 Token

轮询接口返回登录成功，包含 Token：

```json
{
  "code": 200,
  "data": {
    "status": "confirmed",
    "token": "eyJhbGci...",
    "refreshToken": "dGhpcyBp...",
    "expiresIn": 86400
  }
}
```

---

## 🔐 扫码登录的安全性设计

### 1. 二维码有效期限制

- ⏰ 二维码有效期：**5 分钟**
- 🔄 过期后必须重新生成
- 🗑️ 服务器自动清理过期二维码

### 2. 一次性使用

- ✅ 每个 `loginTid` 只能使用一次
- ✅ 扫码成功后立即失效
- ✅ 拒绝后不可再次使用

### 3. 状态机设计

```
[GENERATED] ──扫码──► [SCANNED] ──确认──► [CONFIRMED]
     │                      │
     │ 超时(5min)           │ 拒绝
     ▼                      ▼
[EXPIRED]              [REJECTED]
```

### 4. 防止中间人攻击

- 🔒 二维码 URL 使用 HTTPS
- 🔑 `loginTid` 使用 UUID v4（128 位随机）
- 🚫 不包含用户信息在二维码中

### 5. 设备绑定（可选）

- 📱 记录扫码设备的指纹
- 🆔 绑定 `loginTid` 和设备 ID
- ⚠️ 异常设备提醒用户

---

## 📊 OAuth2 vs 扫码登录对比

| 特性 | 标准 OAuth2 | 扫码登录（本项目） |
|------|-------------|-------------------|
| 授权方式 | 跳转授权页 | 扫码+确认 |
| Token 获取 | 后端换取 | 轮询获取 |
| 客户端类型 | 第三方应用 | 同一系统的 Web + App |
| 用户交互 | 登录+授权 | 已登录 App 扫码确认 |
| 安全性 | 高（授权码模式） | 高（需手机物理接触） |

---

## 🛡️ OAuth2 安全最佳实践

### 1. 使用 HTTPS

- ✅ 所有 OAuth2 端点必须使用 HTTPS
- ❌ 禁止 HTTP 传输 Token

### 2. 验证 state 参数

```javascript
// 生成随机 state
const state = crypto.randomBytes(16).toString('hex');

// 回调时验证
if (receivedState !== storedState) {
  throw new Error('CSRF attack detected!');
}
```

### 3. 使用 PKCE（移动端）

Proof Key for Code Exchange，防止授权码拦截攻击。

### 4. Token 安全存储

- 🌐 浏览器：HttpOnly Cookie 或 Memory
- 📱 移动端：Keychain（iOS）/ Keystore（Android）
- ❌ 禁止 LocalStorage（XSS 攻击）

### 5. Token 刷新机制

- ⏰ Access Token 短有效期（24 小时）
- 🔄 Refresh Token 长有效期（7 天）
- ♻️ 支持 Token 轮转

---

## 📚 参考资源

- [RFC 6749 - The OAuth 2.0 Authorization Framework](https://datatracker.ietf.org/doc/html/rfc6749)
- [OAuth 2.1 Draft](https://oauth.net/2.1/)
- [OAuth 2.0 Security Best Current Practice](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-security-topics)
- [PKCE Extension](https://datatracker.ietf.org/doc/html/rfc7636)

---

## 💡 总结

OAuth2 是一个强大的授权协议，虽然不是专门为登录设计的，但通过合理的自定义流程（如扫码登录），可以实现安全、便捷的用户认证体验。

**本项目扫码登录的核心优势**：
1. ✅ 无需输入密码（防钓鱼）
2. ✅ 手机物理接触（防远程攻击）
3. ✅ 二次确认（防误操作）
4. ✅ 设备可信（增强安全）

下一步：查看 `OAUTH2_QRCODE_LOGIN_PLAN.md` 了解详细实现方案。

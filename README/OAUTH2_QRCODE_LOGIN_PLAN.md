# 扫码登录实现方案

> 创建日期: 2026-04-16  
> 状态: 📋 方案设计（待评审）

## 🎯 需求分析

### 业务需求

1. 用户在 Web 端登录页面点击"扫码登录"
2. 系统生成动态二维码
3. 用户使用手机 App 扫描二维码
4. 手机 App 确认授权后，Web 端自动登录

### 技术目标

- ✅ 基于现有 JWT + Token 刷新机制
- ✅ 无需引入第三方 OAuth2 库（自定义实现）
- ✅ 支持二维码过期、刷新、状态轮询
- ✅ 保证安全性（一次性、防重放、防中间人）
- ✅ 与现有登录流程共存

---

## 🏗️ 架构设计

### 整体流程图

```
┌──────────────────────────────────────────────────────────────────────┐
│                          扫码登录流程                                 │
└──────────────────────────────────────────────────────────────────────┘

Web 前端                Auth 服务                 手机 App
   │                       │                        │
   │  1. 生成二维码请求     │                        │
   ├──────────────────────►│                        │
   │                       │                        │
   │  2. 返回二维码+tid     │                        │
   │◄──────────────────────┤                        │
   │                       │                        │
   │  3. 展示二维码         │                        │
   │                       │                        │
   │  4. 轮询状态(2s)      │                        │
   ├──────────────────────►│                        │
   │  等待中...             │                        │
   │◄──────────────────────┤                        │
   │                       │                        │
   │                       │  5. 扫码+确认授权       │
   │                       │◄───────────────────────┤
   │                       │   (需手机已登录)        │
   │                       │                        │
   │  6. 状态:已扫码        │                        │
   │◄──────────────────────┤                        │
   │                       │                        │
   │  7. 继续轮询           │                        │
   ├──────────────────────►│                        │
   │                       │                        │
   │  8. 返回Token          │                        │
   │◄──────────────────────┤                        │
   │                       │                        │
   │  9. 自动登录成功       │                        │
   │                       │                        │
```

---

## 📦 数据模型设计

### 1. 二维码登录会话表（内存存储）

```java
/**
 * 二维码登录会话信息
 */
public record QrCodeLoginSession(
    String loginTid,          // 登录事务ID (主键)
    Long userId,              // 扫码用户ID (扫码后填充)
    String username,          // 扫码用户名 (扫码后填充)
    QrCodeStatus status,      // 当前状态
    String deviceId,          // 扫码设备ID
    Long scannedAt,           // 扫码时间戳
    Long confirmedAt,         // 确认时间戳
    Long createdAt,           // 创建时间戳
    Long expireAt             // 过期时间戳
) {}

/**
 * 二维码状态枚举
 */
public enum QrCodeStatus {
    GENERATED,    // 已生成，等待扫码
    SCANNED,      // 已扫码，等待确认
    CONFIRMED,    // 已确认，登录成功
    REJECTED,     // 已拒绝
    EXPIRED       // 已过期
}
```

**存储方案**：使用 `ConcurrentHashMap<String, QrCodeLoginSession>` 内存存储

**清理策略**：
- 过期会话自动清理（定时任务，每分钟执行）
- 最大容量限制（10000 条）
- 状态终态（CONFIRMED/REJECTED/EXPIRED）立即删除

---

## 🔌 API 接口设计

### 1. 生成二维码

**接口路径**：`POST /api/oauth2/qrcode/generate`

**请求参数**：
```json
{
  "clientType": "web",           // 客户端类型
  "deviceName": "Chrome macOS"   // 设备描述（可选）
}
```

**响应格式**：
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "loginTid": "qr_8f3a2b1c9d4e5f6a7b0c1d2e3f4a5b6c",
    "qrcodeContent": "https://yourapp.com/scan/login?tid=qr_xxx&client=web",
    "qrcodeBase64": "data:image/png;base64,iVBORw0KGgo...",
    "expireIn": 300,
    "pollInterval": 2000
  }
}
```

**实现逻辑**：
1. 生成唯一 `loginTid`（UUID v4）
2. 创建 `QrCodeLoginSession`，状态为 `GENERATED`
3. 生成二维码内容 URL
4. 使用 ZXing 库生成二维码图片（Base64）
5. 存储到内存，返回给前端

---

### 2. 轮询二维码状态

**接口路径**：`GET /api/oauth2/qrcode/status?tid={loginTid}`

**请求参数**：
- `tid`（必填）：登录事务 ID

**响应格式（等待中）**：
```json
{
  "code": 200,
  "data": {
    "status": "generated",
    "message": "等待扫码"
  }
}
```

**响应格式（已扫码）**：
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

**响应格式（已确认）**：
```json
{
  "code": 200,
  "data": {
    "status": "confirmed",
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "refreshToken": "9f9beb95dc0147d398b4c36ca29b1083",
    "expiresIn": 86400,
    "refreshExpiresIn": 604800,
    "user": {
      "userId": 1,
      "username": "admin",
      "nickName": "管理员"
    }
  }
}
```

**响应格式（已过期）**：
```json
{
  "code": 400,
  "message": "二维码已过期，请刷新重试"
}
```

**实现逻辑**：
1. 根据 `tid` 查询会话
2. 检查是否过期（`expireAt < now`）
3. 返回当前状态
4. 如果状态为 `CONFIRMED`，生成 Token 并返回

---

### 3. 手机扫码确认

**接口路径**：`POST /api/oauth2/qrcode/confirm`

**请求头**：
```
Authorization: Bearer {mobile_app_token}
Content-Type: application/json
```

**请求参数**：
```json
{
  "loginTid": "qr_8f3a2b1c9d4e5f6a",
  "action": "confirm",        // confirm 或 reject
  "deviceId": "iPhone 13 Pro"
}
```

**响应格式**：
```json
{
  "code": 200,
  "message": "授权成功"
}
```

**实现逻辑**：
1. 从请求头解析手机用户的 Token
2. 验证 Token 有效性（调用 JwtUtil）
3. 查询 `loginTid` 对应的会话
4. 验证会话状态（必须是 `GENERATED`）
5. 更新会话状态：
   - `confirm` → `SCANNED` → 填充 `userId`、`username`、`scannedAt`
   - `reject` → `REJECTED`
6. 返回结果

---

### 4. 手机确认登录（二次确认）

**接口路径**：`POST /api/oauth2/qrcode/finalize`

**请求头**：
```
Authorization: Bearer {mobile_app_token}
```

**请求参数**：
```json
{
  "loginTid": "qr_8f3a2b1c9d4e5f6a"
}
```

**响应格式**：
```json
{
  "code": 200,
  "message": "登录成功"
}
```

**实现逻辑**：
1. 验证手机用户 Token
2. 查询会话（状态必须是 `SCANNED`，且 `userId` 匹配）
3. 更新状态为 `CONFIRMED`，填充 `confirmedAt`
4. 生成 Access Token 和 Refresh Token
5. 返回成功（Web 端轮询时会获取到 Token）

---

### 5. 刷新二维码（可选）

**接口路径**：`POST /api/oauth2/qrcode/refresh`

**请求参数**：
```json
{
  "oldLoginTid": "qr_old_tid"
}
```

**响应格式**：同"生成二维码"

**实现逻辑**：
1. 将旧会话标记为 `EXPIRED`
2. 调用"生成二维码"逻辑

---

## 🗂️ 代码结构设计

### 新增文件

```
auth/src/main/java/com/dafuweng/auth/
├── controller/
│   └── OAuth2QrCodeController.java        # OAuth2 二维码控制器
├── service/
│   └── QrCodeLoginService.java            # 二维码登录服务
├── entity/
│   ├── QrCodeLoginSession.java            # 二维码会话实体
│   └── QrCodeStatus.java                 # 状态枚举
└── utils/
    └── QrCodeGenerator.java               # 二维码生成工具
```

### 修改文件

```
auth/src/main/java/com/dafuweng/auth/
├── config/
│   └── SecurityConfig.java                # 添加白名单路由
```

---

## 💻 核心实现细节

### 1. QrCodeLoginService（核心服务）

```java
@Service
public class QrCodeLoginService {
    
    // 内存存储
    private final ConcurrentHashMap<String, QrCodeLoginSession> sessionStore;
    
    // 生成二维码会话
    public QrCodeSessionInfo generateSession(String clientType, String deviceName);
    
    // 查询状态
    public QrCodeStatusInfo queryStatus(String loginTid);
    
    // 扫码（手机 App 调用）
    public void scanCode(String loginTid, Long userId, String username, String deviceId);
    
    // 确认/拒绝
    public void confirmCode(String loginTid, Long userId, boolean confirm);
    
    // 获取 Token（Web 端轮询获取）
    public Map<String, Object> getTokens(String loginTid);
    
    // 清理过期会话
    @Scheduled(fixedRate = 60000)
    public void cleanExpiredSessions();
}
```

---

### 2. QrCodeGenerator（二维码生成）

**依赖**：引入 ZXing 库

```xml
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.2</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.2</version>
</dependency>
```

**实现逻辑**：
```java
public class QrCodeGenerator {
    
    /**
     * 生成二维码 Base64 图片
     * @param content 二维码内容（URL）
     * @param width 宽度
     * @param height 高度
     * @return Base64 编码的图片
     */
    public static String generateQrCodeBase64(String content, int width, int height);
}
```

---

### 3. OAuth2QrCodeController（控制器）

```java
@RestController
@RequestMapping("/api/oauth2/qrcode")
public class OAuth2QrCodeController {
    
    @Autowired
    private QrCodeLoginService qrCodeLoginService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TokenStoreService tokenStoreService;
    
    // 生成二维码
    @PostMapping("/generate")
    public Result<Map<String, Object>> generateQrCode(@RequestBody Map<String, String> request);
    
    // 轮询状态
    @GetMapping("/status")
    public Result<Map<String, Object>> queryStatus(@RequestParam String tid);
    
    // 扫码确认
    @PostMapping("/confirm")
    public Result<Void> confirmQrCode(
        @RequestHeader("Authorization") String token,
        @RequestBody Map<String, String> request
    );
    
    // 最终确认
    @PostMapping("/finalize")
    public Result<Void> finalizeQrCode(
        @RequestHeader("Authorization") String token,
        @RequestBody Map<String, String> request
    );
    
    // 刷新二维码
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshQrCode(@RequestBody Map<String, String> request);
}
```

---

## 🎨 前端实现方案

### 1. 登录页面改造

**文件**：`ruoyi-ui/src/views/login.vue`

**改动点**：
- 添加"扫码登录"切换按钮
- 添加二维码展示区域
- 添加轮询逻辑
- 添加扫码成功后的自动登录

**UI 结构**：
```vue
<template>
  <div class="login">
    <el-form v-if="loginMode === 'account'" ...>
      <!-- 原有账号密码登录 -->
    </el-form>
    
    <div v-if="loginMode === 'qrcode'" class="qrcode-login">
      <h3 class="title">扫码登录</h3>
      <div class="qrcode-container">
        <img :src="qrcodeBase64" alt="二维码" />
        <div v-if="qrcodeExpired" class="expired-mask">
          <p>二维码已过期</p>
          <el-button @click="refreshQrCode">刷新二维码</el-button>
        </div>
      </div>
      <p class="hint">{{ statusHint }}</p>
      <el-button @click="switchToAccount">返回账号登录</el-button>
    </div>
    
    <div class="login-mode-switch">
      <el-button text @click="switchLoginMode">
        {{ loginMode === 'account' ? '扫码登录' : '账号密码登录' }}
      </el-button>
    </div>
  </div>
</template>
```

---

### 2. 轮询逻辑实现

```javascript
let pollTimer = null;

async function startPolling(loginTid) {
  pollTimer = setInterval(async () => {
    try {
      const res = await axios.get(`/api/oauth2/qrcode/status?tid=${loginTid}`);
      const { status, token, refreshToken } = res.data.data;
      
      if (status === 'scanned') {
        statusHint.value = '已扫码，请在手机上确认';
      } else if (status === 'confirmed') {
        clearInterval(pollTimer);
        statusHint.value = '登录成功，正在跳转...';
        
        // 保存 Token
        setToken(token);
        setRefreshToken(refreshToken);
        
        // 跳转到首页
        router.push('/');
      } else if (status === 'rejected') {
        clearInterval(pollTimer);
        ElMessage.error('登录已被拒绝');
        refreshQrCode();
      }
    } catch (error) {
      if (error.response?.data?.message?.includes('过期')) {
        clearInterval(pollTimer);
        qrcodeExpired.value = true;
      }
    }
  }, 2000);
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
}
```

---

## 🔒 安全设计

### 1. 防重放攻击

- ✅ `loginTid` 使用 UUID v4（128 位随机）
- ✅ 每个 `loginTid` 只能使用一次
- ✅ 状态终态后立即删除会话

### 2. 防中间人攻击

- ✅ 二维码内容使用 HTTPS
- ✅ 不包含敏感信息（用户名、密码）
- ✅ Token 仅通过轮询接口返回（不在二维码中）

### 3. 防暴力破解

- ✅ 二维码有效期 5 分钟
- ✅ 轮询间隔 2 秒（降低服务器压力）
- ✅ 最大容量限制（10000 条会话）

### 4. 设备可信

- ✅ 记录扫码设备 ID
- ✅ 可选：绑定设备指纹
- ✅ 可选：异常设备提醒

### 5. Token 安全

- ✅ 使用现有 JWT 机制
- ✅ 支持 Refresh Token 轮转
- ✅ 支持 Token 黑名单

---

## 📊 状态机设计

```
┌───────────┐
│ GENERATED │ ◄── 初始状态
└─────┬─────┘
      │
      │ 手机扫码
      ▼
┌───────────┐
│  SCANNED  │ ◄── 已扫码，等待确认
└─────┬─────┘
      │
      │ 手机确认
      ▼
┌───────────┐
│ CONFIRMED │ ◄── 登录成功（终态）
└───────────┘

其他分支：
GENERATED ──超时(5min)──► EXPIRED
GENERATED ──手机拒绝──► REJECTED
SCANNED ──手机拒绝──► REJECTED
```

---

## 🧪 测试方案

### 1. 单元测试

- ✅ 生成二维码会话
- ✅ 状态查询（各状态分支）
- ✅ 扫码确认逻辑
- ✅ Token 生成逻辑
- ✅ 过期会话清理

### 2. 集成测试

- ✅ 完整扫码登录流程
- ✅ 二维码过期处理
- ✅ 并发扫码测试
- ✅ Token 有效性验证

### 3. 安全测试

- ✅ 重放攻击测试（重复使用 loginTid）
- ✅ 中间人攻击测试（篡改二维码内容）
- ✅ 暴力破解测试（大量生成二维码）

---

## 🚀 部署步骤

### 1. 数据库变更

**无需数据库变更**（使用内存存储）

### 2. 依赖更新

```xml
<!-- ZXing 二维码库 -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.2</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.2</version>
</dependency>
```

### 3. Gateway 路由配置

```yaml
# 添加 OAuth2 二维码路由
- id: auth-oauth2-qrcode
  uri: http://auth-service:8085
  predicates:
    - Path=/prod-api/api/oauth2/**
  filters:
    - StripPrefix=1
```

### 4. Security 白名单

```java
// OAuth2 二维码接口（部分需要认证）
.requestMatchers("/api/oauth2/qrcode/generate").permitAll()
.requestMatchers("/api/oauth2/qrcode/status").permitAll()
.requestMatchers("/api/oauth2/qrcode/confirm").authenticated()  // 需要手机 Token
.requestMatchers("/api/oauth2/qrcode/finalize").authenticated() // 需要手机 Token
```

---

## 📈 性能优化

### 1. 内存优化

- 定时清理过期会话（每分钟）
- 状态终态立即删除
- 最大容量限制（10000 条）

### 2. 轮询优化

- 前端动态调整轮询间隔（指数退避）
- 使用 Server-Sent Events（可选，替代轮询）
- WebSocket 推送（可选，最优方案）

### 3. 二维码生成优化

- 缓存二维码图片（相同内容不重复生成）
- 异步生成二维码（不阻塞主流程）

---

## 🔮 未来扩展

### 1. WebSocket 替代轮询

```javascript
const ws = new WebSocket('ws://yourapp.com/ws/oauth2/qrcode');
ws.send(JSON.stringify({ type: 'subscribe', tid: loginTid }));
ws.onmessage = (event) => {
  const { status, token } = JSON.parse(event.data);
  // 实时接收状态变化
};
```

### 2. 多设备管理

- 显示已扫码设备信息
- 支持一键踢出设备
- 设备信任列表

### 3. 统计分析

- 扫码登录使用率
- 平均扫码耗时
- 失败率统计

---

## ⚠️ 注意事项

1. **手机 App 必须已登录**：扫码接口需要手机 Token
2. **二维码有效期**：5 分钟后必须刷新
3. **网络要求**：Web 端和手机 App 都能访问 Auth 服务
4. **HTTPS 要求**：生产环境必须使用 HTTPS
5. **兼容性**：与现有账号密码登录共存，互不影响

---

## 📝 总结

本方案基于现有 JWT + Token 刷新机制，实现了一个安全、便捷的扫码登录功能。核心特点：

- ✅ **无需引入第三方 OAuth2 库**（自定义实现）
- ✅ **内存存储**（高性能，无需 Redis）
- ✅ **完整的状态机**（生成→扫码→确认→登录）
- ✅ **安全设计**（防重放、防中间人、防暴力）
- ✅ **与现有系统兼容**（不破坏原有登录流程）

**下一步**：评审方案后，开始编码实现。

<template>
  <div class="login">
    <!-- 账号密码登录 -->
    <el-form v-if="loginMode === 'account'" ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">{{ title }}</h3>
      <el-form-item prop="username">
        <el-input
          v-model="loginForm.username"
          type="text"
          size="large"
          auto-complete="off"
          placeholder="账号"
        >
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          size="large"
          auto-complete="off"
          placeholder="密码"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox>
      <el-form-item style="width:100%;">
        <el-button
          :loading="loading"
          size="large"
          type="primary"
          style="width:100%;"
          @click.prevent="handleLogin"
        >
          <span v-if="!loading">登 录</span>
          <span v-else>登 录 中...</span>
        </el-button>
      </el-form-item>
    </el-form>

    <!-- 扫码登录 -->
    <div v-if="loginMode === 'qrcode'" class="qrcode-login-form">
      <h3 class="title">扫码登录</h3>
      <div class="qrcode-container">
        <div v-loading="qrcodeLoading" element-loading-text="正在生成二维码...">
          <img v-if="qrcodeBase64" :src="qrcodeBase64" alt="二维码" class="qrcode-image" />
        </div>
        <div class="qrcode-status">{{ qrcodeStatusText }}</div>
        <div v-if="qrcodeExpired" class="qrcode-expired">
          <p>二维码已过期</p>
          <el-button type="primary" size="small" @click="refreshQrCode">点击刷新</el-button>
        </div>
      </div>
      <p class="qrcode-hint">打开管理后台页面扫码确认</p>
      <el-button text type="primary" class="switch-btn" @click="switchToAccount">返回账号登录</el-button>
    </div>

    <!-- 登录方式切换 -->
    <div class="login-mode-switch">
      <el-button text @click="switchLoginMode">
        {{ loginMode === 'account' ? '扫码登录' : '账号密码登录' }}
      </el-button>
    </div>

    <!--  底部  -->
    <div class="el-login-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script setup>
import Cookies from "js-cookie"
import { encrypt, decrypt } from "@/utils/jsencrypt"
import useUserStore from '@/store/modules/user'
import defaultSettings from '@/settings'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const title = import.meta.env.VITE_APP_TITLE
const footerContent = defaultSettings.footerContent
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

// 登录模式：account（账号密码）| qrcode（扫码登录）
const loginMode = ref('account')

// 扫码登录相关状态
const loginTid = ref('')
const qrcodeBase64 = ref('')
const qrcodeLoading = ref(false)
const qrcodeStatus = ref('')
const qrcodeStatusText = ref('请使用管理后台扫码')
const qrcodeExpired = ref(false)
let pollTimer = null

const loginForm = ref({
  username: "admin",
  password: "admin123",
  rememberMe: false
})

const loginRules = {
  username: [{ required: true, trigger: "blur", message: "请输入您的账号" }],
  password: [{ required: true, trigger: "blur", message: "请输入您的密码" }]
}

const loading = ref(false)
const redirect = ref(undefined)

watch(route, (newRoute) => {
    redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

// 切换登录模式
function switchLoginMode() {
  if (loginMode.value === 'account') {
    loginMode.value = 'qrcode'
    generateQrCode()
  } else {
    loginMode.value = 'account'
    stopPolling()
  }
}

// 切换到账号登录
function switchToAccount() {
  loginMode.value = 'account'
  stopPolling()
}

// 生成二维码
async function generateQrCode() {
  qrcodeLoading.value = true
  qrcodeExpired.value = false
  qrcodeStatusText.value = '正在生成二维码...'
  
  try {
    const response = await request({
      url: '/api/oauth2/qrcode/generate',
      method: 'post',
      data: {
        clientType: 'web',
        deviceName: getBrowserInfo()
      }
    })
    
    if (response.code === 200) {
      loginTid.value = response.data.loginTid
      qrcodeBase64.value = response.data.qrcodeBase64
      qrcodeStatus.value = 'generated'
      qrcodeStatusText.value = '请使用管理后台扫码'
      
      // 开始轮询
      startPolling()
    }
  } catch (error) {
    console.error('生成二维码失败:', error)
    qrcodeStatusText.value = '生成失败，点击刷新重试'
    qrcodeExpired.value = true
  } finally {
    qrcodeLoading.value = false
  }
}

// 刷新二维码
function refreshQrCode() {
  generateQrCode()
}

// 开始轮询
function startPolling() {
  stopPolling() // 先清除之前的定时器
  
  pollTimer = setInterval(async () => {
    try {
      const response = await request({
        url: '/api/oauth2/qrcode/status',
        method: 'get',
        params: {
          tid: loginTid.value
        }
      })
      
      if (response.code === 200) {
        const { status, token, refreshToken, message } = response.data
        
        qrcodeStatus.value = status
        
        // 根据状态更新提示文字
        if (status === 'scanned') {
          qrcodeStatusText.value = '已扫码，请在管理后台确认'
        } else if (status === 'confirmed') {
          // 登录成功
          stopPolling()
          qrcodeStatusText.value = '登录成功，正在跳转...'
          
          // 保存 Token
          setToken(token)
          setRefreshToken(refreshToken)
          
          ElMessage.success('扫码登录成功！')
          
          // 跳转到首页
          setTimeout(() => {
            router.push({ path: redirect.value || '/' })
          }, 500)
        } else if (status === 'rejected') {
          stopPolling()
          qrcodeStatusText.value = '登录已被拒绝'
          ElMessage.error('登录已被拒绝')
          setTimeout(() => {
            refreshQrCode()
          }, 2000)
        }
      }
    } catch (error) {
      console.error('轮询状态失败:', error)
      // 检查是否是过期错误
      if (error.response?.data?.code === 400 || 
          (error.response?.data?.message && error.response.data.message.includes('过期'))) {
        stopPolling()
        qrcodeExpired.value = true
        qrcodeStatusText.value = '二维码已过期，请点击刷新'
      }
    }
  }, 2000) // 每 2 秒轮询一次
}

// 停止轮询
function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

// 设置 Token
function setToken(token) {
  userStore.token = token
  Cookies.set('Admin-Token', token)
}

// 设置 Refresh Token
function setRefreshToken(refreshToken) {
  Cookies.set('Refresh-Token', refreshToken)
}

// 获取浏览器信息
function getBrowserInfo() {
  const userAgent = navigator.userAgent
  if (userAgent.includes('Chrome')) return 'Chrome'
  if (userAgent.includes('Firefox')) return 'Firefox'
  if (userAgent.includes('Safari')) return 'Safari'
  return 'Browser'
}

function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (valid) {
      loading.value = true
      // 勾选了需要记住密码设置在 cookie 中设置记住用户名和密码
      if (loginForm.value.rememberMe) {
        Cookies.set("username", loginForm.value.username, { expires: 30 })
        Cookies.set("password", encrypt(loginForm.value.password), { expires: 30 })
        Cookies.set("rememberMe", loginForm.value.rememberMe, { expires: 30 })
      } else {
        // 否则移除
        Cookies.remove("username")
        Cookies.remove("password")
        Cookies.remove("rememberMe")
      }
      // 调用action的登录方法
      userStore.login(loginForm.value).then(() => {
        const query = route.query
        const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
          if (cur !== "redirect") {
            acc[cur] = query[cur]
          }
          return acc
        }, {})
        router.push({ path: redirect.value || "/", query: otherQueryParams })
      }).catch(() => {
        loading.value = false
      })
    }
  })
}

// 组件卸载时清除轮询
onBeforeUnmount(() => {
  stopPolling()
})

function getCookie() {
  const username = Cookies.get("username")
  const password = Cookies.get("password")
  const rememberMe = Cookies.get("rememberMe")
  if (username) loginForm.value.username = username
  if (password) {
    try {
      loginForm.value.password = decrypt(password)
    } catch (e) {
      // 解密失败，使用空密码
      loginForm.value.password = ""
    }
  }
  if (rememberMe) loginForm.value.rememberMe = Boolean(rememberMe)
}

// 组件卸载时清除轮询
onBeforeUnmount(() => {
  stopPolling()
})

getCookie()
</script>

<style lang='scss' scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("../assets/images/login-background.jpg");
  background-size: cover;
  position: relative;
}
.title {
  margin: 0px auto 30px auto;
  text-align: center;
  color: #707070;
}

.login-form {
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 5px 25px;
  z-index: 1;
  .el-input {
    height: 40px;
    input {
      height: 40px;
    }
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 0px;
  }
}

// 扫码登录样式
.qrcode-login-form {
  border-radius: 6px;
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 25px 25px;
  z-index: 1;
  text-align: center;
}

.qrcode-container {
  margin: 0 auto 20px;
  width: 300px;
  height: 300px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.qrcode-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.qrcode-status {
  margin-top: 15px;
  font-size: 14px;
  color: #606266;
  min-height: 20px;
}

.qrcode-expired {
  margin-top: 15px;
  padding: 15px;
  background: #fef0f0;
  border-radius: 6px;
  border: 1px solid #fbc4c4;
  
  p {
    margin: 0 0 10px 0;
    color: #f56c6c;
    font-size: 14px;
  }
}

.qrcode-hint {
  font-size: 13px;
  color: #909399;
  margin: 15px 0;
}

.switch-btn {
  margin-top: 10px;
}

// 登录模式切换按钮
.login-mode-switch {
  position: absolute;
  bottom: 60px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 2;
}

.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}
.login-code {
  width: 33%;
  height: 40px;
  float: right;
  img {
    cursor: pointer;
    vertical-align: middle;
  }
}
.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
.login-code-img {
  height: 40px;
  padding-left: 12px;
}

html.dark .login {
  background-image: linear-gradient(rgba(0, 0, 0, 0.55), rgba(0, 0, 0, 0.55)), url("../assets/images/login-background.jpg");
  .login-form,
  .qrcode-login-form {
    background: var(--el-bg-color-overlay) !important;
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.5);
  }
}
</style>

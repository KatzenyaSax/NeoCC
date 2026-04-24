import router from '@/router'
import { login as loginApi, logout as logoutApi, getInfo as getInfoApi } from '@/api/login'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { isHttp, isEmpty } from "@/utils/validate"
import useLockStore from '@/store/modules/lock'
import defAva from '@/assets/images/profile.jpg'

const useUserStore = defineStore(
  'user',
  {
    state: () => ({
      token: getToken(),
      id: '',
      name: '',
      nickName: '',
      avatar: '',
      deptId: null,
      zoneId: null,
      roles: [],
      permissions: []
    }),
    actions: {
      // 登录
      login(userInfo) {
        const username = userInfo.username.trim()
        const password = userInfo.password
        return new Promise((resolve, reject) => {
          loginApi(username, password).then(res => {
            // NeoCC 后端返回格式: { code: 200, message: "success", data: { token: "xxx" } }
            const token = res.data?.token || res.token
            if (!token) {
              reject(new Error('登录失败：未获取到 token'))
              return
            }
            setToken(token)
            this.token = token
            useLockStore().unlockScreen()
            resolve()
          }).catch(error => {
            reject(error)
          })
        })
      },
      // 获取用户信息
      getInfo() {
        return new Promise((resolve, reject) => {
          getInfoApi().then(res => {
            // AuthController.getInfo() 返回: { code, message, data: { userId, userName, nickName, avatar, roles, permissions } }
            // data 本身就是用户信息，不是嵌套的 { user: {...}, roles, permissions }
            const data = res.data || res

            let avatar = data.avatar || ""
            if (!isHttp(avatar)) {
              avatar = (isEmpty(avatar)) ? defAva : import.meta.env.VITE_APP_BASE_API + avatar
            }

            // 处理角色和权限
            if (data.roles && data.roles.length > 0) {
              this.roles = data.roles || []
              this.permissions = data.permissions || []
            } else {
              this.roles = ['ROLE_DEFAULT']
              this.permissions = ['*:*:*']
            }

            this.id = data.userId || data.id || ''
            this.name = data.userName || data.username || ''
            this.nickName = data.nickName || data.realName || this.name
            this.avatar = avatar
            this.deptId = data.deptId || null
            this.zoneId = data.zoneId || null

            resolve(res)
          }).catch(error => {
            reject(error)
          })
        })
      },
      // 退出系统 - 不调用后端 logout 接口，直接清除本地状态
      logOut() {
        return new Promise((resolve) => {
          // 尝试调用后端 logout，但忽略任何错误
          logoutApi().catch(() => {})
          
          // 清除本地状态
          this.token = ''
          this.roles = []
          this.permissions = []
          removeToken()
          resolve()
        })
      }
    }
  })

export default useUserStore

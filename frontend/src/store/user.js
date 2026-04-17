import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, logout } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  /**
   * 用户登录
   */
  function userLogin(loginForm) {
    return login(loginForm).then(res => {
      token.value = res.data.token
      refreshToken.value = res.data.refreshToken
      userInfo.value = res.data.userInfo
      
      // 存储到 localStorage
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('refreshToken', res.data.refreshToken)
      localStorage.setItem('userInfo', JSON.stringify(res.data.userInfo))
      
      return res
    })
  }

  /**
   * 用户登出
   */
  function userLogout() {
    return logout().then(() => {
      token.value = ''
      refreshToken.value = ''
      userInfo.value = {}
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userInfo')
    })
  }

  return {
    token,
    refreshToken,
    userInfo,
    userLogin,
    userLogout
  }
})

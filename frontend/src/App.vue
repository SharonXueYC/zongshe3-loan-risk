<template>
  <!-- 根据登录状态显示不同组件 -->
  <Login v-if="!isLoggedIn" @login-success="handleLoginSuccess" />
  <Dashboard v-else @logout="handleLogout" />
</template>

<script>
// 导入组件
import Login from './components/Login.vue'
import Dashboard from './components/Dashboard.vue'
import { checkAuth, logout as apiLogout } from './api/admin'
import { clearAuth, getToken } from './api/client'

export default {
  name: 'App',
  components: {
    Login,
    Dashboard
  },
  data() {
    return {
      isLoggedIn: false
    }
  },
  mounted() {
    this.checkLoginStatus()
  },
  methods: {
    async checkLoginStatus() {
      const token = getToken()
      if (!token) {
        this.isLoggedIn = false
        return
      }
      try {
        const res = await checkAuth()
        this.isLoggedIn = !!res.success
        if (!res.success) {
          clearAuth()
        }
      } catch (_) {
        clearAuth()
        this.isLoggedIn = false
      }
    },
    handleLoginSuccess() {
      this.isLoggedIn = true
    },
    async handleLogout() {
      await apiLogout()
      clearAuth()
      this.isLoggedIn = false
    }
  }
}
</script>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: Arial, sans-serif;
  background-color: #f5f5f5;
}
</style>
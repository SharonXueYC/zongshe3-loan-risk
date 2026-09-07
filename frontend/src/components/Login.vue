<template>
  <div class="login-container">
    <div class="login-box">
      <h1 class="login-title">闪借</h1>
      <p class="login-subtitle">管理员登录</p>
      <form class="login-form" @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名</label>
          <input 
            type="text" 
            id="username" 
            v-model="loginForm.username" 
            placeholder="请输入用户名"
            required
          >
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input 
            type="password" 
            id="password" 
            v-model="loginForm.password" 
            placeholder="请输入密码"
            required
          >
        </div>
        <div class="form-group remember">
          <input type="checkbox" id="remember" v-model="loginForm.remember">
          <label for="remember">记住我</label>
        </div>
        <button type="submit" class="login-button">登录</button>
      </form>
    </div>
  </div>
</template>

<script>
import { login } from '../api/admin'
import { setToken, setAdminUser } from '../api/client'

export default {
  name: 'Login',
  data() {
    return {
      loginForm: {
        username: '',
        password: '',
        remember: false
      },
      loading: false
    }
  },
  methods: {
    async handleLogin() {
      if (!this.loginForm.username || !this.loginForm.password) {
        alert('请输入用户名和密码')
        return
      }
      this.loading = true
      try {
        const res = await login(this.loginForm.username, this.loginForm.password)
        if (res.success && res.token) {
          setToken(res.token, this.loginForm.remember)
          if (res.admin) {
            setAdminUser(res.admin, this.loginForm.remember)
          }
          this.$emit('login-success')
        } else {
          alert(res.message || '登录失败')
        }
      } catch (error) {
        alert(error.message || '登录失败，请检查用户名和密码')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
/* 登录界面样式 */
.login-container {
  font-family: Arial, sans-serif;
  min-height: 100vh;
  background-color: #f3e8ff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.login-box {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  padding: 40px;
  width: 100%;
  max-width: 400px;
  text-align: center;
}

.login-title {
  color: #d8b4fe;
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 10px;
}

.login-subtitle {
  color: #666;
  font-size: 18px;
  margin-bottom: 30px;
}

.login-form {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 20px;
}

.form-group {
  width: 100%;
  text-align: left;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: bold;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #d8b4fe;
  box-shadow: 0 0 0 2px rgba(216, 180, 254, 0.2);
}

.form-group.remember {
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-group.remember input {
  width: auto;
  margin: 0;
}

.form-group.remember label {
  margin: 0;
  font-weight: normal;
  cursor: pointer;
}

.login-button {
  width: 100%;
  padding: 12px;
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-top: 10px;
}

.login-button:hover {
  background-color: #45a049;
}

.login-button:active {
  background-color: #388e3c;
}
</style>